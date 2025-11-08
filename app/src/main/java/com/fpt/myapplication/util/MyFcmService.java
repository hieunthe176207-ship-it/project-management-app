package com.fpt.myapplication.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fpt.myapplication.R;
import com.fpt.myapplication.controller.MainActivity; // <- THÊM: proxy qua MainActivity
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.model.UserModel;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFcmService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "chat_channel";
    private static final String CHANNEL_NAME = "App Notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        ensureChannel();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("FCM", "New token: " + token);
        UserModel userModel = new UserModel(this);
        userModel.updateTokenFCM(token, new UserModel.UpdateTokenFCMCallBack() {
            @Override public void onLoading() {}
            @Override public void onSuccess() {}
            @Override public void onError(ResponseError error) {}
        });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        Map<String, String> data = msg.getData();
        String type = safe(data.get("type"));
        String id   = safe(data.get("id"));

        // Title/body ưu tiên notification payload, fallback về data
        String title = (msg.getNotification() != null && !TextUtils.isEmpty(msg.getNotification().getTitle()))
                ? msg.getNotification().getTitle() : safe(data.get("title"));
        String body  = (msg.getNotification() != null && !TextUtils.isEmpty(msg.getNotification().getBody()))
                ? msg.getNotification().getBody() : safe(data.get("body"));

        if (TextUtils.isEmpty(title)) title = "Thông báo";
        if (TextUtils.isEmpty(body))  body  = "Bạn có thông báo mới";


        Intent proxy = new Intent(this, MainActivity.class)
                // Action duy nhất để tránh ROM tái sử dụng pending intent cũ
                .setAction("OPEN_FROM_NOTIFICATION_" + System.currentTimeMillis())
                // NEW_TASK + CLEAR_TOP + SINGLE_TOP để chắc chắn mở/đưa lên MainActivity hiện hữu
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        proxy.putExtra("from_notification", true);
        proxy.putExtra("route_type", type);
        proxy.putExtra("route_id", id);

// Forward toàn bộ data (fallback)
        for (Map.Entry<String, String> e : data.entrySet()) {
            proxy.putExtra("data_" + e.getKey(), e.getValue());
        }

        PendingIntent pi = PendingIntent.getActivity(
                this,
                // requestCode cũng unique, tăng độ chắc chắn
                (int) System.currentTimeMillis(),
                proxy,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w("FCM", "POST_NOTIFICATIONS not granted, skip showing notification.");
            return;
        }

        NotificationManagerCompat.from(this).notify(new Random().nextInt(), b.build());
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
                );
                ch.setDescription("General and chat notifications");
                nm.createNotificationChannel(ch);
            }
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
