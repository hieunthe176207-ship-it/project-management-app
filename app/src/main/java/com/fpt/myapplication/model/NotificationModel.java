package com.fpt.myapplication.model;

import android.content.Context;
import android.util.Log;

import com.fpt.myapplication.api.NotificationApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.NotificationResponse;
import com.fpt.myapplication.util.Util;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationModel {
    private NotificationApi  notificationApi;

    public NotificationModel(Context context) {
        this.notificationApi = ApiClient.getRetrofit(context).create(NotificationApi.class);
    }


    public interface GetMyNotificationsCallback {
        void onSuccess(List<NotificationResponse> data);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface CountUnreadNotificationsCallback {
        void onSuccess(int count);
        void onError(ResponseError error);
        void onLoading();
    }

    public interface  MarkAllAsReadCallback {
        void onSuccess();
        void onError(ResponseError error);
        void onLoading();
    }

    public void getMyNotifications(GetMyNotificationsCallback callback) {
        callback.onLoading();
        notificationApi.getMyNotifications().enqueue(new Callback<ResponseSuccess<List<NotificationResponse>>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<List<NotificationResponse>>> call, Response<ResponseSuccess<List<NotificationResponse>>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    ResponseError error = Util.parseError(response);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<List<NotificationResponse>>> call, Throwable throwable) {
                Log.e("NOTIFY", "API call failed: " + throwable.getMessage());
            }
        });
    }

    public void countUnreadNotifications(CountUnreadNotificationsCallback callback) {
        callback.onLoading();
        notificationApi.countUnreadNotifications().enqueue(new Callback<ResponseSuccess<Integer>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<Integer>> call, Response<ResponseSuccess<Integer>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    ResponseError error = Util.parseError(response);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<Integer>> call, Throwable throwable) {
                Log.e("NOTIFY", "API call failed: " + throwable.getMessage());
            }
        });
    }

    public void markAllAsRead(MarkAllAsReadCallback callback) {
        callback.onLoading();
        notificationApi.markAllAsRead().enqueue(new Callback<ResponseSuccess<Void>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<Void>> call, Response<ResponseSuccess<Void>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess();
                } else {
                    ResponseError error = Util.parseError(response);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<Void>> call, Throwable throwable) {
                Log.e("NOTIFY", "API call failed: " + throwable.getMessage());
            }
        });
    }
}
