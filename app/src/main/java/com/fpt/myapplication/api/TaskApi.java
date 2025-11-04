package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.response.TaskResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface TaskApi {
    @GET("task/my-tasks")
    Call<List<TaskResponse>> getMyTasks();
}
