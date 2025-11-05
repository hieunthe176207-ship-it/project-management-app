package com.fpt.myapplication.view.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.view.adapter.AddUserDialogAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AssignUserDialog extends DialogFragment {

    public interface Callback {
        void onPicked(List<UserResponse> picked);
        default void onCanceled() {}
    }

    private static final String ARG_ALL = "arg_all_users";           // ArrayList<UserResponse> (Serializable)
    private static final String ARG_PICKED_INIT = "arg_picked_init"; // ArrayList<UserResponse> (Serializable)

    public static AssignUserDialog newInstance(ArrayList<UserResponse> allUsers,
                                               ArrayList<UserResponse> alreadyPicked) {
        AssignUserDialog f = new AssignUserDialog();
        Bundle b = new Bundle();
        b.putSerializable(ARG_ALL, allUsers);
        b.putSerializable(ARG_PICKED_INIT, alreadyPicked);
        f.setArguments(b);
        return f;
    }

    private Callback callback;
    public void setCallback(Callback cb) { this.callback = cb; }

    // State
    private final ArrayList<UserResponse> listAll = new ArrayList<>();    // còn có thể chọn
    private final ArrayList<UserResponse> listPicked = new ArrayList<>(); // đã chọn

    // UI
    private AddUserDialogAdapter adapterAll;
    private AddUserDialogAdapter adapterPicked;
    private LinearLayout rowPicked;
    private RecyclerView rvAll, rvPicked;
    private AlertDialog alert;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.assign_user_dialog, null, false);

        // Bind views
        rowPicked = v.findViewById(R.id.rowPicked);
        rvPicked  = v.findViewById(R.id.rvPicked);
        rvAll     = v.findViewById(R.id.rvUsers);

        // 1) Recycler + adapter TRƯỚC
        rvAll.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAll.addItemDecoration(new DividerItemDecoration(requireContext(), RecyclerView.VERTICAL));
        adapterAll = new AddUserDialogAdapter();
        rvAll.setAdapter(adapterAll);

        rvPicked.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPicked.addItemDecoration(new DividerItemDecoration(requireContext(), RecyclerView.VERTICAL));
        adapterPicked = new AddUserDialogAdapter();
        rvPicked.setAdapter(adapterPicked);

        // 2) Nhận dữ liệu args
        ArrayList<UserResponse> allArg    = castList(getArguments(), ARG_ALL);
        ArrayList<UserResponse> pickedArg = castList(getArguments(), ARG_PICKED_INIT);
        if (allArg != null)    listAll.addAll(allArg);
        if (pickedArg != null) listPicked.addAll(pickedArg);

        // 3) Lọc bỏ đã chọn khỏi listAll (CHỈ thao tác list)
        removePickedFromAll_LIST_ONLY();

        // 4) Seed dữ liệu vào adapter
        adapterAll.submitList(new ArrayList<>(listAll));
        adapterPicked.submitList(new ArrayList<>(listPicked));
        updatePickedVisibility();

        // 5) Click ALL -> chuyển sang PICKED
        adapterAll.setOnItemClick((user, position) -> {
            listPicked.add(user);
            adapterPicked.submitList(new ArrayList<>(listPicked));
            // xóa khỏi ALL
            removeFromAllById_LIST_ONLY(user.getId());
            adapterAll.submitList(new ArrayList<>(listAll));
            updatePickedVisibility();
        });

        // 6) Click PICKED -> trả về ALL
        adapterPicked.setOnItemClick((user, position) -> {
            removeFromPickedById_LIST_ONLY(user.getId());
            adapterPicked.submitList(new ArrayList<>(listPicked));
            // trả lại ALL
            listAll.add(user);
            adapterAll.submitList(new ArrayList<>(listAll));
            updatePickedVisibility();
        });

        // 7) Dialog với nút Hủy/Xong
        alert = new AlertDialog.Builder(requireContext())
                .setView(v)
                .setNegativeButton("Hủy", (d, i) -> {
                    if (callback != null) callback.onCanceled();
                })
                .setPositiveButton("Xong", null) // override ở onStart
                .create();

        return alert;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (alert == null) return;
        androidx.appcompat.app.AlertDialog ad = (androidx.appcompat.app.AlertDialog) getDialog();
        if (ad == null) return;

        View buttonPanel = ad.findViewById(androidx.appcompat.R.id.buttonPanel);
        if (buttonPanel != null) {
            buttonPanel.setBackgroundColor(android.graphics.Color.WHITE);
        }

        final android.widget.Button btnPositive = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btnPositive != null) {
            btnPositive.setOnClickListener(v -> {
                if (listPicked.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng chọn ít nhất một thành viên", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (callback != null) callback.onPicked(new ArrayList<>(listPicked));
                dismiss();
            });
        }
    }

    // ===== Helpers (LIST ONLY — không chạm adapter ở đây) =====
    @SuppressWarnings("unchecked")
    private ArrayList<UserResponse> castList(Bundle b, String key) {
        if (b == null) return null;
        Object o = b.getSerializable(key);
        if (o instanceof ArrayList<?>) return (ArrayList<UserResponse>) o;
        return null;
    }

    private void updatePickedVisibility() {
        if (rowPicked != null) {
            rowPicked.setVisibility(listPicked.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void removePickedFromAll_LIST_ONLY() {
        if (listPicked.isEmpty() || listAll.isEmpty()) return;
        for (UserResponse p : new ArrayList<>(listPicked)) {
            removeFromAllById_LIST_ONLY(p.getId());
        }
    }

    private void removeFromAllById_LIST_ONLY(int id) {
        for (Iterator<UserResponse> it = listAll.iterator(); it.hasNext();) {
            if (it.next().getId() == id) { it.remove(); break; }
        }
    }

    private void removeFromPickedById_LIST_ONLY(int id) {
        for (Iterator<UserResponse> it = listPicked.iterator(); it.hasNext();) {
            if (it.next().getId() == id) { it.remove(); break; }
        }
    }
}
