package com.fpt.myapplication.model;

import android.content.Context;
import android.util.Log;

import com.fpt.myapplication.api.ProjectApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ProjectCreateRequest;
import com.fpt.myapplication.util.Util;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectModel {

    private ProjectApi projectApi;

    public ProjectModel(Context ct) {
        this.projectApi = ApiClient.getRetrofit(ct).create(ProjectApi.class);
    }


    public interface CreateProjectCallBack{
        void onSuccess(ResponseSuccess data);
        void onError(ResponseError error);
        void onLoading();
    }


    public void createApi (ProjectCreateRequest body, CreateProjectCallBack cb){
        cb.onLoading();
        projectApi.createProject(body).enqueue(new Callback<ResponseSuccess>() {

            @Override
            public void onResponse(Call<ResponseSuccess> call, Response<ResponseSuccess> response) {
                if(response.isSuccessful()){
                    ResponseSuccess data = response.body();
                    cb.onSuccess(data);
                }
                else{
                    ResponseError error = Util.parseError(response);
                    cb.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess> call, Throwable throwable) {
                Log.e("PROJECTAPI", "onFailure: "+throwable );
            }
        });
    }
}
