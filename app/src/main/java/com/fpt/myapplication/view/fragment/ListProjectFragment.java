package com.fpt.myapplication.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.controller.ProjectDetailActivity;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.view.adapter.ProjectAdapter;

import java.util.List;

public class ListProjectFragment extends Fragment {

    private ProjectModel model;

    private RecyclerView rv;
    private ProjectAdapter adapter;

    private ProgressBar progress;

    public ListProjectFragment() {
    }

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
            Intent intent = new Intent(requireContext(), ProjectDetailActivity.class);
            intent.putExtra("project_id", p.getId());
            startActivity(intent);
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
        model.getMyProjects(new ProjectModel.GetMyProjectsCallBack() {
            @Override
            public void onSuccess(List<ProjectResponse> data) {
                progress.setVisibility(View.GONE);
                adapter.submit(data);
            }

            @Override
            public void onError(ResponseError error) {
                progress.setVisibility(View.GONE);
                Log.e("PROJECT", "onError: "+ error );
            }

            @Override
            public void onLoading() {
                progress.setVisibility(View.VISIBLE);
            }
        });
    }
}
