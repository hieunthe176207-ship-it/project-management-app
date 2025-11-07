package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.ChatGroupResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ChatGroupApi {

    @PATCH("/group-chat/mark-read/{groupId}")
    Call<ResponseSuccess<Void>> markGroupAsRead(@Path("groupId") int groupId);;

    @GET("/group-chat/get-all")
    Call<ResponseSuccess<List<ChatGroupResponse>>> getAllChatGroups();

    @PUT("/group-chat/update/{id}")
    @Multipart
    Call<ResponseSuccess> updateGroupChat(@Part List<MultipartBody.Part> parts, @Path("id") int id);
}
