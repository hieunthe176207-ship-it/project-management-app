package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.ChatGroupResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ChatGroupApi {

    @GET("/group-chat/get-all")
    Call<ResponseSuccess<List<ChatGroupResponse>>> getAllChatGroups();
}
