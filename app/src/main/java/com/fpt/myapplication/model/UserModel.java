package com.fpt.myapplication.model;

import android.content.Context;
import android.util.Log;

import com.fpt.myapplication.api.UserApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.util.Util;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserModel {
    private final String TAG = "UserApi";
    private UserApi userApi;


    public interface GetAccountCallBack{
        void onLoading();
        void onSuccess(UserResponse response);
        void onError(ResponseError error);
    }

    public UserModel(Context ctx) {
        userApi = ApiClient.getRetrofit(ctx).create(UserApi.class);
    }

    public void getAccount(GetAccountCallBack cb){
        cb.onLoading();
        userApi.getAccount().enqueue(new Callback<ResponseSuccess<UserResponse>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<UserResponse>> call, Response<ResponseSuccess<UserResponse>> response) {
                if(response.isSuccessful()){
                    ResponseSuccess<UserResponse> data = response.body();
                    cb.onSuccess(data.getData());
                }
                else{
                    ResponseError error = Util.parseError(response);
                    cb.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<UserResponse>> call, Throwable throwable) {
                Log.e(TAG, "onFailure: "+ throwable );
            }
        });
    }
}
