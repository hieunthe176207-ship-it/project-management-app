package com.fpt.myapplication.model;

import android.content.Context;
import android.util.Log;

import com.fpt.myapplication.api.TaskApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.CreateSubTaskRequest;
import com.fpt.myapplication.dto.request.CreateTaskRequest;
import com.fpt.myapplication.dto.response.SubTaskResponse;
import com.fpt.myapplication.dto.response.TaskDetailResponse;
import com.fpt.myapplication.dto.response.TaskResponse;
import com.fpt.myapplication.util.Util;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskModel {
    private TaskApi taskApi;


    public TaskModel(Context ct) {
        this.taskApi = ApiClient.getRetrofit(ct).create(TaskApi.class);
    }


    public interface CreateTaskCallBack{
        void onSuccess(Integer id);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface GetAllTaskForProjectCallBack{
        void onSuccess(List<TaskResponse> data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface GetTaskDetailCallBack{
        void onSuccess(TaskDetailResponse data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface AddSubTaskCallBack{
        void onSuccess(SubTaskResponse data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface UpdateSubTaskCompletedCallBack{
        void onSuccess();
        void onError(ResponseError error);
        void onLoading();
    }


    public void createTask(CreateTaskRequest data, CreateTaskCallBack callBack){
        callBack.onLoading();
        taskApi.createTask(data).enqueue(new Callback<ResponseSuccess<Integer>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<Integer>> call, Response<ResponseSuccess<Integer>> response) {
                if(response.isSuccessful()){
                    Log.d("CREATE", "onResponse:  "+ response.body());
                    Integer id = response.body().getData();
                    Log.d("CREATE", "onResponse:  "+ id);
                    callBack.onSuccess(id);
                } else {
                    ResponseError error = Util.parseError(response);
                    callBack.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<Integer>> call, Throwable throwable) {
                Log.e("CREATE_TASK", "onFailure: "+ throwable.getMessage() );
            }
        });
    }

    public void getAllTaskForProject(int projectId, GetAllTaskForProjectCallBack callBack) {
        callBack.onLoading();
       taskApi.getAllTasksForProject(projectId).enqueue(new Callback<ResponseSuccess<List<TaskResponse>>>() {
           @Override
           public void onResponse(Call<ResponseSuccess<List<TaskResponse>>> call, Response<ResponseSuccess<List<TaskResponse>>> response) {
                if(response.isSuccessful()){
                     List<TaskResponse> data = response.body().getData();
                     callBack.onSuccess(data);
                } else {
                     ResponseError error = Util.parseError(response);
                     callBack.onError(error);
                }
           }

           @Override
           public void onFailure(Call<ResponseSuccess<List<TaskResponse>>> call, Throwable throwable) {
                Log.e("GET_ALL_TASK", "onFailure: "+ throwable.getMessage() );
           }
       });
    }

    public void getTaskDetail(int taskId, GetTaskDetailCallBack callBack) {
        callBack.onLoading();
        taskApi.getTaskDetail(taskId).enqueue(new Callback<ResponseSuccess<TaskDetailResponse>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<TaskDetailResponse>> call, Response<ResponseSuccess<TaskDetailResponse>> response) {
                if(response.isSuccessful()){
                    TaskDetailResponse data = response.body().getData();
                    callBack.onSuccess(data);
                } else {
                    ResponseError error = Util.parseError(response);
                    callBack.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<TaskDetailResponse>> call, Throwable throwable) {
                Log.e("GET_TASK_DETAIL", "onFailure: "+ throwable.getMessage() );
            }
        });
    }

    public void addSubTask(int taskId, CreateSubTaskRequest data, AddSubTaskCallBack callBack) {
        callBack.onLoading();
        taskApi.addSubTask(taskId, data).enqueue(new Callback<ResponseSuccess<SubTaskResponse>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<SubTaskResponse>> call, Response<ResponseSuccess<SubTaskResponse>> response) {
                if(response.isSuccessful()){
                    ResponseSuccess <SubTaskResponse> res = response.body();
                    callBack.onSuccess(res.getData());
                } else {
                    ResponseError error = Util.parseError(response);
                    callBack.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<com.fpt.myapplication.dto.response.SubTaskResponse>> call, Throwable throwable) {
                Log.e("ADD_SUB_TASK", "onFailure: "+ throwable.getMessage() );
            }
        });
    }

    public void updateSubTaskCompleted(int subTaskId, boolean completed, UpdateSubTaskCompletedCallBack callBack) {
        callBack.onLoading();
        taskApi.updateSubTaskCompleted(subTaskId, completed).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){
                    callBack.onSuccess();
                } else {
                    ResponseError error = Util.parseError(response);
                    callBack.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                Log.e("UPDATE_SUB_TASK", "onFailure: "+ throwable.getMessage() );
            }
        });
    }
}
