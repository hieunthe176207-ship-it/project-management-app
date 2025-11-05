package com.fpt.myapplication.view.fragment;



import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.model.UserModel;
import com.fpt.myapplication.view.adapter.AddUserDialogAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class AddMemberDialog extends DialogFragment {


    // -------------------- CALLBACK RA ACTIVITY --------------------

    /** Callback trả về khi thêm xong để Activity reload danh sách. */
    public interface OnMembersAdded {
        void onAdded(int count);
    }
    private OnMembersAdded callback;
    public void setOnMembersAdded(OnMembersAdded cb) { this.callback = cb; }


    // -------------------- ARGUMENTS --------------------

    /**
     * Tạo instance dialog kèm projectId trong arguments (an toàn khi xoay màn hình).
     */
    public static AddMemberDialog newInstance(int projectId) {
        AddMemberDialog d = new AddMemberDialog();
        Bundle b = new Bundle();
        b.putInt(ARG_PROJECT_ID, projectId);
        d.setArguments(b);
        return d;
    }

    // -------------------- STATE --------------------

    private int projectId;
    private ProjectModel projectModel;
    private UserModel userModel;
    private View rowPicked;                  // container phần "Đã chọn" (ẩn/hiện theo state)
    private RecyclerView rvPicked, rvUsers;
    private android.widget.EditText etSearch;

    private AddUserDialogAdapter pickedAdapter, usersAdapter;

    // Nguồn dữ liệu + state chọn:
    private final List<UserResponse> allUsers = new ArrayList<>();
    private final LinkedHashMap<Integer, UserResponse> pickedMap = new LinkedHashMap<>();

    private boolean isSubmitting = false;    // chặn double-click nút "Thêm" trong lúc submit

    private static final String ARG_PROJECT_ID = "arg_project_id";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View root = getLayoutInflater().inflate(R.layout.add_member_dialog, null, false);
        projectModel = new ProjectModel(requireContext());
        projectId = getArguments() != null ? getArguments().getInt(ARG_PROJECT_ID, -1) : -1;
        userModel = new UserModel(requireContext());
        etSearch = root.findViewById(R.id.etSearch);
        rowPicked = root.findViewById(R.id.rowPicked);
        rvPicked  = root.findViewById(R.id.rvPicked);
        rvUsers   = root.findViewById(R.id.rvUsers);

        rvPicked.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPicked.setNestedScrollingEnabled(false); // để wrap_content mượt trong container
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));

        pickedAdapter = new AddUserDialogAdapter();
        usersAdapter = new AddUserDialogAdapter();

        rvPicked.setAdapter(pickedAdapter);
        rvUsers.setAdapter(usersAdapter);

        usersAdapter.setOnItemClick((user, position) -> {
            // Thêm vào picked
            pickedMap.put(user.getId(), user);
            pickedAdapter.submitList(new ArrayList<>(pickedMap.values()));
            pickedAdapter.notifyDataSetChanged();
            applyFilter(currentKeyword());
            // Cập nhật lại "Đã chọn"
            rowPicked.setVisibility(View.VISIBLE);
        });

        pickedAdapter.setOnItemClick((user, position) -> {
            if (user == null) return;
            if (pickedMap.remove(user.getId()) != null) {
                refreshPickedUI();              // cập nhật vùng “Đã chọn” + nút
                applyFilter(currentKeyword());  // <-- THÊM LẠI vào list users (nếu match search)
            }
        });


        // Load tất cả user có thể thêm
        userModel.getAllUser(projectId,new UserModel.GetAllUserCallBack() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onSuccess(List<UserResponse> response) {
                allUsers.clear();
                if (response != null) {
                    allUsers.addAll(response);
                }
                applyFilter(currentKeyword());
            }

            @Override
            public void onError(ResponseError error) {

            }
        });

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                applyFilter(s.toString());
            }
        });



        return new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Thêm thành viên")
                .setView(root)
                .setPositiveButton("Thêm (0)", null) // gán listener thật trong onStart()
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .create();
    }


    @Override
    public void onStart() {
        super.onStart();
        androidx.appcompat.app.AlertDialog ad = (androidx.appcompat.app.AlertDialog) getDialog();
        if (ad == null) return;

        // Nền trắng cho vùng tiêu đề (title)
        View topPanel = ad.findViewById(androidx.appcompat.R.id.topPanel);
        if (topPanel != null) {
            topPanel.setBackgroundColor(android.graphics.Color.WHITE);
        }

        // Nền trắng cho vùng nút (button bar)
        View buttonPanel = ad.findViewById(androidx.appcompat.R.id.buttonPanel);
        if (buttonPanel != null) {
            buttonPanel.setBackgroundColor(android.graphics.Color.WHITE);
        }

        // (Tuỳ chọn) đổi màu chữ nút nếu cần
        android.widget.Button pos = ad.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        android.widget.Button neg = ad.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);
        if (pos != null) pos.setTextColor(android.graphics.Color.parseColor("#1E88E5")); // xanh dương
        if (neg != null) neg.setTextColor(android.graphics.Color.parseColor("#1E88E5"));

        Button btnAdd = ad.getButton(AlertDialog.BUTTON_POSITIVE);

        btnAdd.setOnClickListener(v -> {
            if (isSubmitting) return;
            if (pickedMap.isEmpty()) return;   // CHỈ ẤN ĐƯỢC KHI có ít nhất 1 người
            isSubmitting = true;
            updatePositiveButtonTitle();       // sẽ disable nút khi đang submit

            // TODO: gọi API thêm members
            List<Integer> pickedIds = new ArrayList<>(pickedMap.keySet());
            projectModel.addMembersToProject(projectId, pickedIds, new ProjectModel.AddMembersToProjectCallBack() {
                @Override
                public void onSuccess(ResponseSuccess data) {
                    if (callback != null) callback.onAdded(pickedMap.size());
                }

                @Override
                public void onError(ResponseError error) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoading() {

                }
            });
            // projectModel.addMembers(projectId, new ArrayList<>(pickedMap.keySet()), callback...)


            // ví dụ khi xong:
            isSubmitting = false;
            if (callback != null) callback.onAdded(pickedMap.size());
            dismiss();
        });
        // ... phần gắn click listener và updatePositiveButtonTitle() của bạn giữ nguyên
    }


    private String currentKeyword() {
        CharSequence cs = etSearch.getText();
        return cs == null ? "" : cs.toString();
    }

    private void refreshPickedUI() {
        List<UserResponse> picked = new ArrayList<>(pickedMap.values());
        pickedAdapter.submitList(picked);
        rowPicked.setVisibility(picked.isEmpty() ? View.GONE : View.VISIBLE);
        updatePositiveButtonTitle();
    }

    private void applyFilter(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<UserResponse> filtered = new ArrayList<>();
        for (UserResponse u : allUsers) {
            if (pickedMap.containsKey(u.getId())) continue; // ẩn người đã chọn
            boolean match = k.isEmpty()
                    || (u.getDisplayName() != null && u.getDisplayName().toLowerCase(Locale.ROOT).contains(k))
                    || (u.getEmail() != null && u.getEmail().toLowerCase(Locale.ROOT).contains(k));
            if (match) filtered.add(u);
        }
        usersAdapter.submitList(filtered);
    }

    private void updatePositiveButtonTitle() {
        AlertDialog ad = (AlertDialog) getDialog();
        if (ad != null) {
            int n = pickedMap.size();
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setText("Thêm (" + n + ")");
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(n > 0 && !isSubmitting);
        }
    }
}
