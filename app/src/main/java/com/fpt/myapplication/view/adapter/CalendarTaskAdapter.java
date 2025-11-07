package com.fpt.myapplication.view.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.fpt.myapplication.R;
import java.util.ArrayList;
import java.util.List;

public class CalendarTaskAdapter extends RecyclerView.Adapter<CalendarTaskAdapter.CalendarTaskVH> {
    private final List<CalendarTaskData> items = new ArrayList<>();

    public void setItems(List<CalendarTaskData> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public CalendarTaskVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_task, parent, false);
        return new CalendarTaskVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarTaskVH h, int position) {
        CalendarTaskData it = items.get(position);
        h.name.setText(it.name);
        h.info.setText(it.info);
        h.progress.setText(it.progress);
        h.status.setText(it.status);
        int color;
        switch (it.status) {
            case "IN_PROGRESS":
                h.status.setBackgroundResource(R.drawable.bg_chip_in_progress);
                color = ContextCompat.getColor(h.itemView.getContext(), R.color.status_in_progress_text); break;
            case "DONE":
                h.status.setBackgroundResource(R.drawable.bg_chip_done);
                color = ContextCompat.getColor(h.itemView.getContext(), R.color.status_done_text); break;
            case "TODO":
                h.status.setBackgroundResource(R.drawable.bg_chip_todo);
                color = ContextCompat.getColor(h.itemView.getContext(), R.color.status_todo_text); break;
            case "IN_REVIEW":
                h.status.setBackgroundResource(R.drawable.bg_chip_in_review);
                color = ContextCompat.getColor(h.itemView.getContext(), R.color.status_in_review_text); break;
            default:
                h.status.setBackgroundResource(R.drawable.bg_chip_todo);
                color = Color.BLACK;
        }
        h.status.setTextColor(color);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class CalendarTaskVH extends RecyclerView.ViewHolder {
        TextView name, info, progress, status;
        CalendarTaskVH(View v) {
            super(v);
            name = v.findViewById(R.id.tvTaskName);
            info = v.findViewById(R.id.tvAssignedInfo);
            progress = v.findViewById(R.id.tvTaskProgress);
            status = v.findViewById(R.id.tvTaskStatusBadge);
        }
    }

    public static class CalendarTaskData {
        public final String name;
        public final String status;
        public final String progress;
        public final String info;
        public CalendarTaskData(String name, String status, String progress, String info) {
            this.name = name; this.status = status; this.progress = progress; this.info = info;
        }
    }
}
