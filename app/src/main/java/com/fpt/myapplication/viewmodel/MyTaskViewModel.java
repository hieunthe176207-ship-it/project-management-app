package com.fpt.myapplication.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
// SỬA IMPORT: Dùng "AndroidViewModel"
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fpt.myapplication.api.TaskApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.response.TaskResponse;

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
}
