package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fpt.myapplication.R;
import com.fpt.myapplication.databinding.ActivityTaskDetailBinding;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.request.CreateSubTaskRequest;
import com.fpt.myapplication.dto.request.TaskUpdateStatusRequestDto;
import com.fpt.myapplication.dto.response.SubTaskResponse;
import com.fpt.myapplication.dto.response.TaskDetailResponse;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.TaskModel;
import com.fpt.myapplication.model.TaskStatus;
import com.fpt.myapplication.util.FileUtil;
import com.fpt.myapplication.view.adapter.SubTaskAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TaskActivity extends AppCompatActivity {

    private ActivityTaskDetailBinding binding;
    private SubTaskAdapter subTaskAdapter;
    private TaskModel model;
    private int taskId;
    private int projectId;

    private TaskDetailResponse currentTaskDetail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskId = getIntent().getIntExtra("task_id", -1);
        projectId = getIntent().getIntExtra("project_id", -1);
        model = new TaskModel(this);

        binding.toolbarProject.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.update_act) {
                Intent intent = new Intent(this, CreateTaskActivity.class);
                intent.putExtra("task_id", currentTaskDetail.getId());
                intent.putExtra("is_edit", true);
                intent.putExtra("project_id", currentTaskDetail.getProjectId());
                startActivity(intent);
                return true;
            } else if (id == R.id.to_act) {
                updateStatus(TaskStatus.TODO);
                return true;
            } else if (id == R.id.in_progess_act) {
                updateStatus(TaskStatus.IN_PROGRESS);
                return true;
            } else if (id == R.id.in_review_act) {
                updateStatus(TaskStatus.IN_REVIEW);
                return true;
            } else if (id == R.id.done_act) {
                updateStatus(TaskStatus.DONE);
                return true;
            }else if(id == R.id.delete_act){
                // Hiển thị dialog xác nhận trước khi xóa
                new SweetAlertDialog(TaskActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Xác nhận xóa")
                        .setContentText("Bạn có chắc chắn muốn xóa công việc này? Hành động này không thể hoàn tác.")
                        .setConfirmText("Xóa")
                        .setCancelText("Hủy")
                        .setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();

                            model.deleteTask(taskId, new TaskModel.DeleteTaskCallBack() {
                                @Override
                                public void onSuccess() {
                                    Intent intent = new Intent(TaskActivity.this, ProjectTaskListActivity.class);
                                    intent.putExtra("project_id", projectId);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onError(ResponseError error) {
                                    showLoading(false);
                                    String msg = (error != null && error.message != null) ? error.message : "Lỗi xóa công việc";
                                    new SweetAlertDialog(TaskActivity.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Có lỗi xảy ra")
                                            .setContentText(msg)
                                            .setConfirmText("OK")
                                            .show();
                                }

                                @Override
                                public void onLoading() {
                                    showLoading(true);
                                }
                            });
                        })
                        .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                        .show();
                return true;
            }
            return false;
        });
        // Toolbar back
        binding.toolbarProject.setNavigationOnClickListener(v -> onBackPressed());

        // RecyclerView subtasks
        subTaskAdapter = new SubTaskAdapter();
        binding.rvSubTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSubTasks.setAdapter(subTaskAdapter);

        subTaskAdapter.setOnToggleRequestListener((item, requested, previous, position, holder) -> {
            model.updateSubTaskCompleted(item.getId(), requested, new TaskModel.UpdateSubTaskCompletedCallBack() {
                @Override
                public void onSuccess() {

                    // Lấy lại vị trí hiện tại của holder để tránh vị trí cũ bị recycle
                    int pos = holder.getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;

                    // Cập nhật model + yêu cầu adapter rebind -> checkbox + gạch ngang sẽ tự đúng
                    item.setCompleted(requested);
                    subTaskAdapter.applyServerChecked(pos, requested);
                }

                @Override
                public void onError(ResponseError error) {
                    // Không đổi UI (UI đang ở previous rồi), chỉ báo lỗi
                    String msg = (error != null && error.message != null)
                            ? error.message
                            : "Lỗi cập nhật trạng thái subtask.";
                    new SweetAlertDialog(TaskActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Có lỗi xảy ra")
                            .setContentText(msg)
                            .setConfirmText("OK")
                            .show();

                    // Gắn lại listener để lần sau user bấm tiếp (phòng null)
                    holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (subTaskAdapter.getOnToggleRequestListener() == null) return;
                        int p = holder.getBindingAdapterPosition();
                        if (p == RecyclerView.NO_POSITION) return;
                        subTaskAdapter.getOnToggleRequestListener()
                                .onToggleRequest(item, isChecked, previous, p, holder);
                    });
                }

                @Override
                public void onLoading() { /* optional */ }
            });
        });

        // Nút: thêm subtask (dialog cực đơn giản)
        binding.btnAddSubtask.setOnClickListener(v -> showAddSubtaskDialog());

        // Load dữ liệu thật
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchData();
    }

    private void fetchData() {
        model.getTaskDetail(taskId, new TaskModel.GetTaskDetailCallBack() {
            @Override
            public void onLoading() {
                showLoading(true);
            }

            @Override
            public void onSuccess(TaskDetailResponse data) {
                Log.d("TASK_INF", "onSuccess: "+"data");
                currentTaskDetail = data;
                showLoading(false);
                if (data == null) {
                    return;
                }
                bindTask(data);
                bindHeaderAvatars(data.getAssignees());
            }

            @Override
            public void onError(ResponseError error) {
                showLoading(false);
                String msg = (error != null && error.message != null) ? error.message : "Lỗi tải chi tiết công việc";
                showError(msg);
            }
        });
    }

    /** Bật/tắt overlay + progress */
    private void showLoading(boolean loading) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    /** SweetAlert lỗi */
    private void showError(String msg) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Có lỗi xảy ra")
                .setContentText(msg == null ? "Vui lòng thử lại sau." : msg)
                .setConfirmText("OK")
                .show();
    }

    /** Bind dữ liệu màn hình */
    private void bindTask(TaskDetailResponse data) {
        binding.tvTitle.setText(nonNull(data.getTitle()));
        binding.tvDescription.setText(nonNull(data.getDescription()));

        // Set chipVisibility status
        if (data.getStatus() != null) {
            switch (data.getStatus()) {
                case "TODO":
                    binding.chipVisibility.setText(getString(R.string.status_todo));
                    binding.chipVisibility.setChipBackgroundColorResource(R.color.status_todo_bg);
                    binding.chipVisibility.setTextColor(getResources().getColor(R.color.status_todo_text));
                    break;
                case "IN_PROGRESS":
                    binding.chipVisibility.setText(getString(R.string.status_in_progress));
                    binding.chipVisibility.setChipBackgroundColorResource(R.color.status_in_progress_bg);
                    binding.chipVisibility.setTextColor(getResources().getColor(R.color.status_in_progress_text));
                    break;
                case "IN_REVIEW":
                    binding.chipVisibility.setText(getString(R.string.status_in_review));
                    binding.chipVisibility.setChipBackgroundColorResource(R.color.status_in_review_bg);
                    binding.chipVisibility.setTextColor(getResources().getColor(R.color.status_in_review_text));
                    break;
                case "DONE":
                    binding.chipVisibility.setText(getString(R.string.status_done));
                    binding.chipVisibility.setChipBackgroundColorResource(R.color.status_done_bg);
                    binding.chipVisibility.setTextColor(getResources().getColor(R.color.status_done_text));
                    break;
            }
        }

        binding.chipDeadline.setText(String.format(Locale.getDefault(),
                "Deadline %s", data.getDueDate() != null ? data.getDueDate() : "-"));

        List<SubTaskResponse> list = (data.getSubTasks() != null) ? data.getSubTasks() : new ArrayList<>();
        subTaskAdapter.submitAll(list);
    }

    private String nonNull(String s) { return s == null ? "" : s; }

    /** Header avatars: 4 ảnh + badge “+x” */
    private void bindHeaderAvatars(List<UserResponse> members) {
        ImageView a1 = binding.memberAvatar1;
        ImageView a2 = binding.memberAvatar2;
        ImageView a3 = binding.memberAvatar3;
        ImageView a4 = binding.memberAvatar4;

        View overlayCount = binding.overlayCount;
        android.widget.TextView tvCount = binding.membersCount;

        a1.setVisibility(View.GONE);
        a2.setVisibility(View.GONE);
        a3.setVisibility(View.GONE);
        a4.setVisibility(View.GONE);
        tvCount.setVisibility(View.GONE);
        overlayCount.setVisibility(View.GONE);

        if (members == null) return;
        int size = members.size();

        if (size >= 1) {
            a1.setVisibility(View.VISIBLE);
            loadAvatar(members.get(0).getAvatar(), a1);
        }
        if (size >= 2) {
            a2.setVisibility(View.VISIBLE);
            loadAvatar(members.get(1).getAvatar(), a2);
        }
        if (size >= 3) {
            a3.setVisibility(View.VISIBLE);
            loadAvatar(members.get(2).getAvatar(), a3);
        }
        if (size >= 4) {
            a4.setVisibility(View.VISIBLE);
            loadAvatar(members.get(3).getAvatar(), a4);

            int remain = size - 4;
            if (remain > 0) {
                tvCount.setText("+" + remain);
                overlayCount.setVisibility(View.VISIBLE);
                tvCount.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadAvatar(String url, ImageView target) {
        Glide.with(target.getContext())
                .load(FileUtil.GetImageUrl(url))
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(target);
    }

    /** Dialog cực đơn giản: TextView + EditText */
    private void showAddSubtaskDialog() {
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);

        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText("Tên subtask:");
        tv.setTextSize(16f);

        final android.widget.EditText edt = new android.widget.EditText(this);
        edt.setHint("Nhập tên subtask");
        edt.setSingleLine(true);

        container.addView(tv);
        container.addView(edt);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thêm subtask")
                .setView(container)
                .setPositiveButton("Thêm", (d, w) -> {
                    String title = edt.getText() != null ? edt.getText().toString().trim() : "";
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CreateSubTaskRequest req = new CreateSubTaskRequest();
                    req.setName(title);
                    model.addSubTask(taskId, req, new TaskModel.AddSubTaskCallBack() {
                        @Override
                        public void onSuccess(SubTaskResponse data) {
                            subTaskAdapter.addOne(data);
                            binding.rvSubTasks.smoothScrollToPosition(subTaskAdapter.getItemCount() - 1);
                        }

                        @Override
                        public void onError(ResponseError error) {
                            new SweetAlertDialog(TaskActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Lỗi thêm subtask")
                                    .setContentText(error != null && error.message != null ? error.message : "Vui lòng thử lại sau.")
                                    .setConfirmText("OK")
                                    .show();
                        }

                        @Override
                        public void onLoading() {

                        }
                    });


                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateStatus(TaskStatus status) {

        model.updateTaskStatus(taskId, new TaskUpdateStatusRequestDto(status), new TaskModel.UpdateTaskStatusCallBack() {
            @Override
            public void onSuccess() {
                Toast.makeText(TaskActivity.this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                fetchData(); // Tải lại dữ liệu
            }

            @Override
            public void onError(ResponseError error) {
                showLoading(false);
                String msg = (error != null && error.message != null) ? error.message : "Lỗi cập nhật trạng thái";
                new SweetAlertDialog(TaskActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Có lỗi xảy ra")
                        .setContentText(msg)
                        .setConfirmText("OK")
                        .show();
            }

            @Override
            public void onLoading() {
                showLoading(true);
            }
        });
    }


}
