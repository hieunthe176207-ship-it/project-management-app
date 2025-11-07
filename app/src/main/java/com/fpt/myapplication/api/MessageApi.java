package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.ChatGroupDetailResponse;
import com.fpt.myapplication.dto.response.MessageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MessageApi {
    @GET("/messages/get-all/{id}")
    Call<ResponseSuccess<ChatGroupDetailResponse>> getAllMessage(@Path("id") int id);

    @GET("/messages/count-new-messsages" )
    Call<ResponseSuccess<Integer>> countNewMessages();
 }
