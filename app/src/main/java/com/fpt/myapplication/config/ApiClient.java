// com.fpt.myapplication.config.ApiClient
package com.fpt.myapplication.config;

import android.content.Context;



import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static volatile Retrofit retrofit;
    private static final Object LOCK = new Object();

    private ApiClient() {}

    // ✅ Gọi: ApiClient.getRetrofit(getApplicationContext())
    public static Retrofit getRetrofit(Context ctx) {
        if (retrofit == null) {
            synchronized (LOCK) {
                if (retrofit == null) {
                    Context appCtx = ctx.getApplicationContext(); // dùng app context

                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new AuthHeaderInterceptor(appCtx))     // gắn Bearer token
                            .addInterceptor(new UnauthorizedInterceptor(appCtx))   // bắt 401 -> logout
                            .addInterceptor(log)
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl("http://10.0.2.2:8080/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
                }
            }
        }
        return retrofit;
    }
}
