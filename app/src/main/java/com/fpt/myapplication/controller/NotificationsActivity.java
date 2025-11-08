package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.NotificationResponse;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.NotificationModel;
import com.fpt.myapplication.view.adapter.NotificationAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private androidx.recyclerview.widget.RecyclerView rv;
    private MaterialToolbar toolbar;
    private NotificationAdapter adapter;

    private NotificationModel model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_layout); // layout đầu tiên bạn gửi
        model = new NotificationModel(this);
        toolbar = findViewById(R.id.toolbar);
        rv = findViewById(R.id.rvNotifications);

        // Toolbar back
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // (Nếu dùng ActionBar):
        // setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Recycler
        adapter = new NotificationAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        adapter.setOnItemClick((item, pos) -> {
            String type = item.getType();
            switch (type) {
                case "PROJECT":
                    // Open project detail
                    startActivity(new Intent(this, ProjectDetailActivity.class)
                            .putExtra("project_id", item.getTargetId()));
                    break;
                case "TASK":
                    // Open task detail
                    Intent intent = new Intent(this, TaskActivity.class);
                    intent.putExtra("task_id", item.getTargetId());
                    startActivity(intent);
                    break;
                case "REQUEST_JOIN":
                    startActivity(new Intent(this, ProjectDetailActivity.class)
                            .putExtra("project_id", item.getTargetId()));
                    break;
                case "REQUEST_JOIN_APPROVED":
                    startActivity(new Intent(this, ProjectDetailActivity.class)
                            .putExtra("project_id", item.getTargetId()));
                    break;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchData();
        markAllNotificationsAsRead();
    }

    private void markAllNotificationsAsRead() {
        model.markAllAsRead(new NotificationModel.MarkAllAsReadCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(ResponseError error) {

            }

            @Override
            public void onLoading() {

            }
        });
    }

    private void fetchData(){
        model.getMyNotifications(new NotificationModel.GetMyNotificationsCallback() {
            @Override
            public void onSuccess(List<NotificationResponse> data) {
                adapter.submitList(data);
            }

            @Override
            public void onError(ResponseError error) {

            }

            @Override
            public void onLoading() {

            }
        });
    }

    // Nếu bạn dùng setSupportActionBar(toolbar)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    // ---------------- Mock data để xem UI ----------------

}
