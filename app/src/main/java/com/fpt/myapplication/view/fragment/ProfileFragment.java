package com.fpt.myapplication.view.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.fpt.myapplication.R;
import com.fpt.myapplication.controller.LoginActivity;
import com.fpt.myapplication.controller.RegisterActivity;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.UserModel;
import com.fpt.myapplication.util.FileUtil;
import com.fpt.myapplication.util.OnProfileUpdated;
import com.fpt.myapplication.util.SessionPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;

public class ProfileFragment extends Fragment {

    private OnProfileUpdated callback;
    private ActivityResultLauncher<String> pickImageLauncher;
    private CircleImageView imgAvatar;
    private Uri selectedAvatarUri = null;
    private TextView tvTitle;
    private TextInputEditText etEmail;

    private TextInputLayout tilDisplayName;

    private UserModel userModel;
    private MaterialButton btnSave, btnLogout;
    private TextInputEditText etDisplayName;
    public ProfileFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_layout, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đăng ký chọn ảnh (Fragment-safe)
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        if (imgAvatar != null) {
                            selectedAvatarUri = uri;
                            Glide.with(this)
                                    .load(uri)
                                    .into(imgAvatar);
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvTitle = view.findViewById(R.id.tvTitle);
        etEmail = view.findViewById(R.id.etEmail);
        tilDisplayName = view.findViewById(R.id.tilDisplayName);
        etDisplayName = view.findViewById(R.id.etDisplayName);
        btnSave = view.findViewById(R.id.btnSave);
        userModel = new UserModel(requireContext());
        btnLogout = view.findViewById(R.id.btnLogout);
        UserResponse user = SessionPrefs.get(requireContext()).getUser();


        if (user != null) {
            tvTitle.setText("Profile: " + user.getDisplayName());
            etEmail.setText(user.getEmail());
            etDisplayName.setText(user.getDisplayName());
            Glide.with(this)
                    .load(FileUtil.GetImageUrl(user.getAvatar()))
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(imgAvatar);
        }
        // Nút chọn ảnh phải tìm từ "view", không phải từ Fragment trực tiếp
        View btnPick = view.findViewById(R.id.btnPickAvatar);

        btnSave.setOnClickListener(v -> {
            List<MultipartBody.Part> parts = new ArrayList<>();
            String name = etDisplayName.getText() != null ? etDisplayName.getText().toString().trim() : "";
            if (selectedAvatarUri != null) {
                try {
                    parts.add(FileUtil.uriToPart( "avatar" ,selectedAvatarUri, requireContext()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            MultipartBody.Part namePart = FileUtil.stringToPart( "displayName", name);
            parts.add(namePart);
            userModel.updateAccount(parts, new UserModel.UpdateAccountCallBack() {
                @Override
                public void onLoading() {
                    btnSave.setEnabled(false);
                    btnSave.setText("Saving...");
                }
                @Override
                public void onSuccess(UserResponse response) {
                    new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Thông báo ")
                            .setContentText("Cập nhật thành công")
                            .setConfirmText("OK")
                            .show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    SessionPrefs.get(requireContext()).saveUser(response);
                    tvTitle.setText("Profile: " + response.getDisplayName());
                    tilDisplayName.setError(null);
                    if (callback != null) {
                        callback.onProfileUpdated(response.getDisplayName());
                    }
                }

                @Override
                public void onError(ResponseError error) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Thông báo")
                            .setContentText(error.message != null ? error.message : "Có lỗi xảy ra")
                            .setConfirmText("OK")
                            .show();
                }
            });

        });

        btnLogout.setOnClickListener(v -> {
            new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Xác nhận đăng xuất")
                    .setContentText("Bạn có chắc chắn muốn đăng xuất?")
                    .setConfirmText("Đăng xuất")
                    .setCancelText("Hủy")
                    .showCancelButton(true)
                    .setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        SessionPrefs.get(requireContext()).clearAll();
                        LoginActivity.startAsNewTask(requireContext());
                    })
                    .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                    .show();
        });

        btnPick.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ưu tiên lấy từ Activity
        if (context instanceof OnProfileUpdated) {
            callback = (OnProfileUpdated) context;
        } else {
            // Nếu fragment này nằm trong một fragment cha implements callback
            Fragment parent = getParentFragment();
            if (parent instanceof OnProfileUpdated) {
                callback = (OnProfileUpdated) parent;
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null; // tránh leak
    }
}
