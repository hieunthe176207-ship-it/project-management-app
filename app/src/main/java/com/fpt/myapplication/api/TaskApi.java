package com.fpt.myapplication.api;



import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.TaskCreateRequestDto;
import com.fpt.myapplication.dto.request.TaskUpdateStatusRequestDto;

import com.fpt.myapplication.model.KanbanBoardModel;
import com.fpt.myapplication.model.TaskModel;

import retrofit2.Call;
import retrofit2.http.*;

public interface TaskApi {

    @GET("api/tasks/kanban/{projectId}")
    Call<ResponseSuccess<KanbanBoardModel>> getKanbanBoard(@Path("projectId") Integer projectId);

    @POST("api/tasks")
    Call<ResponseSuccess<TaskModel>> createTask(@Body TaskCreateRequestDto request);

    @PUT("api/tasks/{taskId}/status")
    Call<ResponseSuccess<TaskModel>> updateTaskStatus(
            @Path("taskId") Integer taskId,
            @Body TaskUpdateStatusRequestDto request


    );

    @DELETE("api/tasks/{taskId}")
    Call<ResponseSuccess<Void>> deleteTask(@Path("taskId") Integer taskId);
}
