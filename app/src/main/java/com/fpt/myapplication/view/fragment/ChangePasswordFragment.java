package com.fpt.myapplication.view.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ChangePasswordRequest;
import com.fpt.myapplication.model.UserModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChangePasswordFragment extends Fragment {

    private TextInputLayout tilOld, tilNew, tilConfirm;
    private TextInputEditText etOld, etNew, etConfirm;
    private MaterialButton btnChange;
    private UserModel userModel;

    public ChangePasswordFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.change_password_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        tilOld = v.findViewById(R.id.tilOld);
        tilNew = v.findViewById(R.id.tilNew);
        tilConfirm = v.findViewById(R.id.tilConfirm);
        etOld = v.findViewById(R.id.etOld);
        etNew = v.findViewById(R.id.etNew);
        etConfirm = v.findViewById(R.id.etConfirm);
        btnChange = v.findViewById(R.id.btnChange);

        userModel = new UserModel(requireContext());

        btnChange.setOnClickListener(view -> {
            String oldPw = textOf(etOld);
            String newPw = textOf(etNew);
            String cfPw  = textOf(etConfirm);

            clearErrors();

            if (!validate(oldPw, newPw, cfPw)) return;

            btnChange.setEnabled(false);
            btnChange.setText("Đang đổi...");

            ChangePasswordRequest request = new ChangePasswordRequest(oldPw, newPw, cfPw);

            userModel.ChangePassword(request, new UserModel.ChangePasswordCallBack() {
                @Override
                public void onLoading() {
                    btnChange.setEnabled(false);
                    btnChange.setText("Đang đổi...");
                }

                @Override
                public void onSuccess() {
                    btnChange.setEnabled(true);
                    btnChange.setText("Đổi mật khẩu");

                    new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Thành công")
                            .setContentText("Đổi mật khẩu thành công")
                            .setConfirmText("OK")
                            .setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                // Clear input
                                etOld.setText("");
                                etNew.setText("");
                                etConfirm.setText("");
                            })
                            .show();
                }

                @Override
                public void onError(ResponseError error) {
                    btnChange.setEnabled(true);
                    btnChange.setText("Đổi mật khẩu");

                    new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Thất bại")
                            .setContentText(error.message)
                            .setConfirmText("OK")
                            .show();
                }
            });
        });
    }

    private void clearErrors() {
        tilOld.setError(null);
        tilNew.setError(null);
        tilConfirm.setError(null);
    }

    private boolean validate(String oldPw, String newPw, String cfPw) {
        boolean ok = true;

        if (TextUtils.isEmpty(oldPw)) {
            tilOld.setError("Vui lòng nhập mật khẩu hiện tại");
            ok = false;
        }
        if (TextUtils.isEmpty(newPw)) {
            tilNew.setError("Vui lòng nhập mật khẩu mới");
            ok = false;
        } else if (newPw.length() < 6) {
            tilNew.setError("Mật khẩu mới tối thiểu 6 ký tự");
            ok = false;
        }
        if (TextUtils.isEmpty(cfPw)) {
            tilConfirm.setError("Vui lòng nhập xác nhận mật khẩu");
            ok = false;
        } else if (!newPw.equals(cfPw)) {
            tilConfirm.setError("Xác nhận mật khẩu không khớp");
            ok = false;
        }

        return ok;
    }

    private String textOf(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
