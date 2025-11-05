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
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {

    @GET("/user/account")
    Call<ResponseSuccess<UserResponse>> getAccount();

    @Multipart
    @PUT("/user/update")
    Call<ResponseSuccess<UserResponse>> updateAccount(@Part List<MultipartBody.Part> parts);

    @GET("/user/get-all/{id}")
    Call<ResponseSuccess<List<UserResponse>>> getAllUser(@Path("id") int id);

    @POST("/user/update-token-fcm")
    Call<ResponseSuccess> updateTokenFCM(@Query("token") String token);
}
