package com.fpt.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.fpt.myapplication.dto.response.UserResponse;
import com.google.gson.Gson;

public class SessionPrefs {
    private static final String PREF = "app_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER  = "auth_user";

    private static SessionPrefs instance;
    private final SharedPreferences sp;
    private final Gson gson = new Gson();

    private SessionPrefs(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static synchronized SessionPrefs get(Context ctx) {
        if (instance == null) instance = new SessionPrefs(ctx);
        return instance;
    }

    // -------- TOKEN --------
    public void saveToken(String token) {
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    @Nullable
    public String getToken() {
        return sp.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        sp.edit().remove(KEY_TOKEN).apply();
    }

    // -------- USER (JSON) --------
    public <T> void saveUser(T user) {
        sp.edit().putString(KEY_USER, gson.toJson(user)).apply();
    }

    @Nullable
    public <T> T getUser(Class<T> clazz) {
        String json = sp.getString(KEY_USER, null);
        return (json == null) ? null : gson.fromJson(json, clazz);
    }

    public void clearUser() {
        sp.edit().remove(KEY_USER).apply();
    }

    // -------- ALL --------
    public void clearAll() {
        sp.edit().clear().apply();
    }

    @Nullable
    public UserResponse getUser() {
        String json = sp.getString(KEY_USER, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, UserResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
