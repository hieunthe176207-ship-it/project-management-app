package com.fpt.myapplication.api;

import androidx.annotation.Nullable;

import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.UserResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserApi {

    @GET("/user/account")
    Call<ResponseSuccess<UserResponse>> getAccount();

    @Multipart
    @POST("/user/update")
    Call<ResponseSuccess<UserResponse>> updateAccount(@Part List<MultipartBody.Part> parts);
}
