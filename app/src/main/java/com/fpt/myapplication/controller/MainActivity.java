package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fpt.myapplication.R;
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

    // Timeout 15s: nếu /me quá lâu thì về Login
    private final Runnable timeout = () -> {
        if (isLoading && !navigated) {
            Toast.makeText(this, "Hết thời gian chờ. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            goLogin();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        progress = findViewById(R.id.progress);
        progressText = findViewById(R.id.progressText);

        String token = SessionPrefs.get(this).getToken();
        if (token == null || token.isEmpty()) {
            goLogin();
            return;
        }

        // Có token -> gọi /me qua UserModel
        UserModel model = new UserModel(getApplicationContext());
        model.getAccount(new UserModel.GetAccountCallBack() {
            @Override
            public void onLoading() {
                showLoading(true, "Đang tải...");
                handler.postDelayed(timeout, 15_000); // start timeout
            }

            @Override
            public void onSuccess(UserResponse response) {
                handler.removeCallbacks(timeout);
                SessionPrefs.get(MainActivity.this).saveUser(response);
                showLoading(false, null);
                goHome();
            }

            @Override
            public void onError(ResponseError error) {
                handler.removeCallbacks(timeout);
                Log.e("USERAPI", "onError: " + error.message);
                showLoading(false, null);
                // 401 sẽ bị UnauthorizedInterceptor đá về Login; các lỗi khác chủ động về Login
                goLogin();
            }
        });
    }

    private void showLoading(boolean show, @Nullable String message) {
        isLoading = show;
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        progressText.setVisibility(show ? View.VISIBLE : View.GONE);
        if (message != null && progressText != null) {
            progressText.setText(message);
        }
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
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timeout);
    }
}
