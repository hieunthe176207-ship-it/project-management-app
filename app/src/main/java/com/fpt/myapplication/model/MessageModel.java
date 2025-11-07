package com.fpt.myapplication.model;

import android.content.Context;

import com.fpt.myapplication.api.MessageApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.ChatGroupDetailResponse;
import com.fpt.myapplication.dto.response.MessageResponse;
import com.fpt.myapplication.util.Util;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageModel {
    private MessageApi messageApi;


    public MessageModel(Context ct) {
        messageApi = ApiClient.getRetrofit(ct).create(MessageApi.class);
    }


    public interface GetAllMessageCallBack{
        void onLoading();
        void onError(ResponseError error);
        void onSuccess(ChatGroupDetailResponse data);
    }

    public interface CountNewMessagesCallBack{
        void onLoading();
        void onError(ResponseError error);
        void onSuccess(int count);
    }

    public void getAllMessage(int id, GetAllMessageCallBack callBack){
        callBack.onLoading();
        messageApi.getAllMessage(id).enqueue(new Callback<ResponseSuccess<ChatGroupDetailResponse>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<ChatGroupDetailResponse>> call, Response<ResponseSuccess<ChatGroupDetailResponse>> response) {
                if(!response.isSuccessful() || response.body() == null){
                    // Handle error
                    ResponseError error = Util.parseError(response);
                    callBack.onError(error);
                }
                else{
                    ResponseSuccess<ChatGroupDetailResponse>  resSuccess = response.body();
                    callBack.onSuccess(resSuccess.getData());
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<ChatGroupDetailResponse>> call, Throwable throwable) {

            }
        });

    }

    public void countNewMessages(CountNewMessagesCallBack callBack){
        callBack.onLoading();
        messageApi.countNewMessages().enqueue(new Callback<ResponseSuccess<Integer>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<Integer>> call, Response<ResponseSuccess<Integer>> response) {
                if(!response.isSuccessful() || response.body() == null){
                    // Handle error
                    ResponseError error = Util.parseError(response);
                    callBack.onError(error);
                }
                else{
                    ResponseSuccess<Integer>  resSuccess = response.body();
                    callBack.onSuccess(resSuccess.getData());
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<Integer>> call, Throwable throwable) {

            }
        });

    }
}
