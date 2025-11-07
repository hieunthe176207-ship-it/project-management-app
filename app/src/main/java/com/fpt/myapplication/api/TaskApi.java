package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.request.CreateTaskRequest;
import com.fpt.myapplication.dto.response.TaskResponse;


import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.TaskCreateRequestDto;
import com.fpt.myapplication.dto.request.TaskUpdateStatusRequestDto;

import com.fpt.myapplication.dto.response.UpdateTaskReponse;
import com.fpt.myapplication.model.KanbanBoardModel;
import com.fpt.myapplication.model.TaskModel;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.*;

public interface TaskApi {
    @GET("task/my-tasks")
    Call<List<TaskResponse>> getMyTasks();

    @POST("task/create")
    Call<Void> createTask(@Body CreateTaskRequest data);
    @GET("/task/kanban/{projectId}")
    Call<ResponseSuccess<KanbanBoardModel>> getKanbanBoard(@Path("projectId") Integer projectId);

    @GET("/task/project/{projectId}")
    Call<List<TaskResponse>> getTasksByProject(@Path("projectId") int projectId);
    @PUT("/task/{taskId}/status")
    Call<ResponseSuccess<UpdateTaskReponse>> updateTaskStatus(
            @Path("taskId") Integer taskId,
            @Body TaskUpdateStatusRequestDto request);

    @GET("task/{id}")
    Call<TaskResponse> getTaskById(@Path("id") int id);



}
