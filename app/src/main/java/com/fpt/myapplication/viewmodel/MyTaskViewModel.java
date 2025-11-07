package com.fpt.myapplication.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
// SỬA IMPORT: Dùng "AndroidViewModel"
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fpt.myapplication.api.TaskApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.request.CreateTaskRequest;
import com.fpt.myapplication.dto.response.TaskResponse;

import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTaskViewModel extends AndroidViewModel {
    private MutableLiveData<List<TaskResponse>> _myTasks = new MutableLiveData<>();
    public LiveData<List<TaskResponse>> myTasks = _myTasks;
    private TaskApi taskApi;

    public MyTaskViewModel(@NonNull Application application) {
        super(application);
        this.taskApi = ApiClient.getRetrofit(application).create(TaskApi.class);
    }

    public void fetchMyTasks() {

        taskApi.getMyTasks().enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if (response.isSuccessful()) {
                    _myTasks.postValue(response.body());
                } else {
                    _myTasks.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                _myTasks.postValue(null);
            }
        });
    }

    public void createTask(String title, String desc, String dueDate, int projectId) {
        CreateTaskRequest req = new CreateTaskRequest(title, desc, dueDate, projectId, Collections.emptyList());
        taskApi.createTask(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchMyTasks(); // refresh sau khi tạo
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Có thể post một LiveData error riêng nếu cần
            }
        });
    }
}
