package com.fpt.myapplication.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.request.CreateTaskRequest;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.model.TaskModel;
import com.fpt.myapplication.view.adapter.AssignUserAdapter;
import com.fpt.myapplication.view.fragment.AssignUserDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class CreateTaskActivity extends AppCompatActivity {
    // Thêm vào class:
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private TextInputLayout tilName, tilDesc;
    private TextInputEditText etName, etDescription;
    private MaterialButton btnDueDate, btnCreate, btnCancel, btnAssignees; // NEW


    private TaskModel  taskModel;
    private RecyclerView rvMembers;

    private AssignUserAdapter adapter;

    private ProjectModel projectModel;

    private final ArrayList<UserResponse> allUsers = new ArrayList<>();
    private final ArrayList<UserResponse> pickedUsers = new ArrayList<>();

    // NEW

    // Lưu ngày đã chọn (LocalDate) + formatter yyyy-MM-dd
    private LocalDate selectedDate = null;
    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_task_layout);
        projectModel = new ProjectModel(this);
        taskModel = new TaskModel(this);
        bindViews();

        adapter = new AssignUserAdapter();
        adapter.setShowRemove(true); // nếu item có nút Xoá
        adapter.setOnRemoveClickListener((user, pos) -> {
            // Bỏ user khỏi pickedUsers
            for (int i = 0; i < pickedUsers.size(); i++) {
                if (pickedUsers.get(i).getId() == user.getId()) {
                    pickedUsers.remove(i);
                    break;
                }
            }
            // Cập nhật lại danh sách hiển thị
            renderPickedChips();
        });
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(adapter);

        setupListeners();
        loadAllUsers();

        renderPickedChips(); // ban đầu trống
    }

    private void bindViews() {
        rvMembers = findViewById(R.id.rvMembers);
        tilName = findViewById(R.id.tilName);
        tilDesc = findViewById(R.id.tilDescription);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        btnDueDate = findViewById(R.id.btnDueDate);
        btnCreate = findViewById(R.id.btnCreate);
        btnCancel = findViewById(R.id.btnCancel);
        btnAssignees = findViewById(R.id.btnAssignees);
    }

    private void setupListeners() {
        btnDueDate.setOnClickListener(v -> openDatePicker());

        btnCreate.setOnClickListener(v -> {
            clearErrors();
            if (validateForm()) {
                TaskForm form = collectForm();
                // Lấy list ID của assignees (nếu cần đẩy API)
                List<Integer> assigneeIds = getAssigneeIds(); // NEW
                String due = selectedDate != null ? DISPLAY_FMT.format(selectedDate) : "";
                CreateTaskRequest request = new CreateTaskRequest();
                request.setTitle(form.name);
                request.setDescription(form.description);
                request.setDueDate(due);
                request.setAssigneeIds(assigneeIds);
                request.setProjectId(1);

                taskModel.createTask(request, new TaskModel.CreateTaskCallBack() {
                    @Override
                    public void onSuccess() {

                        btnCreate.setEnabled(true);
                        btnCancel.setEnabled(true);
                        btnCreate.setText("Tạo");

                        new SweetAlertDialog(CreateTaskActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Thông báo")
                                .setContentText("Đã tạo thành công công việc!")
                                .setConfirmText("OK")
                                .show();
                    }
                    @Override
                    public void onError(ResponseError error) {
                        btnCreate.setEnabled(true);
                        btnCancel.setEnabled(true);
                        btnCreate.setText("Hủy");

                        new SweetAlertDialog(CreateTaskActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Thông báo")
                                .setContentText(error.message)
                                .setConfirmText("OK")
                                .show();
                    }

                    @Override
                    public void onLoading() {
                        btnCreate.setEnabled(false);
                        btnCancel.setEnabled(false);
                        btnCreate.setText("Đang tạo...");
                    }
                });

            }
        });

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        // Mở dialog chọn thành viên (chỉ mở khi đã tải xong users)
        btnAssignees.setOnClickListener(v -> {
            if (allUsers.isEmpty()) {
                Toast.makeText(this, "Đang tải danh sách thành viên...", Toast.LENGTH_SHORT).show();
                loadAllUsers();
                return;
            }
            openAssignDialog();
        });
    }

    // ====== USERS ====== //
    private void loadAllUsers() {
        projectModel.getProjectsByMemberId(1, new ProjectModel.GetProjectsByMemberCallBack() {
            @Override
            public void onSuccess(List<UserResponse> data) {
                allUsers.clear();
                allUsers.addAll(data);
            }

            @Override
            public void onError(ResponseError error) {

            }

            @Override
            public void onLoading() {

            }
        });
    }

    private void openAssignDialog() { // NEW
        AssignUserDialog dlg = AssignUserDialog.newInstance(
                new ArrayList<>(allUsers),     // ALL (đã lấy từ API)
                new ArrayList<>(pickedUsers)   // đã chọn hiện tại
        );
        dlg.setCallback(new AssignUserDialog.Callback() {
            @Override public void onPicked(List<UserResponse> picked) {
                pickedUsers.clear();
                pickedUsers.addAll(picked);
                renderPickedChips();
            }
            @Override public void onCanceled() { /* no-op */ }
        });
        dlg.show(getSupportFragmentManager(), "assign_users");
    }

    private void renderPickedChips() {
        adapter.submitList(new ArrayList<>(pickedUsers));
    }

    private List<Integer> getAssigneeIds() {
        List<Integer> ids = new ArrayList<>();
        for (UserResponse u : pickedUsers) ids.add(u.getId());
        return ids;
    }

    // ====== DATE ====== //
    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày hết hạn")
                .build();

        picker.addOnPositiveButtonClickListener(selectionMillis -> {
            LocalDate date = Instant.ofEpochMilli(selectionMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            selectedDate = date;
            btnDueDate.setText(DISPLAY_FMT.format(date));
        });

        picker.show(getSupportFragmentManager(), "due_date_picker");
    }

    // ====== VALIDATE ====== //
    private void clearErrors() {
        if (tilName != null) tilName.setError(null);
        if (tilDesc != null) tilDesc.setError(null);
    }

    private boolean validateForm() {
        boolean ok = true;

        String name = etName != null ? trimOrEmpty(etName.getText()) : "";
        String desc = etDescription != null ? trimOrEmpty(etDescription.getText()) : "";

        if (TextUtils.isEmpty(name)) {
            if (tilName != null) tilName.setError("Vui lòng nhập tên công việc");
            ok = false;
        }

        if (TextUtils.isEmpty(desc)) {
            if (tilDesc != null) tilDesc.setError("Vui lòng nhập mô tả");
            ok = false;
        }

        if (selectedDate == null) {
            new SweetAlertDialog(CreateTaskActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Thông báo")
                    .setContentText("Vui lòng chọn này hết hạn")
                    .setConfirmText("OK")
                    .show();
            ok = false;
        }

        if(pickedUsers.isEmpty()){
            new SweetAlertDialog(CreateTaskActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Thông báo")
                    .setContentText("Vui lòng chọn người được giao công việc")
                    .setConfirmText("OK")
                    .show();
            ok = false;
        }

        return ok;
    }

    // ====== FORM ====== //
    private TaskForm collectForm() {
        String name = trimOrEmpty(etName.getText());
        String desc = trimOrEmpty(etDescription.getText());
        String due = selectedDate != null ? ISO_LOCAL.format(selectedDate) : ""; // yyyy-MM-dd
        return new TaskForm(name, desc, due);
    }

    private String trimOrEmpty(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    private static class TaskForm {
        final String name;
        final String description;
        final String dueDate; // yyyy-MM-dd
        TaskForm(String name, String description, String dueDate) {
            this.name = name; this.description = description; this.dueDate = dueDate;
        }
    }
}
