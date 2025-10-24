package com.fpt.myapplication.api;



import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ProjectCreateRequest;
import com.fpt.myapplication.dto.response.ProjectResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ProjectApi {

    @POST("/project/create")
    Call<ResponseSuccess> createProject(@Body ProjectCreateRequest body);

    @GET("/project/my-projects")
    Call<ResponseSuccess<List<ProjectResponse>>> getMyProjects();

    @GET("/project/get/{id}")
    Call<ResponseSuccess<ProjectResponse>> getProjectById(@Path("id") int id);
}
