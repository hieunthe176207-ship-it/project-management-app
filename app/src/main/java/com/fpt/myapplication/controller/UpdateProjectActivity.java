package com.fpt.myapplication.controller;

import android.os.Bundle;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fpt.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class UpdateProjectActivity extends AppCompatActivity {

    // Views
    private TextInputLayout tilName, tilDesc;
    private TextInputEditText etName, etDesc;
    private MaterialButtonToggleGroup tgVisibility;
    private MaterialButton btnAddDeadline, btnCreate;

    // Giữ deadline đã chọn (định dạng để gửi backend sau này)
    // Ở đây mình để "yyyy-MM-dd" để khớp LocalDate.toString() khi bạn cần dùng.
    private @Nullable String selectedDueDateIso = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_project_detail_layout);
        bindViews();
        setupUi();
        prefillIfAny();   // nếu muốn nhận sẵn dữ liệu qua Intent (tuỳ chọn)
    }

    private void bindViews() {
        tilName = findViewById(R.id.tilName);
        tilDesc = findViewById(R.id.tilDesc);
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        tgVisibility = findViewById(R.id.tgVisibility);
        btnAddDeadline = findViewById(R.id.btnAddDeadline);
        btnCreate = findViewById(R.id.btnCreate);
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
                SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
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

            // Chỉ hiển thị kết quả để bạn thấy luồng (không call API)
            String msg = "OK\nTên: " + name +
                    "\nMô tả: " + desc +
                    "\nCông khai: " + (isPublic ? "Có" : "Không") +
                    "\nDeadline(ISO): " + (selectedDueDateIso == null ? "(chưa chọn)" : selectedDueDateIso);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            // TODO: sau này thay bằng gọi API update hoặc setResult(...) trả về màn trước
        });
    }

    private void clearErrors() {
        tilName.setError(null);
        tilDesc.setError(null);
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /** Tuỳ chọn: tiền điền dữ liệu khi vào màn Update (qua Intent) */
    private void prefillIfAny() {
        // Ví dụ:
        // String initName = getIntent().getStringExtra("name");
        // String initDesc = getIntent().getStringExtra("desc");
        // String initDeadlineIso = getIntent().getStringExtra("deadline"); // yyyy-MM-dd
        // int initIsPublic = getIntent().getIntExtra("isPublic", 1);

        // if (initName != null) etName.setText(initName);
        // if (initDesc != null) etDesc.setText(initDesc);
        // tgVisibility.check(initIsPublic == 1 ? R.id.btnPublic : R.id.btnPrivate);
        //
        // if (initDeadlineIso != null && !initDeadlineIso.isEmpty()) {
        //     selectedDueDateIso = initDeadlineIso;
        //     // chuyển ISO -> dd/MM/yyyy để set text nút
        //     try {
        //         java.time.LocalDate d = java.time.LocalDate.parse(initDeadlineIso); // cần desugaring Java 8
        //         java.time.format.DateTimeFormatter out = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        //         btnAddDeadline.setText(d.format(out));
        //     } catch (Throwable ignored) {
        //         btnAddDeadline.setText(initDeadlineIso);
        //     }
        // }
    }
}
