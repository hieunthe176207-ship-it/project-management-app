package com.fpt.myapplication.controller;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.view.adapter.MemberAdapter;
import com.fpt.myapplication.view.fragment.AddMemberDialog;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MemberActivity extends AppCompatActivity {

    private RecyclerView rvMembers;
    private MemberAdapter adapter;

    private ProjectModel projectModel;
    private int projectId;
    private MaterialButton btnAdd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_layout);

        // Khởi tạo model gọi API
        projectModel = new ProjectModel(this);

        // Lấy projectId từ Intent; -1 nếu không có
        projectId = getIntent().getIntExtra("projectId", -1);

        // Setup RecyclerView hiển thị danh sách member
        rvMembers = findViewById(R.id.rvMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MemberAdapter();
        rvMembers.setAdapter(adapter);

        // Nút thêm mở dialog chọn user
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> openAddDialog());

        // Tải danh sách thành viên ban đầu
        loadMembers();
    }


    private void loadMembers() {
        projectModel.getProjectsByMemberId(projectId, new ProjectModel.GetProjectsByMemberCallBack() {
            @Override
            public void onSuccess(List<UserResponse> data) {
                runOnUiThread(() -> adapter.submitList(data));
            }

            @Override
            public void onError(ResponseError error) {
                // TODO: Toast/Snackbar hiển thị lỗi nếu cần
            }

            @Override
            public void onLoading() {
                // Optional: overlay loading nếu có
            }
        });
    }

    private void openAddDialog() {
        AddMemberDialog dialog = AddMemberDialog.newInstance(projectId);
        dialog.setOnMembersAdded(addedCount -> {
            // Sau khi dialog báo xong (kể cả 0), reload danh sách để sync
            loadMembers();
        });
        dialog.show(getSupportFragmentManager(), "add_member_dialog");
    }
}
