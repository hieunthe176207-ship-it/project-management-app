package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.model.AuthModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilDisplayName, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText edDisplayName, edEmail, edPassword, edConfirm;
    private AuthModel authModel;

    private MaterialButton materialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register_layout);
        authModel = new AuthModel(this);

        tilDisplayName = findViewById(R.id.tilDisplayName);
        tilEmail       = findViewById(R.id.tilEmail);
        tilPassword    = findViewById(R.id.tilPassword);
        tilConfirm     = findViewById(R.id.tilConfirm);

        edDisplayName  = findViewById(R.id.edDisplayName);
        edEmail        = findViewById(R.id.edEmail);
        edPassword     = findViewById(R.id.edPassword);
        edConfirm      = findViewById(R.id.edConfirm);

        materialButton =  findViewById(R.id.btnRegister);

        // Xoá lỗi khi người dùng gõ lại
        addClearErrorOnTextChange(tilDisplayName, edDisplayName);
        addClearErrorOnTextChange(tilEmail, edEmail);
        addClearErrorOnTextChange(tilPassword, edPassword);
        addClearErrorOnTextChange(tilConfirm, edConfirm);

        materialButton.setOnClickListener(v -> {
            if(validateForm()){
                CallRegisterApi();
            }
        });
    }


    private boolean validateForm() {
        boolean ok = true;

        String name  = getText(edDisplayName);
        String email = getText(edEmail);
        String pass  = getText(edPassword);
        String conf  = getText(edConfirm);

        if (name.isEmpty()) {
            tilDisplayName.setError("Vui lòng nhập tên hiển thị");
            ok = false;
        }

        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            ok = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            ok = false;
        }

        if (pass.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            ok = false;
        } else if (pass.length() < 6) {
            tilPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            ok = false;
        }

        if (conf.isEmpty()) {
            tilConfirm.setError("Vui lòng nhập lại mật khẩu");
            ok = false;
        } else if (!conf.equals(pass)) {
            tilConfirm.setError("Mật khẩu nhập lại không khớp");
            ok = false;
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
                til.setError(null);            // ẩn dòng lỗi
                til.setErrorEnabled(false);    // tắt trạng thái lỗi (đỡ chừa khoảng)
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void CallRegisterApi(){
        tilDisplayName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);

        String name  = getText(edDisplayName);
        String email = getText(edEmail);
        String pass  = getText(edPassword);
        String conf  = getText(edConfirm);

        authModel.register(this, name, email, pass, conf, new AuthModel.RegisterCallback() {
            @Override
            public void onLoading() {
                setLoadingButton(true);
            }

            @Override
            public void onSuccess() {
                setLoadingButton(false);
                new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Đăng ký thành công")
                        .setContentText("Tài khoản của bạn đã được tạo. Hãy đăng nhập để tiếp tục!")
                        .setConfirmText("Đăng nhập ngay")
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();

                            // Chuyển sang LoginActivity
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // kết thúc RegisterActivity để không quay lại
                        })
                        .show();
            }

            @Override
            public void onError(ResponseError error) {
                setLoadingButton(false);
                new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Thông báo")
                        .setContentText(error.message != null ? error.message : "Có lỗi xảy ra")
                        .setConfirmText("OK")
                        .show();

            }
        });
    }


    private void setLoadingButton(boolean isLoading){
        if(isLoading){
            materialButton.setText("Đang xử lý...");
            materialButton.setEnabled(false);
        }
        else{
            materialButton.setText("Đăng ký");
            materialButton.setEnabled(true);
        }
    }
}
