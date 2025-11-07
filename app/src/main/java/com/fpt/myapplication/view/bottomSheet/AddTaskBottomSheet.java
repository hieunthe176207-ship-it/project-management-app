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

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    public interface OnTaskCreated {
        void onCreated(String title, String desc, String dueDateIso);
    }

    @Nullable private OnTaskCreated callback;
    @Nullable private String selectedDueDateIso = null; // TODO BACKEND: Format yyyy/MM/dd hiện tại -> kiểm tra với BE

    public AddTaskBottomSheet setOnTaskCreated(OnTaskCreated cb) {
        this.callback = cb; return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View content = LayoutInflater.from(getContext())
                .inflate(R.layout.bottom_sheet_add_task, null, false);
        dialog.setContentView(content);

        content.post(() -> {
            View parent = (View) content.getParent();
            if (parent != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });

        TextInputLayout tilTitle = content.findViewById(R.id.tilTaskTitle);
        TextInputLayout tilDesc = content.findViewById(R.id.tilTaskDesc);
        TextInputEditText etTitle = content.findViewById(R.id.etTaskTitle);
        TextInputEditText etDesc = content.findViewById(R.id.etTaskDesc);
        MaterialButton btnPick = content.findViewById(R.id.btnPickDueDate);
        MaterialButton btnCreate = content.findViewById(R.id.btnCreateTask);

        btnPick.setOnClickListener(v -> {
            // TODO BACKEND: DatePicker trả về UTC millis, convert -> format BE yêu cầu (hiện đang yyyy/MM/dd)
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày hết hạn")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            picker.addOnPositiveButtonClickListener(sel -> {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd", Locale.US); // TODO BACKEND: Xác nhận pattern
                fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                selectedDueDateIso = fmt.format(new Date(sel));
                btnPick.setText(selectedDueDateIso);
                btnPick.setIconResource(R.drawable.ic_calendar);
            });
            picker.show(getParentFragmentManager(), "taskDatePicker");
        });

        btnCreate.setOnClickListener(v -> {
            tilTitle.setError(null); tilDesc.setError(null);
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
            boolean ok = true;
            if (title.isEmpty()) { tilTitle.setError("Bắt buộc"); ok = false; }
            if (desc.isEmpty()) { tilDesc.setError("Bắt buộc"); ok = false; }
            if (!ok) return;
            if (callback != null) {
                // TODO BACKEND: selectedDueDateIso có thể rỗng -> quyết định yêu cầu BE (bắt buộc hay không)
                String due = selectedDueDateIso != null ? selectedDueDateIso : "";
                callback.onCreated(title, desc, due); // Gửi lên ViewModel -> API createTask
            }
            dismiss();
        });
        return dialog;
    }
}
