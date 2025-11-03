package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.fpt.myapplication.R;
import com.fpt.myapplication.config.WebSocketManager;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.LoginResponse;
import com.fpt.myapplication.model.AuthModel;
import com.fpt.myapplication.util.SessionPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class LoginActivity extends AppCompatActivity {
    private TextInputLayout  tilEmail, tilPassword ;
    private TextInputEditText  etEmail, etPassword ;
    private MaterialButton button;
    private TextView tvRegister;
    private AuthModel authModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_layout);
        authModel = new AuthModel(this);
        tilEmail       = findViewById(R.id.tilEmail);
        tilPassword    = findViewById(R.id.tilPassword);


        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        button = findViewById(R.id.btnLogin);

        tvRegister = findViewById(R.id.tvRegister);

        addClearErrorOnTextChange(tilEmail, etEmail);
        addClearErrorOnTextChange(tilPassword, etPassword);



        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        button.setOnClickListener(v -> {
            if(validateForm()){
                String email = getText(etEmail);
                String pass  = getText(etPassword);
                authModel.login(email, pass, new AuthModel.LoginCallBack() {
                    @Override
                    public void onLoading() {

                    }

                    @Override
                    public void onSuccess(LoginResponse response) {
                        SessionPrefs sp = SessionPrefs.get(LoginActivity.this);
                        sp.saveToken(response.getToken().getAccessToken());
                        sp.saveUser(response.getUser());

                        WebSocketManager.get().connect(response.getToken().getAccessToken());
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(ResponseError error) {
                        new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Thông báo")
                                .setContentText(error.message != null ? error.message : "Có lỗi xảy ra")
                                .setConfirmText("OK")
                                .show();
                    }
                });
            }
        });
    }


    private boolean validateForm() {
        boolean ok = true;

        String email = getText(etEmail);
        String pass  = getText(etPassword);

        // Email
        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            ok = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            ok = false;
        }

        // Password
        if (pass.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            ok = false;
        } else if (pass.length() < 6) {
            tilPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            ok = false;
        }

        // Đưa focus vào ô lỗi đầu tiên (nếu có)
        if (!ok) {
            if (tilEmail.getError() != null) etEmail.requestFocus();
            else if (tilPassword.getError() != null) etPassword.requestFocus();
        }
        return ok;
    }

    private static String getText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void addClearErrorOnTextChange(TextInputLayout til, TextInputEditText et) {
        et.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                til.setError(null);
                til.setErrorEnabled(false);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

}
