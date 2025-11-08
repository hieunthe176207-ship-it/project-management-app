package com.fpt.myapplication.controller;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.UpdateProjectRequest;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UpdateProjectActivity extends AppCompatActivity {

    // Views
    private TextInputLayout tilName, tilDesc;
    private TextInputEditText etName, etDesc;
    private MaterialButtonToggleGroup tgVisibility;
    private MaterialButton btnAddDeadline, btnCreate;

    private ProgressBar progress;

    // Giữ deadline đã chọn (định dạng để gửi backend sau này)
    // Ở đây mình để "yyyy-MM-dd" để khớp LocalDate.toString() khi bạn cần dùng.
    private @Nullable String selectedDueDateIso = null;

    private int projectId;

    private ProjectModel projectModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_project_detail_layout);
        projectId = getIntent().getIntExtra("project_id", -1);
        projectModel = new ProjectModel(this);
        bindViews();
        setupUi();
        fetchData();
    }


    private void fetchData() {
        showLoading(true);
        projectModel.getProjectById(projectId, new ProjectModel.GetProjectDetailCallBack() {
            @Override
            public void onSuccess(ProjectResponse data) {
                showLoading(false);
                etName.setText(data.getName());
                etDesc.setText(data.getDescription());
                if (data.getIsPublic() == 1) {
                    tgVisibility.check(R.id.btnPublic);
                } else {
                    tgVisibility.check(R.id.btnPrivate);
                }
                selectedDueDateIso = data.getDeadline();
                btnAddDeadline.setText(data.getDeadline());
            }

            @Override
            public void onError(ResponseError error) {
                showLoading(false);
                btnCreate.setEnabled(false); // disable nút cập nhật nếu load thất bại
                String msg = (error != null && error.message != null && !error.message.trim().isEmpty())
                        ? error.message
                        : "Không tải được thông tin dự án. Vui lòng thử lại.";

                new SweetAlertDialog(UpdateProjectActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Có lỗi xảy ra")
                        .setContentText(msg)
                        .setConfirmText("OK")
                        .show();
            }

            @Override
            public void onLoading() {
                showLoading(true);
            }
        });
    }


    private void showLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (btnCreate != null) btnCreate.setEnabled(!loading);
        if (btnAddDeadline != null) btnAddDeadline.setEnabled(!loading);
        if (tgVisibility != null) { // nếu có toggle-group
            for (int i = 0; i < tgVisibility.getChildCount(); i++) {
                tgVisibility.getChildAt(i).setEnabled(!loading);
            }
        }
        if (etName != null) etName.setEnabled(!loading);
        if (etDesc != null) etDesc.setEnabled(!loading);
    }

    private void bindViews() {
        tilName = findViewById(R.id.tilName);
        tilDesc = findViewById(R.id.tilDesc);
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        tgVisibility = findViewById(R.id.tgVisibility);
        btnAddDeadline = findViewById(R.id.btnAddDeadline);
        btnCreate = findViewById(R.id.btnCreate);
        progress = findViewById(R.id.progress);
    }

    private void setupUi() {
        // Mặc định chọn Public
        tgVisibility.check(R.id.btnPublic);

        // Chọn deadline bằng MaterialDatePicker (chỉ UI, không call API)
        btnAddDeadline.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày kết thúc")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    // .setTheme(R.style.MyDatePickerTheme) // nếu bạn có theme riêng
                    .build();

            picker.addOnPositiveButtonClickListener(selMillis -> {
                // Hiển thị dạng người dùng nhìn (dd/MM/yyyy)
                SimpleDateFormat uiFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                uiFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                String uiDate = uiFmt.format(new Date(selMillis));
                btnAddDeadline.setText(uiDate);

                // Lưu ISO (yyyy-MM-dd) để dùng khi gửi backend sau này
                SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                isoFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                selectedDueDateIso = isoFmt.format(new Date(selMillis));
            });

            picker.show(getSupportFragmentManager(), "datePicker");
        });

        // Nút "Cập nhật dự án" chỉ validate và báo UI (không gọi API)
        btnCreate.setOnClickListener(v -> {
            clearErrors();

            String name = safeText(etName);
            String desc = safeText(etDesc);
            boolean isPublic = tgVisibility.getCheckedButtonId() == R.id.btnPublic;

            boolean ok = true;
            if (name.isEmpty()) {
                tilName.setError("Tên dự án là bắt buộc");
                ok = false;
            }
            if (desc.isEmpty()) {
                tilDesc.setError("Mô tả là bắt buộc");
                ok = false;
            }
            // Nếu muốn bắt buộc deadline, mở comment dưới:
            // if (selectedDueDateIso == null) {
            //     Toast.makeText(this, "Vui lòng chọn hạn chót", Toast.LENGTH_SHORT).show();
            //     ok = false;
            // }

            if (!ok) return;

            UpdateProjectRequest request = new UpdateProjectRequest();
            request.setTitle(name);
            request.setDescription(desc);
            request.setIsPublic(isPublic ? 1 : 0);
            request.setDeadline(selectedDueDateIso);
             projectModel.updateProject(projectId, request, new ProjectModel.UpdateProjectCallBack() {
              @Override
              public void onSuccess(ResponseSuccess data) {
                  showLoading(false);
                  Toast.makeText(UpdateProjectActivity.this, "Cập nhật dự án thành công", Toast.LENGTH_SHORT).show();
                  setResult(RESULT_OK);
                  finish();

              }

              @Override
              public void onError(ResponseError error) {
                  showLoading(false);
                  SweetAlertDialog dialog = new SweetAlertDialog(UpdateProjectActivity.this, SweetAlertDialog.ERROR_TYPE)
                          .setTitleText("Có lỗi xảy ra")
                          .setContentText(error != null && error.message != null ? error.message : "Cập nhật thất bại.")
                          .setConfirmText("OK");
                  dialog.show();
              }

              @Override
              public void onLoading() {
                  showLoading(true);
              }
          });
        }
        );
    }

    private void clearErrors() {
        tilName.setError(null);
        tilDesc.setError(null);
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /** Tuỳ chọn: tiền điền dữ liệu khi vào màn Update (qua Intent) */
}
