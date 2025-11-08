package com.fpt.myapplication.model;

import android.content.Context;
import android.util.Log;

import com.fpt.myapplication.api.ProjectApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ProjectCreateRequest;
import com.fpt.myapplication.dto.request.UpdateProjectRequest;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.dto.response.SearchResponse;
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

    public interface GetPublicProjectsCallBack{
        void onSuccess(List<ProjectResponse> data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface SendJoinRequestCallBack{
        void onSuccess(ResponseSuccess data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface GetJoinRequestsCallBack{
        void onSuccess(List<UserResponse> data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface HandleJoinRequestCallBack{
        void onSuccess(ResponseSuccess data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface UpdateMemberFromProjectCallBack{
        void onSuccess(ResponseSuccess data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface  RemoveMemberFromProjectCallBack{
        void onSuccess(ResponseSuccess data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface GlobalSearchCallBack{
        void onSuccess(SearchResponse data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface UpdateProjectCallBack{
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

    public void getPublicProjects (GetPublicProjectsCallBack cb){
        cb.onLoading();
        projectApi.getPublicProjects().enqueue(new Callback<ResponseSuccess<List<ProjectResponse>>>() {
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

            }
        });
    }

    public void sendJoinRequest (int projectId, SendJoinRequestCallBack cb){
        cb.onLoading();
        projectApi.sendJoinRequest(projectId).enqueue(new Callback<ResponseSuccess>() {
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

    public void getJoinRequests (int projectId, GetJoinRequestsCallBack cb){
        cb.onLoading();
        projectApi.getJoinRequests(projectId).enqueue(new Callback<ResponseSuccess<List<UserResponse>>>() {
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


    public void handleJoinRequest (int projectId, int userId, boolean accept ,HandleJoinRequestCallBack cb){
        cb.onLoading();
        projectApi.handleJoinRequest(projectId, userId, accept).enqueue(new Callback<ResponseSuccess>() {
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

    public void updateMemberFromProject (int projectId, int userId, int role ,UpdateMemberFromProjectCallBack cb){
        cb.onLoading();
        projectApi.updateMemberRole(projectId, userId, role).enqueue(new Callback<ResponseSuccess>() {
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


    public void removeMemberFromProject (int projectId, int userId ,RemoveMemberFromProjectCallBack cb){
        cb.onLoading();
        projectApi.removeMemberFromProject(projectId, userId).enqueue(new Callback<ResponseSuccess>() {
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

    public void globalSearch (String keyword ,GlobalSearchCallBack cb){
        cb.onLoading();
        projectApi.searchGlobal(keyword).enqueue(new Callback<ResponseSuccess<SearchResponse>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<SearchResponse>> call, Response<ResponseSuccess<SearchResponse>> response) {
                if(response.isSuccessful()){
                    ResponseSuccess<SearchResponse> data = response.body();
                    cb.onSuccess(data.getData());
                }
                else{
                    ResponseError error = Util.parseError(response);
                    cb.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<SearchResponse>> call, Throwable throwable) {

            }
        });
    }

    public void updateProject (int projectId, UpdateProjectRequest body, UpdateProjectCallBack cb){
        cb.onLoading();
        projectApi.updateProject(projectId, body).enqueue(new Callback<ResponseSuccess>() {
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
