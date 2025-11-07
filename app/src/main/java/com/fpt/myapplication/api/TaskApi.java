package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.CreateSubTaskRequest;
import com.fpt.myapplication.dto.request.CreateTaskRequest;
import com.fpt.myapplication.dto.response.SubTaskResponse;
import com.fpt.myapplication.dto.response.TaskDetailResponse;
import com.fpt.myapplication.dto.response.TaskResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface  TaskApi {
    @GET("task/my-tasks")
    Call<List<TaskResponse>> getMyTasks();

    @POST("task/create")
    Call<ResponseSuccess<Integer>> createTask(@Body CreateTaskRequest data);

    @GET("task/project/{projectId}")
    Call<List<TaskResponse>> getTasksByProject(@Path("projectId") int projectId);

    @GET("task/{id}")
    Call<TaskResponse> getTaskById(@Path("id") int id);

    @GET("task/all-task-for-project/{projectId}")
    Call<ResponseSuccess<List<TaskResponse>>> getAllTasksForProject(@Path("projectId") int projectId);

    @GET("/task/detail/{id}")
    Call<ResponseSuccess<TaskDetailResponse>> getTaskDetail(@Path("id") int id);

    @POST("/task/add-subtask/{id}")
    Call<ResponseSuccess<SubTaskResponse>> addSubTask(@Path("id") int id, @Body CreateSubTaskRequest data);

    @PATCH("/task/update-subtask-completed/{id}")
    Call<Void> updateSubTaskCompleted(@Path("id") int id, @Query("completed") boolean completed);
}