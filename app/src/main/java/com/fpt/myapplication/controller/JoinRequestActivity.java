package com.fpt.myapplication.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.view.adapter.JoinRequestAdapter;


import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class JoinRequestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JoinRequestAdapter adapter;
    private FrameLayout loadingOverlay;
    private ProgressBar progressBar;
    private List<UserResponse> joinRequests;

    private ProjectModel projectModel;
    private int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_join_request_layout);

        projectModel = new ProjectModel(this);
        projectId = getIntent().getIntExtra("project_id", -1);
        initViews();
        setupRecyclerView();
        loadJoinRequests();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        progressBar = findViewById(R.id.progress);
    }

    private void setupRecyclerView() {
        joinRequests = new ArrayList<>();
        adapter = new JoinRequestAdapter(joinRequests);

        adapter.setOnItemActionListener(new JoinRequestAdapter.OnItemActionListener() {
            @Override
            public void onAccept(UserResponse user, int position) {
                handleAcceptRequest(user, position);
            }

            @Override
            public void onDecline(UserResponse user, int position) {
                handleDeclineRequest(user, position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadJoinRequests() {
        projectModel.getJoinRequests(projectId, new ProjectModel.GetJoinRequestsCallBack() {
            @Override
            public void onSuccess(List<UserResponse> data) {
                adapter.updateData(data);
                showLoading(false);
            }

            @Override
            public void onError(ResponseError error) {
                showLoading(false);
            }

            @Override
            public void onLoading() {
                showLoading(true);
            }
        });
    }

    private void handleAcceptRequest(UserResponse user, int position) {
        SweetAlertDialog confirmDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Xác nhận")
                .setContentText("Bạn có chắc chắn muốn chấp nhận yêu cầu của " + user.getDisplayName() + "?")
                .setCancelText("Hủy")
                .setConfirmText("Đồng ý")
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                    projectModel.handleJoinRequest(projectId, user.getId(), true, new ProjectModel.HandleJoinRequestCallBack() {
                        @Override
                        public void onSuccess(ResponseSuccess data) {
                            showLoading(false);
                            adapter.removeItem(position);
                            SweetAlertDialog successDialog = new SweetAlertDialog(JoinRequestActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Thành công")
                                    .setContentText("Đã chấp nhận yêu cầu của " + user.getDisplayName());
                            successDialog.getProgressHelper().setBarColor(android.graphics.Color.BLACK);
                            successDialog.show();
                        }

                        @Override
                        public void onError(ResponseError error) {
                            showLoading(false);
                            SweetAlertDialog errorDialog = new SweetAlertDialog(JoinRequestActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Thông báo")
                                    .setContentText(error.message);
                            errorDialog.getProgressHelper().setBarColor(android.graphics.Color.BLACK);
                            errorDialog.show();
                        }

                        @Override
                        public void onLoading() {
                            showLoading(true);
                        }
                    });
                });
        confirmDialog.getProgressHelper().setBarColor(android.graphics.Color.BLACK);
        confirmDialog.show();
    }

    private void handleDeclineRequest(UserResponse user, int position) {
        SweetAlertDialog confirmDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Xác nhận")
                .setContentText("Bạn có chắc chắn muốn từ chối yêu cầu của " + user.getDisplayName() + "?")
                .setCancelText("Hủy")
                .setConfirmText("Từ chối")
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                    projectModel.handleJoinRequest(projectId, user.getId(), false, new ProjectModel.HandleJoinRequestCallBack() {
                        @Override
                        public void onSuccess(ResponseSuccess data) {
                            showLoading(false);
                            adapter.removeItem(position);
                            SweetAlertDialog successDialog = new SweetAlertDialog(JoinRequestActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Thành công")
                                    .setContentText("Đã từ chối yêu cầu của " + user.getDisplayName());
                            successDialog.getProgressHelper().setBarColor(android.graphics.Color.BLACK);
                            successDialog.show();
                        }

                        @Override
                        public void onError(ResponseError error) {
                            showLoading(false);
                            SweetAlertDialog errorDialog = new SweetAlertDialog(JoinRequestActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Thông báo")
                                    .setContentText(error.message);
                            errorDialog.getProgressHelper().setBarColor(android.graphics.Color.BLACK);
                            errorDialog.show();
                        }

                        @Override
                        public void onLoading() {
                            showLoading(true);
                        }
                    });
                });
        confirmDialog.getProgressHelper().setBarColor(android.graphics.Color.BLACK);
        confirmDialog.show();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
