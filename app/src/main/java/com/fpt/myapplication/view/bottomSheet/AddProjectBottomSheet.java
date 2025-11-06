package com.fpt.myapplication.view.bottomSheet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fpt.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddProjectBottomSheet extends BottomSheetDialogFragment {

    public interface OnProjectCreated {
        void onCreated(String name, String desc, String dueDateIso, int isPublic);
    }

    @Nullable private OnProjectCreated callback;

    // Lưu lại ngày đã chọn
    @Nullable private String selectedDueDateIso = null; // dạng yyyy-MM-dd

    public AddProjectBottomSheet setOnProjectCreated(OnProjectCreated cb) {
        this.callback = cb;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View content = LayoutInflater.from(getContext())
                .inflate(R.layout.bottom_sheet_add_project, null, false);
        dialog.setContentView(content);

        // Mở full-height khi hiển thị
        content.post(() -> {
            View parent = (View) content.getParent();
            if (parent != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                // Nếu muốn cao hơn nữa khi nội dung ít, có thể ép peekHeight:
                // behavior.setPeekHeight(content.getHeight(), true);
            }
        });
        com.google.android.material.button.MaterialButtonToggleGroup tgVisibility =
                content.findViewById(R.id.tgVisibility);

        tgVisibility.check(R.id.btnPublic);
        TextInputLayout tilName = content.findViewById(R.id.tilName);
        TextInputLayout tilDesc = content.findViewById(R.id.tilDesc);
        TextInputEditText etName = content.findViewById(R.id.etName);
        TextInputEditText etDesc = content.findViewById(R.id.etDesc);
        MaterialButton btnAddDeadline = content.findViewById(R.id.btnAddDeadline);
        MaterialButton btnCreate = content.findViewById(R.id.btnCreate);

        // 1) Chọn ngày bằng MaterialDatePicker
        btnAddDeadline.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày kết thúc")
                    .setTheme(R.style.MyDatePickerTheme)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            picker.addOnPositiveButtonClickListener(sel -> {
                // Chuyển millis -> yyyy-MM-dd (UTC để khớp LocalDate ở backend)
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                String date = fmt.format(new Date(sel));
                selectedDueDateIso = date;

                // Cập nhật UI nút: hiển thị ngày đã chọn
                btnAddDeadline.setText(date);
                btnAddDeadline.setIconResource(R.drawable.ic_calendar); // dùng chung icon calendar
                // Có thể đổi màu nút nhẹ để user thấy đã chọn
                // btnAddDeadline.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0E6CFD")));
            });

            picker.show(getParentFragmentManager(), "datePicker");
        });

        // 2) Validate + tạo
        btnCreate.setOnClickListener(v -> {
            // clear lỗi cũ\
            int isPublic = (tgVisibility.getCheckedButtonId() == R.id.btnPublic) ? 1 : 0;
            tilName.setError(null);
            tilDesc.setError(null);

            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";

            boolean ok = true;

            if (name.isEmpty()) {
                tilName.setError("Tên dự án là bắt buộc");
                ok = false;
            }
            if (desc.isEmpty()) {
                tilDesc.setError("Mô tả là bắt buộc");
                ok = false;
            }
            // Nếu deadline là bắt buộc, mở khóa đoạn dưới:
            // if (selectedDueDateIso == null) {
            //     Toast.makeText(getContext(), "Vui lòng chọn Deadline", Toast.LENGTH_SHORT).show();
            //     ok = false;
            // }

            if (!ok) return;

            if (callback != null) {
                // Nếu không chọn ngày, bạn có thể cho null hoặc "" tùy backend
                String due = selectedDueDateIso != null ? selectedDueDateIso : "";
                callback.onCreated(name, desc, due, isPublic);
            }
            dismiss();
        });

        return dialog;
    }
}
