package com.fpt.myapplication.view.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.controller.ProjectDetailActivity;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.view.adapter.ProjectAdapter;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ListPublicProjectFragment extends Fragment {

    private ProjectModel model;

    private RecyclerView rv;
    private ProjectAdapter adapter;

    private ProgressBar progress;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_project_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ProjectModel(requireContext());

        rv = view.findViewById(R.id.rvItems);
        progress = view.findViewById(R.id.progress);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProjectAdapter();
        rv.setAdapter(adapter);

        adapter.setOnItemClick(p -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn muốn gửi yêu cầu tham gia vào dự án " + p.getName() + " ?")
                    .setPositiveButton("Có", (d, which) -> {
                        model.sendJoinRequest(p.getId(), new ProjectModel.SendJoinRequestCallBack() {
                            @Override
                            public void onSuccess(ResponseSuccess data) {
                                new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Thành công")
                                        .setContentText("Yêu cầu đã được gửi!")
                                        .show();
                            }

                            @Override
                            public void onError(ResponseError error) {
                                new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Thông báo")
                                        .setContentText(error.message)
                                        .show();
                            }

                            @Override
                            public void onLoading() {}
                        });
                    })
                    .setNegativeButton("Không", null)
                    .create();

            dialog.show();
            // Set background color to white
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            int textColor = getResources().getColor(android.R.color.black);
            TextView titleView = dialog.findViewById(android.R.id.title);
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (titleView != null) titleView.setTextColor(textColor);
            if (messageView != null) messageView.setTextColor(textColor);
        });
        getParentFragmentManager().setFragmentResultListener(
                "add_project_result", this, (key, bundle) -> {
                    if (bundle.getBoolean("created", false)) {
                        getData();
                    }
                }
        );
        getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    public void getData(){
        model.getPublicProjects(new ProjectModel.GetPublicProjectsCallBack() {
            @Override
            public void onSuccess(List<ProjectResponse> data) {
                progress.setVisibility(View.GONE);
                adapter.submit(data);
            }

            @Override
            public void onError(ResponseError error) {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onLoading() {
                progress.setVisibility(View.VISIBLE);
            }
        });
    }
}
