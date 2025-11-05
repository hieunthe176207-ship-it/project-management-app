package com.fpt.myapplication.model;

import android.content.Context;
import android.util.Log;

import com.fpt.myapplication.api.ProjectApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ProjectCreateRequest;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.util.Util;

import java.util.List;

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

    public interface GetMyProjectsCallBack{
        void onSuccess(List<ProjectResponse> data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface GetProjectDetailCallBack{
        void onSuccess(ProjectResponse data);
        void onError(ResponseError error);
        void onLoading();
    }


    public interface GetProjectsByMemberCallBack{
        void onSuccess(List<UserResponse> data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface AddMembersToProjectCallBack{
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


    public void getMyProjects (GetMyProjectsCallBack cb){
        cb.onLoading();
        projectApi.getMyProjects().enqueue(new Callback<ResponseSuccess<List<ProjectResponse>>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<List<ProjectResponse>>> call, Response<ResponseSuccess<List<ProjectResponse>>> response) {
                if(response.isSuccessful()){
                    ResponseSuccess<List<ProjectResponse>> data = response.body();
                    cb.onSuccess(data.getData());
                }
                else{
                    ResponseError error = Util.parseError(response);
                    cb.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<List<ProjectResponse>>> call, Throwable throwable) {
                Log.e("PROJECTAPI", "onFailure: "+throwable );
            }
        });
    }

    public void getProjectById (int id, GetProjectDetailCallBack cb){
        cb.onLoading();
        projectApi.getProjectById(id).enqueue(new Callback<ResponseSuccess<ProjectResponse>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<ProjectResponse>> call, Response<ResponseSuccess<ProjectResponse>> response) {
                if(response.isSuccessful()){
                    ResponseSuccess<ProjectResponse> data = response.body();
                    cb.onSuccess(data.getData());
                }
                else{
                    ResponseError error = Util.parseError(response);
                    cb.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<ProjectResponse>> call, Throwable throwable) {
                Log.e("PROJECTAPI", "onFailure: "+throwable );
            }
        });
    }

    public void getProjectsByMemberId (int id, GetProjectsByMemberCallBack cb){
        cb.onLoading();
        projectApi.getProjectsByMemberId(id).enqueue(new Callback<ResponseSuccess<List<UserResponse>>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<List<UserResponse>>> call, Response<ResponseSuccess<List<UserResponse>>> response) {
                if(response.isSuccessful()){
                    ResponseSuccess<List<UserResponse>> data = response.body();
                    cb.onSuccess(data.getData());
                }
                else{
                    ResponseError error = Util.parseError(response);
                    cb.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<List<UserResponse>>> call, Throwable throwable) {

            }
        });
    }

    public void addMembersToProject (int projectId, List<Integer> memberIds, AddMembersToProjectCallBack cb){
        cb.onLoading();
        projectApi.addMembersToProject(projectId, memberIds).enqueue(new Callback<ResponseSuccess>() {
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

            }
        });
    }
}
