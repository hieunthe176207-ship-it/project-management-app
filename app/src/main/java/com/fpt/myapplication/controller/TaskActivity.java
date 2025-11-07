package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.fpt.myapplication.R;
import com.fpt.myapplication.constant.TaskStatus;
import com.fpt.myapplication.databinding.ActivityTaskDetailBinding;
import com.fpt.myapplication.viewmodel.TaskDetailViewModel;

public class TaskActivity extends AppCompatActivity {
    // TODO BACKEND: Nếu sau này cần project info đầy đủ => thêm ViewModel/call project detail bằng project_id gửi qua Intent
    private ActivityTaskDetailBinding binding;
    private TaskDetailViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        viewModel = new ViewModelProvider(this).get(TaskDetailViewModel.class);
        bindFromExtras(intent); // TODO BACKEND: Hiển thị nhanh data truyền kèm từ list; sẽ bị override khi fetch chi tiết
        int taskId = intent.getIntExtra("task_id", -1);
        observeViewModel();
        if (taskId != -1) viewModel.fetchTask(taskId); // TODO BACKEND: Gọi API /task/{id}
    }

    private void bindFromExtras(Intent intent) {
        // TODO BACKEND: Tối ưu - chỉ dùng tạm khi mở màn hình, tránh màn hình trống trước khi API trả về
        String title = intent.getStringExtra("task_title");
        String status = intent.getStringExtra("task_status");
        String owner = intent.getStringExtra("owner_name");
        binding.tvTaskTitle.setText(title != null ? title : "");
        if (owner != null && !owner.isEmpty()) {
            binding.tvOwner.setText(getString(R.string.label_owner, owner));
            binding.tvOwner.setVisibility(View.VISIBLE);
        } else {
            binding.tvOwner.setVisibility(View.GONE); // Ẩn nếu chưa có owner
        }
        // TODO BACKEND: deadline & description chưa có trong TaskResponse hiện tại => chờ BE bổ sung
        binding.tvDeadlineDetail.setText(getString(R.string.label_deadline, "-"));
        binding.tvDescription.setText(getString(R.string.label_description, getString(R.string.empty_description)));
        applyStatusStyle(status); // TODO BACKEND: Convert enum string -> style
    }

    private void applyStatusStyle(String status) {
        if (status == null) return;
        try {
            TaskStatus st = TaskStatus.valueOf(status);
            switch (st) {
                case TODO:
                    binding.tvStatus.setText(getString(R.string.status_todo));
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_todo);
                    binding.tvStatus.setTextColor(getColor(R.color.status_todo_text));
                    break;
                case IN_PROGRESS:
                    binding.tvStatus.setText(getString(R.string.status_in_progress));
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_in_progress);
                    binding.tvStatus.setTextColor(getColor(R.color.status_in_progress_text));
                    break;
                case DONE:
                    binding.tvStatus.setText(getString(R.string.status_done));
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_done);
                    binding.tvStatus.setTextColor(getColor(R.color.status_done_text));
                    break;
                case IN_REVIEW:
                    binding.tvStatus.setText(getString(R.string.status_in_review));
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_in_review);
                    binding.tvStatus.setTextColor(getColor(R.color.status_in_review_text));
                    break;
            }
        } catch (IllegalArgumentException ignored) {
            // TODO BACKEND: fallback nếu status từ BE chưa nằm trong enum hiện tại
        }
    }

    private void observeViewModel() {
        // TODO BACKEND: Kết hợp loading -> hiển thị progressTask
        viewModel.error.observe(this, err -> {
            if (err != null) {
                binding.tvTaskError.setVisibility(View.VISIBLE);
                binding.tvTaskError.setText(err); // TODO BACKEND: Có thể map code -> thông điệp user-friendly
            } else {
                binding.tvTaskError.setVisibility(View.GONE);
            }
        });
        viewModel.task.observe(this, task -> {
            if (task == null) return;
            // TODO BACKEND: Sau khi nhận TaskResponse đầy đủ override dữ liệu hiển thị
            if (task.getTitle() != null) binding.tvTaskTitle.setText(task.getTitle());
            if (task.getStatus() != null) applyStatusStyle(task.getStatus().name());
            if (task.getCreatedBy() != null) {
                binding.tvOwner.setVisibility(View.VISIBLE);
                binding.tvOwner.setText(getString(R.string.label_owner, task.getCreatedBy().getDisplayName()));
                // TODO BACKEND: avatarOwner -> hiển thị initial hoặc load ảnh (nếu BE cung cấp URL)
            }
            // TODO BACKEND: deadline & description -> bind ở đây khi BE thêm trường (ví dụ task.getDueDate())
            // TODO BACKEND: assignees -> hiển thị danh sách chip nếu cần
        });
    }
}
