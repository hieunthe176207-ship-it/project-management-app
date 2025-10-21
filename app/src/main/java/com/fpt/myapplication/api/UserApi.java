package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UserApi {

    @GET("/user/account")
    Call<ResponseSuccess<UserResponse>> getAccount();
}
