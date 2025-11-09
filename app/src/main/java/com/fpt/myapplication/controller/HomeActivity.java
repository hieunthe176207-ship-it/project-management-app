package com.fpt.myapplication.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.fpt.myapplication.R;
import com.fpt.myapplication.config.WebSocketManager;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ProjectCreateRequest;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.MessageModel;
import com.fpt.myapplication.model.NotificationModel;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.model.UserModel;
import com.fpt.myapplication.util.OnProfileUpdated;
import com.fpt.myapplication.util.SessionPrefs;
import com.fpt.myapplication.view.bottomSheet.AddProjectBottomSheet;
import com.fpt.myapplication.view.fragment.InfomationFragment;
import com.fpt.myapplication.view.fragment.ListProjectFragment;
import com.fpt.myapplication.view.fragment.ListPublicProjectFragment;
import com.fpt.myapplication.view.fragment.MyTaskFragment;
import com.fpt.myapplication.view.fragment.NotificationFragment;
import com.fpt.myapplication.view.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

import cn.pedant.SweetAlert.SweetAlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class HomeActivity extends AppCompatActivity implements WebSocketManager.MessageListener , OnProfileUpdated {



    private ProjectModel model;
    private MessageModel messageModel;
    private MaterialCardView btnMessage;

    private MaterialCardView btnSearch;

    private MaterialCardView btnBell;

    private UserModel userModel;
    private NotificationModel notificationModel;

    private TextView badge, badgeBell, tvGreeting;

    private UserResponse user;

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        1001
                );
            }
        }
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_layout);
        user = SessionPrefs.get(this).getUser();
        userModel = new UserModel(this);
        notificationModel = new NotificationModel(this);
        messageModel = new MessageModel(this);

        ensureNotificationPermission();
        updateFcmToken();

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    "chat_channel",
                    "Chat",
                    NotificationManager.IMPORTANCE_HIGH
            );
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setBackground(null);
        tvGreeting = findViewById(R.id.tvGreeting);

        badge = findViewById(R.id.badgeMessage);
        badgeBell = findViewById(R.id.badgeBell);
        btnMessage = findViewById(R.id.btnMessage);
        btnSearch = findViewById(R.id.btnSearch);
        MaterialCardView btnCalendar = findViewById(R.id.btnCalendar);


        UserResponse user = SessionPrefs.get(this).getUser();
        model = new ProjectModel(this);
        tvGreeting.setText("Hi, "+user.getDisplayName());


        FloatingActionButton fab = findViewById(R.id.fab);

        btnMessage.setOnClickListener(
                v -> {
                    Intent intent = new Intent(HomeActivity.this, ChatGroupActivity.class);
                    startActivity(intent);
                }
        );

        btnSearch.setOnClickListener(
                v -> {
                    Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                    startActivity(intent);
                }
        );

        btnBell = findViewById(R.id.btnBell);
        btnBell.setOnClickListener(
                v -> {
                    Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                }
        );


        fab.setOnClickListener(v -> {
            new AddProjectBottomSheet()
                    .setOnProjectCreated((name, desc, due, isPublic) -> {
                        createProject(name, desc, due, isPublic);
                    })
                    .show(getSupportFragmentManager(), "addProject");
        });


        setCurrentFragment(new ListProjectFragment(), "home");
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_home){
                setCurrentFragment(new ListProjectFragment(), "home");
            }
            else if(item.getItemId() == R.id.nav_project){
                setCurrentFragment(new ListPublicProjectFragment(), "profile");
            }
            else if(item.getItemId() == R.id.nav_discussion){
                setCurrentFragment(new MyTaskFragment(), "my_task");
            }
            else{
                setCurrentFragment(new InfomationFragment(), "profile");
            }
            return true;
        });

        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, TaskCalendarActivity.class));
            });
        }



    }


    @Override
    public void onProfileUpdated(String newName) {
        tvGreeting.setText("Hi, "+newName);
    }

    private void setCurrentFragment(@NonNull Fragment fragment, @NonNull String tag) {
        // Kiểm tra fragment hiện tại
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // Nếu fragment hiện tại có cùng tag thì không cần replace (tránh reload)
        if (current != null && tag.equals(current.getTag())) {
            return;
        }

        // Gắn fragment vào container
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            // bạn có thể log hoặc hiện toast nếu muốn
            // nếu từ chối, thông báo có thể không hiện (tuỳ OEM/cài đặt)
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        WebSocketManager.get().addListener(this);
        ///topic/notify/message/1
        WebSocketManager.get().subscribeTopic("/topic/notify/message/" + user.getId());
        WebSocketManager.get().subscribeTopic("/topic/notify/" + user.getId());
        getCountMesssge();
        getCountNotifications();
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onNewMessage(String topic, String payload) {
        Log.d("SOCKET_R", "onNewMessage: "+ topic);
        if(topic.startsWith("/topic/notify/message")) {
            Log.d("HomeActivity", "onNewMessage: " + payload);
            // Cập nhật badge tin nhắn
            if(badge.getText() == null || badge.getText().toString().isEmpty()){
                runOnUiThread(() -> {
                    badge.setText("1");
                    badge.setVisibility(View.VISIBLE);
                });
            }
            else{
                int count = Integer.parseInt(badge.getText().toString());
                count += 1;
                final int finalCount = count;
                runOnUiThread(() -> {
                    badge.setText(String.valueOf(finalCount));
                    badge.setVisibility(View.VISIBLE);
                });
            }
        }
        if(topic.equals("/topic/notify/"+user.getId())) {
            Log.d("SOCKET_R", "onNewNotify: "+ topic);
            // Cập nhật badge thông báo
            if(badgeBell.getText() == null || badgeBell.getText().toString().isEmpty()){
                runOnUiThread(() -> {
                    badgeBell.setText("1");
                    badgeBell.setVisibility(View.VISIBLE);
                });
            }
            else{
                int count = Integer.parseInt(badgeBell.getText().toString());
                count += 1;
                final int finalCount = count;
                runOnUiThread(() -> {
                    badgeBell.setText(String.valueOf(finalCount));
                    badgeBell.setVisibility(View.VISIBLE);
                });
            }
        }
    }


    private void getCountMesssge() {
        badge.setText("0");
        badge.setVisibility(View.GONE);
        messageModel.countNewMessages(new MessageModel.CountNewMessagesCallBack() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onError(ResponseError error) {

            }

            @Override
            public void onSuccess(int count) {
                if(count <= 0){
                    badge.setVisibility(View.GONE);
                }
                else{
                    badge.setText(String.valueOf(count));

                    badge.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getCountNotifications() {
        badgeBell.setText("0");
        badgeBell.setVisibility(View.GONE);

        notificationModel.countUnreadNotifications(new NotificationModel.CountUnreadNotificationsCallback() {
            @Override
            public void onSuccess(int count) {
                if(count <= 0){
                    badgeBell.setVisibility(View.GONE);
                }
                else{
                    badgeBell.setText(String.valueOf(count));

                    badgeBell.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(ResponseError error) {

            }

            @Override
            public void onLoading() {

            }
        });
        // Tương tự, bạn có thể gọi model để lấy số lượng thông báo chưa đọc
        // và cập nhật badgeBell tương tự như badge ở trên
    }

    private void createProject(String name, String desc, String due, int isPublic) {
        ProjectCreateRequest body = new ProjectCreateRequest(name, desc, due, isPublic);
        model.createApi(body, new ProjectModel.CreateProjectCallBack() {
            @Override
            public void onSuccess(ResponseSuccess data) {
                Bundle result = new Bundle();
                result.putBoolean("created", true);
                getSupportFragmentManager().setFragmentResult("add_project_result", result);

                new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Thông báo")
                        .setContentText(data.getMessage())
                        .setConfirmText("ok")
                        .show();
            }

            @Override
            public void onError(ResponseError error) {
                new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Thông báo")
                        .setContentText(error.message)
                        .setConfirmText("ok")
                        .show();
            }

            @Override
            public void onLoading() {
                Toast.makeText(HomeActivity.this, "đang tải", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void updateFcmToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;
                    String token = task.getResult();
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
                });
    }
}
