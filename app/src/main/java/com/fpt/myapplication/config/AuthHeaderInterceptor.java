package com.fpt.myapplication.config;

import android.content.Context;

import androidx.annotation.NonNull;

import com.fpt.myapplication.util.SessionPrefs;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthHeaderInterceptor implements Interceptor {

    private final Context appCtx;

    public AuthHeaderInterceptor(Context ctx) {
        this.appCtx = ctx.getApplicationContext();
    }

    @NonNull
    @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request req = chain.request();

            // Bỏ qua nếu request đánh dấu "No-Auth"
            if (req.header("No-Auth") != null) {
                return chain.proceed(req.newBuilder().removeHeader("No-Auth").build());
            }

            String token = SessionPrefs.get(appCtx).getToken();
            if (token == null || token.isEmpty()) {
                return chain.proceed(req);
            }

            Request withAuth = req.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(withAuth);
        }
    }

