package com.fpt.myapplication.model;

import android.content.Context;
import android.util.Log;

import com.fpt.myapplication.api.AuthApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.LoginRequest;
import com.fpt.myapplication.dto.request.RegisterRequest;
import com.fpt.myapplication.dto.response.LoginResponse;
import com.fpt.myapplication.util.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthModel {

    private final AuthApi authApi;

    public AuthModel(Context ctx) {
        this.authApi = ApiClient.getRetrofit(ctx.getApplicationContext()).create(AuthApi.class);
    }

    public interface RegisterCallback {
        void onLoading();
        void onSuccess();
        void onError(ResponseError error);
    }

    public interface LoginCallBack{
        void onLoading();
        void onSuccess(LoginResponse response);
        void onError(ResponseError error);
    }

    public void register(Context ctx,
                         String displayName,
                         String email,
                         String password,
                         String confirmPassword,
                         RegisterCallback cb) {

        cb.onLoading();
        RegisterRequest body = new RegisterRequest(displayName, email, password, confirmPassword);
        authApi.register(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("AUTHAPI", "Register success, code=" + response.code());
                    cb.onSuccess();
                } else {
                    ResponseError err = Util.parseError(response);
                    Log.e("AUTHAPI", "Register HTTP error code=" + err.code + ", message=" + err.message);
                    cb.onError(err);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                ResponseError err = new ResponseError(-1, "Lỗi mạng: " + t.getMessage());
                Log.e("AUTHAPI", "Register failure", t);
                cb.onError(err);
            }
        });
    }


    public void login(String email, String password, LoginCallBack cb){
        cb.onLoading();
        LoginRequest body = new LoginRequest(email, password);
        authApi.login(body).enqueue(new Callback<ResponseSuccess<LoginResponse>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<LoginResponse>> call, Response<ResponseSuccess<LoginResponse>> response) {
                if(response.isSuccessful()){
                    ResponseSuccess<LoginResponse> loginResponse = response.body();
                    Log.d("AUTH", "onResponse: "+loginResponse.getData().getUser().getEmail() );
                    cb.onSuccess(loginResponse.getData());
                }
                else{
                    ResponseError error = Util.parseError(response);
                    cb.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<LoginResponse>> call, Throwable throwable) {

            }
        });
    }


}
