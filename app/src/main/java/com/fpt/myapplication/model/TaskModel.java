package com.fpt.myapplication.model;

import android.content.Context;
import android.util.Log;

import com.fpt.myapplication.api.TaskApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.request.CreateTaskRequest;
import com.fpt.myapplication.util.Util;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskModel {
    private TaskApi taskApi;


    public TaskModel(Context ct) {
        this.taskApi = ApiClient.getRetrofit(ct).create(TaskApi.class);
    }


    public interface CreateTaskCallBack{
        void onSuccess();
        void onError(ResponseError error);
        void onLoading();
    }


    public void createTask(CreateTaskRequest data, CreateTaskCallBack callBack){
        callBack.onLoading();
         taskApi.createTask(data).enqueue(new Callback<Void>() {
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
                 Log.e("CREATE_TASK", "onFailure: "+ throwable.getMessage() );
             }
         });
    }
}
