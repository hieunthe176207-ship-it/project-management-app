package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.TaskResponse;
import com.fpt.myapplication.view.adapter.TaskAdapter;
import com.fpt.myapplication.view.bottomSheet.AddTaskBottomSheet;
import com.fpt.myapplication.viewmodel.ProjectTaskListViewModel;
import java.util.ArrayList;
import java.util.List;

public class ProjectTaskListActivity extends AppCompatActivity {
    private ProjectTaskListViewModel viewModel;
    private TaskAdapter adapter;
    private int projectId;
    private List<TaskResponse> allTasks = new ArrayList<>();
    private TextView btnTask, btnProcess, btnCompleted, btnInform, tvProgressCount, tvProjectDeadline;
    private ProgressBar pbTasks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_task_list);
        projectId = getIntent().getIntExtra("project_id", -1); // TODO BACKEND: Truyền từ ProjectDetailActivity
        androidx.recyclerview.widget.RecyclerView rv = findViewById(R.id.rvProjectTasks);
        adapter = new TaskAdapter();
        rv.setLayoutManager(new GridLayoutManager(this, 2)); // 2 cột theo yêu cầu UI
        rv.setAdapter(adapter);
        tvProjectDeadline = findViewById(R.id.tvProjectDeadline);
        tvProgressCount = findViewById(R.id.tvProgressCount);
        pbTasks = findViewById(R.id.pbProjectTasks);
        btnTask = findViewById(R.id.btnProjTabTask);
        btnProcess = findViewById(R.id.btnProjTabProcess);
        btnCompleted = findViewById(R.id.btnProjTabCompleted);
        btnInform = findViewById(R.id.btnProjTabInform);
        setupPillTabs();
        adapter.setOnTaskClickListener(task -> {
            // TODO BACKEND: Mở TaskActivity, truyền id để fetch chi tiết /task/{id}
            Intent i = new Intent(ProjectTaskListActivity.this, TaskActivity.class);
            i.putExtra("task_id", task.getId());
            i.putExtra("task_title", task.getTitle());
            i.putExtra("task_status", task.getStatus() != null ? task.getStatus().name() : "");
            if (task.getProject() != null) {
                i.putExtra("project_id", task.getProject().getId());
                i.putExtra("project_name", task.getProject().getName());
            }
            if (task.getCreatedBy() != null) {
                i.putExtra("owner_name", task.getCreatedBy().getDisplayName());
            }
            if (task.getAssignees() != null && !task.getAssignees().isEmpty()) {
                ArrayList<String> names = new ArrayList<>();
                for (com.fpt.myapplication.dto.response.UserResponse u : task.getAssignees()) names.add(u.getDisplayName());
                i.putExtra("assignee_names", names);
            }
            startActivity(i);
        });
        viewModel = new ViewModelProvider(this).get(ProjectTaskListViewModel.class);
        viewModel.tasks.observe(this, tasks -> {
            allTasks = tasks != null ? tasks : new java.util.ArrayList<>();
            updateProgress();
            applyFilter(getSelectedTabIndex()); // TODO BACKEND: Có thể optimize bằng LiveData transform
        });
        viewModel.error.observe(this, err -> {
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show(); // TODO BACKEND: Map lỗi cho user
            }
        });
        if (projectId != -1) viewModel.fetchForProject(projectId); // TODO BACKEND: /task/project/{projectId}
        findViewById(R.id.fabAddProjectTask).setOnClickListener(v -> {
            if (projectId == -1) return; // bảo vệ
            new AddTaskBottomSheet().setOnTaskCreated((title, desc, dueDateIso) -> {
                // TODO BACKEND: Convert dueDateIso -> đúng format BE (đang yyyy/MM/dd)
                viewModel.createTask(title, desc, dueDateIso, projectId); // /task/create
            }).show(getSupportFragmentManager(), "addProjectTask");
        });
        // TODO BACKEND: Set deadline dự án nếu có trường deadline ở ProjectResponse
        tvProjectDeadline.setText("Deadline -");
    }

    private void setupPillTabs() {
        btnTask.setOnClickListener(v -> selectTab(0));
        btnProcess.setOnClickListener(v -> selectTab(1));
        btnCompleted.setOnClickListener(v -> selectTab(2));
        btnInform.setOnClickListener(v -> selectTab(3));
        selectTab(0);
    }

    private void selectTab(int index) {
        resetTab(btnTask); resetTab(btnProcess); resetTab(btnCompleted); resetTab(btnInform);
        TextView target;
        switch (index) {
            case 0: target = btnTask; break;
            case 1: target = btnProcess; break;
            case 2: target = btnCompleted; break;
            case 3: target = btnInform; break;
            default: target = btnTask;
        }
        setActiveTab(target);
        applyFilter(index); // TODO BACKEND: Filter trên client; có thể thay bằng gọi API theo trạng thái nếu muốn
    }

    private int getSelectedTabIndex() {
        if (btnTask.getBackground().getConstantState() == getResources().getDrawable(R.drawable.bg_tab_active).getConstantState()) return 0;
        if (btnProcess.getBackground().getConstantState() == getResources().getDrawable(R.drawable.bg_tab_active).getConstantState()) return 1;
        if (btnCompleted.getBackground().getConstantState() == getResources().getDrawable(R.drawable.bg_tab_active).getConstantState()) return 2;
        if (btnInform.getBackground().getConstantState() == getResources().getDrawable(R.drawable.bg_tab_active).getConstantState()) return 3;
        return 0;
    }

    private void resetTab(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_tab_inactive);
        tv.setTextColor(getResources().getColor(R.color.md_primary));
    }

    private void setActiveTab(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_tab_active);
        tv.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void updateProgress() {
        // TODO BACKEND: Tính done dựa trên status DONE
        int total = allTasks.size();
        long done = allTasks.stream().filter(t -> t.getStatus() != null && t.getStatus().name().equals("DONE")).count();
        tvProgressCount.setText(done + "/" + total); // TODO BACKEND: Có thể đổi sang getString(R.string.progress_count, done, total)
        int percent = total == 0 ? 0 : (int) ((done * 100f) / total);
        pbTasks.setProgress(percent); // TODO BACKEND: Nếu muốn animate -> dùng ObjectAnimator
    }

    private void applyFilter(int pos) {
        if (allTasks == null) { adapter.submitList(new ArrayList<>()); return; }
        String target = null;
        switch (pos) {
            case 0: target = "TODO"; break; // TODO BACKEND: Map với TaskStatus.TODO
            case 1: target = "IN_PROGRESS"; break;
            case 2: target = "DONE"; break;
            case 3: adapter.submitList(allTasks); return; // Inform tab hiển thị tất cả
        }
        if (target == null) { adapter.submitList(allTasks); return; }
        List<TaskResponse> filtered = new ArrayList<>();
        for (TaskResponse t : allTasks) if (t.getStatus() != null && t.getStatus().name().equals(target)) filtered.add(t);
        adapter.submitList(filtered);
        updateProgress(); // TODO BACKEND: Có thể tách progress theo tab (nếu muốn)
    }
}
