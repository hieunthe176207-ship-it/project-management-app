package com.fpt.myapplication.util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.fpt.myapplication.controller.LoginActivity;

import java.util.concurrent.atomic.AtomicBoolean;

public class LogoutManger {
    private static final AtomicBoolean loggingOut = new AtomicBoolean(false);

    private LogoutManger() {}

    public static void logout(Context appCtx) {
        if (!loggingOut.compareAndSet(false, true)) return;

        // Xóa session
        SessionPrefs.get(appCtx).clearAll();

        // Về Login (xoá back stack)
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent i = new Intent(appCtx, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appCtx.startActivity(i);
            loggingOut.set(false);
        });
    }
}
