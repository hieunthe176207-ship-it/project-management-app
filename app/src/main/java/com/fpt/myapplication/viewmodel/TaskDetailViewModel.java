package com.fpt.myapplication.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.fpt.myapplication.api.TaskApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.response.TaskResponse;
import java.util.concurrent.atomic.AtomicBoolean;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskDetailViewModel extends AndroidViewModel {
    private final TaskApi taskApi;
    private final MutableLiveData<TaskResponse> _task = new MutableLiveData<>();
    public LiveData<TaskResponse> task = _task;
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> loading = _loading;
    private final MutableLiveData<String> _error = new MutableLiveData<>(null);
    public LiveData<String> error = _error;
    private final AtomicBoolean fetching = new AtomicBoolean(false);

    public TaskDetailViewModel(@NonNull Application application) {
        super(application);
        taskApi = ApiClient.getRetrofit(application).create(TaskApi.class);
    }

    public void fetchTask(int id) {
        if (fetching.get()) return; // tránh gọi song song
        fetching.set(true);
        _loading.postValue(true);
        _error.postValue(null);
        taskApi.getTaskById(id).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                fetching.set(false);
                _loading.postValue(false);
                if (response.isSuccessful()) {
                    _task.postValue(response.body());
                } else {
                    _error.postValue("Task load failed: " + response.code());
                    _task.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                fetching.set(false);
                _loading.postValue(false);
                _error.postValue("Network error: " + t.getMessage());
                _task.postValue(null);
            }
        });
    }
}

