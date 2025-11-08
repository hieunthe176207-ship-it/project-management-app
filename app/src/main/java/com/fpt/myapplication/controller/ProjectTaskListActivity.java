package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.TaskResponse;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.TaskModel;

import com.fpt.myapplication.view.adapter.CardTaskAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class ProjectTaskListActivity extends AppCompatActivity {

    private CardTaskAdapter adapter;
    private TaskModel model;
    private int projectId;

    // Dữ liệu toàn bộ task của project
    private final List<TaskResponse> allTasks = new ArrayList<>();

    // UI: 4 tab (giữ nguyên id như trong layout của bạn)
    private TextView tab1; // Cần làm (TODO)
    private TextView tab2; // Đang làm (IN_PROGRESS)
    private TextView tab3; // Nhận xét (IN_REVIEW)
    private TextView tab4; // Hoàn thành (DONE)

    // 0=tab1, 1=tab2, 2=tab3, 3=tab4
    private int selectedTabIndex = 0;

    // Nhãn hiển thị tab
    private static final String[] TAB_TITLES = {
            "Cần làm",     // tab1 = TODO
            "Đang làm",    // tab2 = IN_PROGRESS
            "Nhận xét",    // tab3 = IN_REVIEW
            "Hoàn thành"   // tab4 = DONE
    };

    // Mapping tab -> tên enum (khớp backend: TODO/IN_PROGRESS/IN_REVIEW/DONE)
    private static final String[] TAB_STATUS = {
            "TODO",         // tab1
            "IN_PROGRESS",  // tab2
            "IN_REVIEW",    // tab3
            "DONE"          // tab4
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_task_list);

        projectId = getIntent().getIntExtra("project_id", -1);
        model = new TaskModel(this);

        // Toolbar với nút back
        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        // RecyclerView
        RecyclerView rv = findViewById(R.id.rvProjectTasks);
        adapter = new CardTaskAdapter();
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setAdapter(adapter);

        // Tabs (id theo XML bạn đang dùng)
        tab1 = findViewById(R.id.btnProjTabTask);
        tab2 = findViewById(R.id.btnProjTabProcess);
        tab3 = findViewById(R.id.btnProjTabCompleted);
        tab4 = findViewById(R.id.btnProjTabInform);

        // Đặt text cho tabs (phòng khi text trong XML chưa đúng)
        tab1.setText(TAB_TITLES[0]);
        tab2.setText(TAB_TITLES[1]);
        tab3.setText(TAB_TITLES[2]);
        tab4.setText(TAB_TITLES[3]);

        setupPillTabs();

        // Click item mở TaskActivity
        adapter.setOnTaskClickListener(task -> {
            Intent i = new Intent(ProjectTaskListActivity.this, TaskActivity.class);
            i.putExtra("task_id", task.getId());
            i.putExtra("project_id" , projectId);
            startActivity(i);
        });


        findViewById(R.id.fabAddProjectTask).setOnClickListener(v ->{
            Intent i = new Intent(ProjectTaskListActivity.this, CreateTaskActivity.class);
            i.putExtra("project_id", projectId);
            startActivity(i);
        }

        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchData();
    }

    private void fetchData() {
        model.getAllTaskForProject(projectId, new TaskModel.GetAllTaskForProjectCallBack() {
            @Override
            public void onLoading() {
                // TODO: hiển thị loading nếu cần
            }

            @Override
            public void onSuccess(List<TaskResponse> data) {
                Log.d("ProjectTaskList", "Fetch tasks success: " + (data != null ? data.size() : 0));
                allTasks.clear();
                if (data != null) allTasks.addAll(data);
                applyFilter(selectedTabIndex);
            }

            @Override
            public void onError(ResponseError error) {
                String msg = error != null && error.message != null ? error.message : "Lỗi tải danh sách công việc";
                Toast.makeText(ProjectTaskListActivity.this, msg, Toast.LENGTH_SHORT).show();
                adapter.submitList(new ArrayList<>());
            }
        });
    }

    // ------------------------- Tabs & Filter -------------------------

    private void setupPillTabs() {
        tab1.setOnClickListener(v -> selectTab(0)); // Cần làm (TODO)
        tab2.setOnClickListener(v -> selectTab(1)); // Đang làm (IN_PROGRESS)
        tab3.setOnClickListener(v -> selectTab(2)); // Nhận xét (IN_REVIEW)
        tab4.setOnClickListener(v -> selectTab(3)); // Hoàn thành (DONE)

        // Mặc định mở Tab 1 = To Do
        selectTab(0);
    }

    private void selectTab(int index) {
        selectedTabIndex = index;

        resetTab(tab1);
        resetTab(tab2);
        resetTab(tab3);
        resetTab(tab4);

        TextView target = tab1;
        switch (index) {
            case 1: target = tab2; break;
            case 2: target = tab3; break;
            case 3: target = tab4; break;
            default: target = tab1; break;
        }
        setActiveTab(target);
        applyFilter(index);
    }

    private void resetTab(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_tab_inactive);
        tv.setTextColor(ContextCompat.getColor(this, R.color.md_primary));
    }

    private void setActiveTab(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_tab_active);
        tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void applyFilter(int pos) {
        if (allTasks.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            return;
        }

        String targetEnumName = (pos >= 0 && pos < TAB_STATUS.length) ? TAB_STATUS[pos] : null;
        if (targetEnumName == null) {
            adapter.submitList(new ArrayList<>(allTasks));
            return;
        }

        List<TaskResponse> filtered = new ArrayList<>();
        for (TaskResponse t : allTasks) {
            if (t != null && t.getStatus() != null) {
                String name = t.getStatus().name();
                if (targetEnumName.equals(name)) {
                    filtered.add(t);
                }
            }
        }
        adapter.submitList(filtered);
    }
}
