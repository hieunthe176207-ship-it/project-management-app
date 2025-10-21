package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.LoginRequest;
import com.fpt.myapplication.dto.request.RegisterRequest;
import com.fpt.myapplication.dto.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {
    @Headers("No-Auth: true")
    @POST("/auth/register")
    Call<Void> register(@Body RegisterRequest body);

    @Headers("No-Auth: true")
    @POST("/auth/login")
    Call<ResponseSuccess<LoginResponse>> login(@Body LoginRequest body);
}
