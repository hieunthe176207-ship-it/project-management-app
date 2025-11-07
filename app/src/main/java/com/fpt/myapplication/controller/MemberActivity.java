package com.fpt.myapplication.controller;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.view.adapter.MemberAdapter;
import com.fpt.myapplication.view.fragment.AddMemberDialog;
import com.fpt.myapplication.view.fragment.RolePickerDialog;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

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

        projectModel = new ProjectModel(this);
        projectId = getIntent().getIntExtra("projectId", -1);

        rvMembers = findViewById(R.id.rvMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MemberAdapter(
                getSupportFragmentManager(),
                this,
                new MemberAdapter.OnRoleListener() {
                    @Override
                    public void onRoleUpdate(int position, int newRole) {
                        // newRole: 0=Thành viên, 1=Quản lý
                        UserResponse user = adapter.getCurrentList().get(position);
                        String newRoleStr = (newRole == 1) ? "Quản lý" : "Thành viên";

                        projectModel.updateMemberFromProject(
                                projectId,
                                user.getId(),
                                newRole, // nếu BE cần 0/1 thì ok; nếu BE cần chuỗi thì map tại đây
                                new ProjectModel.UpdateMemberFromProjectCallBack() {
                                    @Override
                                    public void onSuccess(ResponseSuccess data) {
                                        user.setRole(newRoleStr);
                                        adapter.notifyItemChanged(position);
                                        new SweetAlertDialog(MemberActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText("Thông báo")
                                                .setContentText("Cập nhật thành công")
                                                .show();
                                    }
                                    @Override
                                    public void onError(ResponseError error) {
                                        new SweetAlertDialog(MemberActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText("Thất bại")
                                                .setContentText(error.message)
                                                .show();
                                    }
                                    @Override public void onLoading() {}
                                }
                        );
                    }

                    @Override
                    public void onRemoveMember(int position, UserResponse user) {
                        // giữ nguyên phần xóa của bạn
                        new SweetAlertDialog(MemberActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Xác nhận")
                                .setContentText("Bạn có chắc muốn xóa thành viên " + user.getDisplayName() + " ?")
                                .setConfirmText("Xóa")
                                .setCancelText("Hủy")
                                .setConfirmClickListener(sDialog -> {
                                    sDialog.dismissWithAnimation();
                                    projectModel.removeMemberFromProject(projectId, user.getId(), new ProjectModel.RemoveMemberFromProjectCallBack() {
                                        @Override
                                        public void onSuccess(ResponseSuccess data) {
                                            adapter.getCurrentList().remove(position);
                                            adapter.notifyItemRemoved(position);
                                            new SweetAlertDialog(MemberActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Thông báo")
                                                    .setContentText("Xóa thành công")
                                                    .show();
                                        }
                                        @Override
                                        public void onError(ResponseError error) {
                                            new SweetAlertDialog(MemberActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Thất bại")
                                                    .setContentText(error.message)
                                                    .show();
                                        }
                                        @Override public void onLoading() {}
                                    });
                                })
                                .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                                .show();
                    }
                }
        );
        rvMembers.setAdapter(adapter);

        getSupportFragmentManager().setFragmentResultListener(
                RolePickerDialog.KEY_REQUEST,
                this,
                (requestKey, bundle) -> {
                    int pickedIndex = bundle.getInt(RolePickerDialog.KEY_RESULT_ROLE, 0);
                    int position = adapter.consumePendingPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Gọi vào flow update bạn đã định nghĩa trong OnRoleListener
                        adapter.getCurrentList().get(position); // nếu cần lấy user
                        // optional: cập nhật UI tạm thời
                        adapter.notifyItemChanged(position);
                        // gọi callback để bắn API
                        adapter.getOnRoleUpdateListener().onRoleUpdate(position, pickedIndex);

                    }
                }
        );

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> openAddDialog());

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
        dialog.setOnMembersAdded(addedCount -> loadMembers());
        dialog.show(getSupportFragmentManager(), "add_member_dialog");
    }
}