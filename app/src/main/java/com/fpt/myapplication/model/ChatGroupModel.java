package com.fpt.myapplication.model;

import android.content.Context;

import com.fpt.myapplication.api.ChatGroupApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.ChatGroupResponse;
import com.fpt.myapplication.util.Util;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatGroupModel {
    private ChatGroupApi chatGroupApi;

    public ChatGroupModel(Context ct) {
        this.chatGroupApi = ApiClient.getRetrofit(ct).create(ChatGroupApi.class);
    }


    public interface GetAllGroupChatCallBack{
        void onSuccess(List<ChatGroupResponse> data);
        void onError(ResponseError error);
        void onLoading();
    }



    public void getAllChatGroups(GetAllGroupChatCallBack cb) {
        cb.onLoading();
        // Implementation for fetching all chat groups will go here
        chatGroupApi.getAllChatGroups().enqueue(new Callback<ResponseSuccess<List<ChatGroupResponse>>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<List<ChatGroupResponse>>> call, Response<ResponseSuccess<List<ChatGroupResponse>>> response) {
                if (response.isSuccessful()) {
                    ResponseSuccess<List<ChatGroupResponse>> data = response.body();
                    cb.onSuccess(data.getData());
                    // Handle success
                } else {
                    ResponseError error = Util.parseError(response);
                    // Handle error
                    cb.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<List<ChatGroupResponse>>> call, Throwable t) {
                // Handle failure
            }
        });
    }
}
