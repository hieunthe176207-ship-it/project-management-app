package com.fpt.myapplication.view.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fpt.myapplication.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RolePickerDialog extends DialogFragment {

    public static final String TAG = "RolePickerDialog";
    public static final String KEY_REQUEST = "role_picker_request";
    public static final String KEY_RESULT_ROLE = "result_role";
    private static final String ARG_CURRENT_ROLE_INDEX = "arg_current_role_index";
    private static final String ARG_TITLE = "arg_title";

    public static RolePickerDialog newInstance(int currentRoleIndex, @Nullable String title) {
        RolePickerDialog d = new RolePickerDialog();
        Bundle b = new Bundle();
        b.putInt(ARG_CURRENT_ROLE_INDEX, currentRoleIndex);
        if (title != null) b.putString(ARG_TITLE, title);
        d.setArguments(b);
        return d;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.update_role_member_dialog, null, false);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        Spinner spRole = view.findViewById(R.id.spRole);

        // Set title if needed (optional, since your layout already has a static title)
        String dynamicTitle = getArguments() != null
                ? getArguments().getString(ARG_TITLE, "Cập nhật vai trò nhân viên")
                : "Cập nhật vai trò nhân viên";
        tvTitle.setText(dynamicTitle);

        // Set up spinner with two roles
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Thành viên", "Quản lý"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);

        // Set default selection based on current role index
        int currentIndex = getArguments() != null ? getArguments().getInt(ARG_CURRENT_ROLE_INDEX, 0) : 0;
        spRole.setSelection(currentIndex);

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    int pickedIndex = spRole.getSelectedItemPosition();
                    Bundle result = new Bundle();
                    result.putInt(KEY_RESULT_ROLE, pickedIndex);
                    getParentFragmentManager().setFragmentResult(KEY_REQUEST, result);
                })
                .setNegativeButton("Hủy", null)
                .create();
    }
}