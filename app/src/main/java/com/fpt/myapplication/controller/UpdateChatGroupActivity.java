package com.fpt.myapplication.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.model.ChatGroupModel;
import com.fpt.myapplication.util.FileUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.fpt.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.MultipartBody;


public class UpdateChatGroupActivity extends AppCompatActivity {

    private ImageView imgGroupAvatar;
    private TextInputEditText edtGroupName;
    private MaterialButton btnChangeAvatar, btnSave;

    private ChatGroupModel chatGroupModel;
    private String currentImageUrl;
    private Uri selectedImageUri;
    private int groupId;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_group_chat_layout);
        chatGroupModel = new ChatGroupModel(this);
        initViews();
        setupImagePicker();
        loadDataFromIntent();
        setupListeners();
    }

    private void initViews() {
        imgGroupAvatar = findViewById(R.id.imgAvatar);
        edtGroupName = findViewById(R.id.edtGroupName);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this)
                                .load(uri)
                                .into(imgGroupAvatar);
                    }
                }
        );
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String groupName = intent.getStringExtra("groupName");
            currentImageUrl = intent.getStringExtra("imageUrl");
            groupId = intent.getIntExtra("groupId", -1);

            if (!TextUtils.isEmpty(groupName)) {
                edtGroupName.setText(groupName);
            }

            if (!TextUtils.isEmpty(currentImageUrl)) {
                Glide.with(this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.ic_group)
                    .into(imgGroupAvatar);
            }
        }
    }

    private void setupListeners() {
        btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveChanges());
    }


    private void saveChanges() {
        String groupName = edtGroupName.getText().toString().trim();

        if (TextUtils.isEmpty(groupName)) {
            edtGroupName.setError("Vui lòng nhập tên nhóm");
            edtGroupName.requestFocus();
            return;
        }


        List<MultipartBody.Part> parts = new ArrayList<>();
        if (selectedImageUri != null) {
            try {
                MultipartBody.Part imagePart = FileUtil.uriToPart("avatar", selectedImageUri, this);
                parts.add(imagePart);
            } catch (Exception e) {
                e.printStackTrace();
                btnSave.setText("Lưu");
                btnSave.setEnabled(true);
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Lỗi")
                        .setContentText("Lỗi khi xử lý ảnh")
                        .show();
                return;
            }
        }
        MultipartBody.Part namePart = FileUtil.stringToPart("name",groupName);
        parts.add(namePart);

        chatGroupModel.updateGroupChat(parts, groupId ,new ChatGroupModel.UpdateGroupChatCallBack() {
            @Override
            public void onSuccess() {
                btnSave.setText("Lưu");
                btnSave.setEnabled(true);
                new SweetAlertDialog(UpdateChatGroupActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Thành công")
                        .setContentText("Cập nhật nhóm thành công")
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
                            finish();
                        })
                        .show();
            }

            @Override
            public void onError(ResponseError error) {
                btnSave.setText("Lưu");
                btnSave.setEnabled(true);
                new SweetAlertDialog(UpdateChatGroupActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Thất bại")
                        .setContentText(error != null ? error.message : "Có lỗi xảy ra")
                        .show();
            }

            @Override
            public void onLoading() {
                btnSave.setText("Đang cập nhật...");
                btnSave.setEnabled(false);
            }
        });
    }
}