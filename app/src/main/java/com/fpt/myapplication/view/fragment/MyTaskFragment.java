package com.fpt.myapplication.view.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fpt.myapplication.R;
import com.fpt.myapplication.controller.TaskActivity;
import com.fpt.myapplication.dto.response.TaskResponse;
import com.fpt.myapplication.view.adapter.TaskAdapter;
import com.fpt.myapplication.view.bottomSheet.AddTaskBottomSheet;
import com.fpt.myapplication.viewmodel.MyTaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class MyTaskFragment extends Fragment {

    private TextView btnTask, btnProcess, btnCompleted, btnInform, tvProgressCount, tvProjectDeadline;
    private ProgressBar pbTasks;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
    private MyTaskViewModel myTaskViewModel;
    private FloatingActionButton fabAdd;

    public MyTaskFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Dùng chung layout với ProjectTaskListActivity để tránh trùng lặp
        return inflater.inflate(R.layout.activity_project_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvProjectDeadline = view.findViewById(R.id.tvProjectDeadline);
        tvProgressCount = view.findViewById(R.id.tvProgressCount);
        pbTasks = view.findViewById(R.id.pbProjectTasks);
        btnTask = view.findViewById(R.id.btnProjTabTask);
        btnProcess = view.findViewById(R.id.btnProjTabProcess);
        btnCompleted = view.findViewById(R.id.btnProjTabCompleted);
        btnInform = view.findViewById(R.id.btnProjTabInform);
        rvTasks = view.findViewById(R.id.rvProjectTasks);
        fabAdd = view.findViewById(R.id.fabAddProjectTask);
        setupPillTabs();
        setupRecycler();
        setupViewModel();
        setupFab();
        tvProjectDeadline.setVisibility(View.GONE); // chưa có deadline thật
    }

    private void setupPillTabs() {
        btnTask.setOnClickListener(v -> selectTab(0));
        btnProcess.setOnClickListener(v -> selectTab(1));
        btnCompleted.setOnClickListener(v -> selectTab(2));
        btnInform.setOnClickListener(v -> selectTab(3));
        selectTab(0);
    }

    private void selectTab(int index) {
        // Reset all
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
        filterTasks(index);
    }

    private void resetTab(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_tab_inactive);
        tv.setTextColor(getResources().getColor(R.color.md_primary));
    }

    private void setActiveTab(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_tab_active);
        tv.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void setupRecycler() {
        rvTasks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        taskAdapter = new TaskAdapter();
        rvTasks.setAdapter(taskAdapter);
        taskAdapter.setOnTaskClickListener(task -> {
            if (getContext() != null) {
                Intent i = new Intent(getContext(), TaskActivity.class);
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
            }
        });
    }

    private void setupViewModel() {
        myTaskViewModel = new ViewModelProvider(this).get(MyTaskViewModel.class);
        myTaskViewModel.myTasks.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks == null) {
                taskAdapter.submitList(new ArrayList<>());
                tvProgressCount.setText(getString(R.string.progress_format, 0, 0));
                return;
            }
            taskAdapter.submitList(tasks);
            updateProgress(tasks);
        });
        myTaskViewModel.fetchMyTasks();
    }

    private void updateProgress(List<TaskResponse> tasks) {
        int total = tasks.size();
        long done = tasks.stream().filter(t -> t.getStatus() != null && t.getStatus().name().equals("DONE")).count();
        tvProgressCount.setText(done + "/" + total);
        int percent = total == 0 ? 0 : (int) ((done * 100f) / total);
        pbTasks.setProgress(percent);
    }

    private void filterTasks(int tabIndex) {
        List<TaskResponse> current = taskAdapter.getCurrentList();
        if (current == null) return;
        String targetStatus;
        switch (tabIndex) {
            case 0: targetStatus = "TODO"; break;
            case 1: targetStatus = "IN_PROGRESS"; break;
            case 2: targetStatus = "DONE"; break;
            case 3: taskAdapter.submitList(current); return; // Inform
            default: targetStatus = null;
        }
        if (targetStatus == null) { taskAdapter.submitList(current); return; }
        List<TaskResponse> filtered = new ArrayList<>();
        for (TaskResponse t : current) {
            if (t.getStatus() != null && t.getStatus().name().equals(targetStatus)) { filtered.add(t); }
        }
        taskAdapter.submitList(filtered);
        updateProgress(current); // progress vẫn theo toàn bộ
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> {
            new AddTaskBottomSheet()
                    .setOnTaskCreated((title, desc, dueDateIso) -> {
                        // Giả sử tạo task cá nhân không cần projectId (hoặc projectId=-1 nếu backend yêu cầu)
                        myTaskViewModel.createTask(title, desc, dueDateIso, -1);
                    })
                    .show(getParentFragmentManager(), "addTask");
        });
    }
}
