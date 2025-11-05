package com.fpt.myapplication.util;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fpt.myapplication.R;
import com.fpt.myapplication.controller.ChatActivity;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.model.UserModel;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFcmService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        // Token có thể đổi bất kỳ lúc nào (install lại app, clear data, rotate…)
        Log.d("FCM", "New token: " + token);

        // (Tuỳ chọn) lưu local để dùng lại
        UserModel userModel = new UserModel(this);
        userModel.updateTokenFCM(token, new UserModel.UpdateTokenFCMCallBack() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(ResponseError error) {

            }
        });

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        // Nếu server gửi "notification" payload: msg.getNotification() sẽ có title/body.
        // Nếu server gửi "data-only": lấy từ msg.getData().
        String title = (msg.getNotification() != null && msg.getNotification().getTitle() != null)
                ? msg.getNotification().getTitle()
                : msg.getData().get("title");

        String body  = (msg.getNotification() != null && msg.getNotification().getBody() != null)
                ? msg.getNotification().getBody()
                : msg.getData().get("body");

        // Intent mở app/màn hình mong muốn khi bấm thông báo
        Intent intent = new Intent(this, ChatActivity.class); // TODO: thay Activity của bạn

        Log.d("FCM", "onMessageReceived: "+ msg.getData().get("groupId"));
        // Ví dụ mở thẳng phòng chat:
        intent.putExtra("id",msg.getData().get("groupId"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, "chat_channel")
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title != null ? title : "Tin nhắn mới")
                .setContentText(body != null ? body : "Bạn có thông báo")
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        NotificationManagerCompat.from(this).notify(new Random().nextInt(), b.build());
    }
}
