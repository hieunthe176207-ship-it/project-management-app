package com.fpt.myapplication.api;

import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.NotificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;

public interface NotificationApi {

    @GET("/notification/my-notifications")
    Call<ResponseSuccess<List<NotificationResponse>>> getMyNotifications();


    @GET("/notification/unread-count")
    Call<ResponseSuccess<Integer>> countUnreadNotifications();

    @PATCH("/notification/mark-all-read")
    Call<ResponseSuccess<Void>> markAllAsRead();

}
