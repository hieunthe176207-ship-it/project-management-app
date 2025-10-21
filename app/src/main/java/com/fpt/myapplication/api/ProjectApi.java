package com.fpt.myapplication.api;



import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ProjectCreateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ProjectApi {

    @POST("/project/create")
    Call<ResponseSuccess> createProject(@Body ProjectCreateRequest body);
}
