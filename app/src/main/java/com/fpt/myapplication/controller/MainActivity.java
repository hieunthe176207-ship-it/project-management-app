package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fpt.myapplication.R;
import com.fpt.myapplication.config.WebSocketManager;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.UserModel;
import com.fpt.myapplication.util.SessionPrefs;


public class MainActivity extends AppCompatActivity {

    private ProgressBar progress;
    private TextView progressText;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isLoading = false;
    private boolean navigated = false;

    // ==== NEW: giữ deep-link từ notification ====
    @Nullable private String routeType;
    @Nullable private String routeId;

    private final Runnable timeout = () -> {
        if (isLoading && !navigated) {
            Toast.makeText(this, "Hết thời gian chờ. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            goLogin(); // vẫn forward route trong goLogin()
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        progress = findViewById(R.id.progress);
        progressText = findViewById(R.id.progressText);

        // ==== NEW: lấy route từ Intent khi mở bằng notification ====
        extractRouteFromIntent(getIntent());

        String token = SessionPrefs.get(this).getToken();
        if (token == null || token.isEmpty()) {
            goLogin(); // sẽ forward route
            return;
        }

        UserModel model = new UserModel(getApplicationContext());
        model.getAccount(new UserModel.GetAccountCallBack() {
            @Override
            public void onLoading() {
                showLoading(true, "Đang tải...");
                handler.postDelayed(timeout, 15_000);
            }

            @Override
            public void onSuccess(UserResponse response) {
                handler.removeCallbacks(timeout);
                SessionPrefs.get(MainActivity.this).saveUser(response);
                showLoading(false, null);
                // kết nối socket nếu cần
                WebSocketManager.get().connect(token);

                // ==== NEW: điều hướng theo route (nếu có), else về Home ====
                routeAfterLogin();
            }

            @Override
            public void onError(ResponseError error) {
                handler.removeCallbacks(timeout);
                showLoading(false, null);
                goLogin(); // forward route
            }
        });
    }

    // ==== NEW: nếu MainActivity là singleTop, khi tap notification lần 2 (app đang foreground) ====
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        extractRouteFromIntent(intent);
        // Nếu đã đăng nhập sẵn rồi thì route luôn:
        String token = SessionPrefs.get(this).getToken();
        if (token != null && !token.isEmpty()) {
            routeAfterLogin();
        }
    }

    // ==== NEW: đọc route ====
    private void extractRouteFromIntent(Intent intent) {
        if (intent == null) return;

        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String k : extras.keySet()) {
                Object v = extras.get(k);
                Log.d("ROUTE_EXTRAS", k + " = " + v);
            }
        } else {
            Log.d("ROUTE_EXTRAS", "no extras");
        }

        routeType = intent.getStringExtra("route_type");
        routeId   = intent.getStringExtra("route_id");

        // 1) fallback data_* (do mình forward trong proxy)
        if (TextUtils.isEmpty(routeType) && extras != null) routeType = extras.getString("data_type");
        if (TextUtils.isEmpty(routeId)   && extras != null) routeId   = extras.getString("data_id");
        if (TextUtils.isEmpty(routeId)   && extras != null) routeId   = extras.getString("data_groupId");

        // 2) fallback notification payload mở LAUNCHER: keys thường là "type"/"id"
        if (TextUtils.isEmpty(routeType)) routeType = intent.getStringExtra("type");
        if (TextUtils.isEmpty(routeId))   routeId   = intent.getStringExtra("id");

        Log.d("ROUTE", "type=" + routeType + ", id=" + routeId
                + ", from_notification=" + intent.getBooleanExtra("from_notification", false));
    }
    // ==== NEW: quyết định điều hướng sau khi /me ok ====
    private void routeAfterLogin() {
        if (navigated) return;

        if (!TextUtils.isEmpty(routeType) && !TextUtils.isEmpty(routeId)) {
            Intent target = buildTargetIntentFromRoute(routeType, routeId);
            target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            navigated = true;
            startActivity(target);
            finish();
            return;
        }
        goHome();
    }

    // ==== NEW: map route -> Activity đích ====
    private Intent buildTargetIntentFromRoute(String type, String id) {
        Intent i;
        switch (type) {
            case "REQUEST_JOIN":
            case "REQUEST_JOIN_APPROVED":
            case "PROJECT":
                i = new Intent(this, ProjectDetailActivity.class);
                putIntExtraSafe(i, "project_id", id);
                break;
            case "TASK":
                i = new Intent(this, TaskActivity.class);
                putIntExtraSafe(i, "task_id", id);
                break;
            case "MESSAGE":
                i = new Intent(this, ChatActivity.class);
                putIntExtraSafe(i, "id", id);
                break;
            default:
                i = new Intent(this, NotificationsActivity.class);
                i.putExtra("type", type);
                i.putExtra("id", id);
                break;
        }
        return i;
    }

    private void showLoading(boolean show, @Nullable String message) {
        isLoading = show;
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        progressText.setVisibility(show ? View.VISIBLE : View.GONE);
        if (message != null) progressText.setText(message);
    }

    private void goHome() {
        if (navigated) return;
        navigated = true;
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goLogin() {
        if (navigated) return;
        navigated = true;
        Intent i = new Intent(this, LoginActivity.class);
        // ==== NEW: forward route sang Login để sau login vẫn quay đúng chỗ ====
        if (!TextUtils.isEmpty(routeType)) i.putExtra("route_type", routeType);
        if (!TextUtils.isEmpty(routeId))   i.putExtra("route_id", routeId);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private static void putIntExtraSafe(Intent intent, String key, String value) {
        try {
            if (!TextUtils.isEmpty(value)) intent.putExtra(key, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            intent.putExtra(key, value);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timeout);
    }
}

