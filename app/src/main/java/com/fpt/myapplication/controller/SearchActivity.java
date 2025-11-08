package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.dto.response.SearchResponse;
import com.fpt.myapplication.dto.response.TaskResponseDto;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.view.adapter.ProjectAdapter;
import com.fpt.myapplication.view.adapter.SearchTaskAdapter;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class SearchActivity extends AppCompatActivity {

    // Model & Adapter
    private ProjectModel projectModel;
    private ProjectAdapter projectAdapter;
    private SearchTaskAdapter searchTaskAdapter;

    // Views
    private TextInputEditText etQuery;

    private androidx.recyclerview.widget.RecyclerView rvTaskResults;
    private androidx.recyclerview.widget.RecyclerView rvProjectResults;
    private LinearProgressIndicator progress;

    // Rx
    private final CompositeDisposable bag = new CompositeDisposable();
    private final PublishSubject<String> querySubject = PublishSubject.create();

    // In-flight API disposable
    private Disposable inFlight;

    // State
    private String keyword = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_global_layout);

        // Init model
        projectModel = new ProjectModel(this);

        // Bind views
        etQuery = findViewById(R.id.etQuery);
        rvTaskResults = findViewById(R.id.rvTaskResults);
        rvProjectResults = findViewById(R.id.rvProjectResults);
        progress = findViewById(R.id.progress);

        // Setup RecyclerViews
        rvTaskResults.setLayoutManager(new GridLayoutManager(this, 2));
        rvProjectResults.setLayoutManager(new LinearLayoutManager(this));

        // Setup adapters với click listeners
        searchTaskAdapter = new SearchTaskAdapter(task -> {
            navigateToTaskDetail(task.getId());
        });

        projectAdapter = new ProjectAdapter();
        projectAdapter.setOnItemClick(project -> {
            navigateToProjectDetail(project.getId());
        });

        rvTaskResults.setAdapter(searchTaskAdapter);
        rvProjectResults.setAdapter(projectAdapter);

        // Text change -> emit vào subject
        etQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                querySubject.onNext(s == null ? "" : s.toString());
            }
        });

        // Debounce 400ms, trim, lọc trùng
        bag.add(
                querySubject
                        .debounce(400, TimeUnit.MILLISECONDS)
                        .map(s -> s == null ? "" : s.trim())
                        .distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(q -> {
                            keyword = q;
                            if (q.isEmpty()) {
                                showProgress(false);
                                renderTasks(new ArrayList<>());
                                renderProjects(new ArrayList<>());
                            } else {
                                fetchData();
                            }
                        }, err -> {
                            showProgress(false);
                        })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bag.clear();
        if (inFlight != null && !inFlight.isDisposed()) inFlight.dispose();
    }

    /** Gọi API tìm kiếm toàn cục */
    private void fetchData() {
        showProgress(true);

        projectModel.globalSearch(keyword, new ProjectModel.GlobalSearchCallBack() {
            @Override
            public void onSuccess(SearchResponse data) {
                showProgress(false);
                if (data == null) {
                    renderTasks(new ArrayList<>());
                    renderProjects(new ArrayList<>());
                    return;
                }
                List<TaskResponseDto> tasks = data.getTasks() != null ? data.getTasks() : new ArrayList<>();
                List<ProjectResponse> projects = data.getProjects() != null ? data.getProjects() : new ArrayList<>();
                renderTasks(tasks);
                renderProjects(projects);
            }

            @Override
            public void onError(ResponseError error) {
                showProgress(false);
                renderTasks(new ArrayList<>());
                renderProjects(new ArrayList<>());
            }

            @Override
            public void onLoading() {
                showProgress(true);
            }
        });
    }

    // ---------------- Navigation ----------------

    private void navigateToTaskDetail(int taskId) {
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("task_id", taskId);
        startActivity(intent);
    }

    private void navigateToProjectDetail(int projectId) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra("project_id", projectId);
        startActivity(intent);
    }

    // ---------------- UI Helpers ----------------

    private void showProgress(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void renderTasks(List<TaskResponseDto> tasks) {
        searchTaskAdapter.submitList(tasks);
    }

    private void renderProjects(List<ProjectResponse> projects) {
        projectAdapter.submit(projects);
    }
}
