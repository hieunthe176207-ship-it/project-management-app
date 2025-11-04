package com.fpt.myapplication.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fpt.myapplication.api.ProjectApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ProjectCreateRequest;
import com.fpt.myapplication.dto.response.ProjectResponse;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProjectViewModel extends AndroidViewModel {

    private ProjectApi projectApi;

    public MutableLiveData<Boolean> projectCreationSuccess = new MutableLiveData<>();

    private MutableLiveData<List<ProjectResponse>> _projectList = new MutableLiveData<>();
    public LiveData<List<ProjectResponse>> projectList = _projectList;


    public ProjectViewModel(@NonNull Application application) {
        super(application);
        this.projectApi = ApiClient.getRetrofit(application).create(ProjectApi.class);
    }

    public void createProject(ProjectCreateRequest request) {
        projectApi.createProject(request).enqueue(new Callback<ResponseSuccess>() {
            @Override
            public void onResponse(Call<ResponseSuccess> call, Response<ResponseSuccess> response) {
                if (response.isSuccessful()) {
                    projectCreationSuccess.postValue(true);
                } else {
                    projectCreationSuccess.postValue(false);
                }
            }
            @Override
            public void onFailure(Call<ResponseSuccess> call, Throwable t) {
                projectCreationSuccess.postValue(false);
            }
        });
    }

    public void fetchMyProjects() {
        projectApi.getMyProjects().enqueue(new Callback<ResponseSuccess<List<ProjectResponse>>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<List<ProjectResponse>>> call, Response<ResponseSuccess<List<ProjectResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _projectList.postValue(response.body().getData());
                } else {
                    _projectList.postValue(null);
                }
            }
            @Override
            public void onFailure(Call<ResponseSuccess<List<ProjectResponse>>> call, Throwable t) {
                _projectList.postValue(null);
            }
        });
    }
}