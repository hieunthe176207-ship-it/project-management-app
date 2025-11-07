package com.fpt.myapplication.view.adapter;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.TaskResponseDto;
import com.google.android.material.chip.Chip;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Adapter hiển thị Task dạng lưới 2 cột.
 * - Hiển thị: title, projectName, dueDate (dd/MM/yyyy), status (chip màu).
 */
public class SearchTaskAdapter extends ListAdapter<TaskResponseDto, SearchTaskAdapter.TaskVH> {

    public interface OnTaskClick {
        void onClick(TaskResponseDto item);
    }

    private final OnTaskClick onTaskClick;

    // dueDate backend trả về từ LocalDate.toString() => "yyyy-MM-dd"
    private final DateTimeFormatter inDate  = DateTimeFormatter.ISO_LOCAL_DATE;                 // yyyy-MM-dd
    private final DateTimeFormatter outDate = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()); // dd/MM/yyyy

    public SearchTaskAdapter(OnTaskClick cb) {
        super(DIFF);
        this.onTaskClick = cb;
    }

    // DiffUtil cho hiệu năng
    private static final DiffUtil.ItemCallback<TaskResponseDto> DIFF =
            new DiffUtil.ItemCallback<TaskResponseDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull TaskResponseDto o, @NonNull TaskResponseDto n) {
                    if (o.getId() == null || n.getId() == null) return o == n;
                    return o.getId().equals(n.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull TaskResponseDto o, @NonNull TaskResponseDto n) {
                    return eq(o.getTitle(), n.getTitle())
                            && eq(o.getProjectName(), n.getProjectName())
                            && eq(o.getDueDate(), n.getDueDate())
                            && eq(o.getStatus(), n.getStatus())
                            && eq(o.getDescription(), n.getDescription());
                }

                private boolean eq(Object a, Object b) {
                    return a == b || (a != null && a.equals(b));
                }
            };

    @NonNull
    @Override
    public TaskVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_task_item, parent, false);
        return new TaskVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskVH h, int position) {
        h.bind(getItem(position), this);
    }

    // ---------------------- ViewHolder ----------------------
    static class TaskVH extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvProjectName, tvDeadline;
        Chip chipStatus;

        TaskVH(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        @SuppressLint("SetTextI18n")
        void bind(TaskResponseDto item, SearchTaskAdapter adapter) {
            // Tên task
            String title = item.getTitle() != null ? item.getTitle() : "(Không có tên)";
            tvTaskName.setText(title);

            // Tên dự án
            String pj = item.getProjectName() != null ? item.getProjectName() : "(Chưa gán dự án)";
            tvProjectName.setText("Dự án: " + pj);

            // Deadline dd/MM/yyyy
            tvDeadline.setText("Hạn: " + adapter.formatDue(item.getDueDate()));

            // Trạng thái
            String normalized = normalizeStatus(item.getStatus());
            chipStatus.setText(adapter.statusLabel(normalized));
            adapter.styleStatusChip(chipStatus, normalized);

            itemView.setOnClickListener(v -> {
                if (adapter.onTaskClick != null) adapter.onTaskClick.onClick(item);
            });
        }

        private String normalizeStatus(String raw) {
            if (raw == null) return "TODO";
            String s = raw.trim().toUpperCase(Locale.US)
                    .replace(' ', '_')
                    .replace('-', '_');
            switch (s) {
                case "TODO":
                case "IN_PROGRESS":
                case "DONE":
                case "IN_REVIEW":
                    return s;
                default:
                    return "TODO";
            }
        }
    }

    // ---------------------- Helpers ----------------------

    /** Format yyyy-MM-dd -> dd/MM/yyyy; nếu lỗi, trả nguyên văn để dễ debug */
    private String formatDue(String due) {
        if (due == null || due.trim().isEmpty()) return "(không đặt)";
        try {
            LocalDate d = LocalDate.parse(due.trim(), inDate);
            return d.format(outDate);
        } catch (Exception e) {
            return due;
        }
    }

    /** Nhãn tiếng Việt cho trạng thái */
    private String statusLabel(String s) {
        switch (s) {
            case "DONE":        return "Hoàn thành";
            case "IN_PROGRESS": return "Đang làm";
            case "IN_REVIEW":   return "Chờ duyệt";
            case "TODO":
            default:            return "Chờ làm";
        }
    }

    /** Màu chip theo trạng thái (đổi theo palette app nếu muốn) */
    private void styleStatusChip(Chip chip, String status) {
        @ColorInt int bg;
        @ColorInt int fg;

        switch (status) {
            case "DONE":
                // xanh lá nhạt
                bg = Color.parseColor("#C8E6C9");
                fg = Color.parseColor("#1B5E20");
                break;
            case "IN_PROGRESS":
                // amber nhạt
                bg = Color.parseColor("#FFE0B2");
                fg = Color.parseColor("#E65100");
                break;
            case "IN_REVIEW":
                // xanh dương nhạt
                bg = Color.parseColor("#BBDEFB");
                fg = Color.parseColor("#0D47A1");
                break;
            case "TODO":
            default:
                // xám nhạt
                bg = Color.parseColor("#E0E0E0");
                fg = Color.parseColor("#424242");
                break;
        }

        chip.setChipBackgroundColor(ColorStateList.valueOf(bg));
        chip.setTextColor(fg);
        chip.setClickable(false);
        chip.setCheckable(false);
        chip.setChipIconVisible(false);
    }

    // ---------------------- Tiện ích setup nhanh ----------------------

    /** Gọi nhanh ở Fragment/Activity để set grid 2 cột */
    public static void setupAsTwoColumns(RecyclerView rv) {
        rv.setLayoutManager(new GridLayoutManager(rv.getContext(), 2));
        rv.setHasFixedSize(true);
        // Nếu muốn spacing đẹp, thêm ItemDecoration riêng của bạn.
        // rv.addItemDecoration(new GridSpacingItemDecoration(2, dp(rv, 8), true));
    }
}
