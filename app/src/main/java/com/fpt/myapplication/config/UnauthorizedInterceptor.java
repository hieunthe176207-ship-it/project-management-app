package com.fpt.myapplication.config;

import android.content.Context;

import androidx.annotation.NonNull;

import com.fpt.myapplication.util.LogoutManger;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class UnauthorizedInterceptor implements Interceptor {

    private final Context appCtx;

    public UnauthorizedInterceptor(Context ctx) {
        this.appCtx = ctx.getApplicationContext();
    }
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response resp = chain.proceed(chain.request());
        if (resp.code() == 401) {
            // Xóa token + đá về Login (clear task)
            LogoutManger.logout(appCtx);
        }
        return resp;
    }
}
