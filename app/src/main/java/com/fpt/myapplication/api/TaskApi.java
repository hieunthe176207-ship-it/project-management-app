package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.request.CreateTaskRequest;
import com.fpt.myapplication.dto.response.TaskResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface TaskApi {
    @GET("task/my-tasks")
    Call<List<TaskResponse>> getMyTasks();

    @POST("task/create")
    Call<Void> createTask(@Body CreateTaskRequest data);
}
