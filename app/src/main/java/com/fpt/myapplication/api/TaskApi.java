package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.CreateSubTaskRequest;
import com.fpt.myapplication.dto.request.CreateTaskRequest;
import com.fpt.myapplication.dto.request.UpdateTaskRequest;
import com.fpt.myapplication.dto.response.SearchResponse;
import com.fpt.myapplication.dto.response.SubTaskResponse;
import com.fpt.myapplication.dto.response.TaskDetailResponse;
import com.fpt.myapplication.dto.response.TaskResponse;


import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.TaskCreateRequestDto;
import com.fpt.myapplication.dto.request.TaskUpdateStatusRequestDto;

import com.fpt.myapplication.dto.response.TaskResponseDto;
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
    Call<ResponseSuccess<List<TaskResponseDto>>> getMyTasks();

    @POST("task/create")
    Call<ResponseSuccess<Integer>> createTask(@Body CreateTaskRequest data);
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

    @GET("task/all-task-for-project/{projectId}")
    Call<ResponseSuccess<List<TaskResponse>>> getAllTasksForProject(@Path("projectId") int projectId);

    @GET("/task/detail/{id}")
    Call<ResponseSuccess<TaskDetailResponse>> getTaskDetail(@Path("id") int id);

    @POST("/task/add-subtask/{id}")
    Call<ResponseSuccess<SubTaskResponse>> addSubTask(@Path("id") int id, @Body CreateSubTaskRequest data);

    @PATCH("/task/update-subtask-completed/{id}")
    Call<Void> updateSubTaskCompleted(@Path("id") int id, @Query("completed") boolean completed);

    @PUT("/task/update-task")
    Call<ResponseSuccess<Integer>> updateTask(@Body UpdateTaskRequest request);

    @DELETE("/task/delete-task/{id}")
    Call<Void> deleteTask(@Path("id") int id);
}