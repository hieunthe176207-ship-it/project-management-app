package com.fpt.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.constant.TaskStatus;
import com.fpt.myapplication.dto.response.TaskResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CardTaskAdapter extends ListAdapter<TaskResponse, CardTaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(TaskResponse task);
    }

    private OnTaskClickListener listener;

    public CardTaskAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnTaskClickListener(OnTaskClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponse task = getItem(position);
        holder.bind(task);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvStatus, tvProject, tvDeadline, tvOverdue;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvProject = itemView.findViewById(R.id.tv_project);
            tvDeadline = itemView.findViewById(R.id.tv_deadline);
            tvOverdue = itemView.findViewById(R.id.tv_overdue);
        }

        public void bind(TaskResponse task) {
            if (task == null) return;
            tvTitle.setText(task.getTitle());
            tvDeadline.setText("Hạn: " + task.getDueDate());

            // Tính toán quá hạn
            boolean isOverdue = isTaskOverdue(task.getDueDate());
            if (isOverdue) {
                tvOverdue.setVisibility(View.VISIBLE);
            } else {
                tvOverdue.setVisibility(View.GONE);
            }

            if (task.getStatus() != null) {
                applyStatusStyle(task.getStatus());
            } else {
                applyStatusStyle(TaskStatus.TODO);
            }
            itemView.setOnClickListener(v -> { if (listener != null) listener.onTaskClick(task); });
        }

        private boolean isTaskOverdue(String dueDate) {
            if (dueDate == null || dueDate.isEmpty()) {
                return false;
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date deadline = sdf.parse(dueDate);

                if (deadline == null) {
                    return false;
                }

                // Reset time để so sánh chỉ ngày
                Calendar deadlineCal = Calendar.getInstance();
                deadlineCal.setTime(deadline);
                deadlineCal.set(Calendar.HOUR_OF_DAY, 0);
                deadlineCal.set(Calendar.MINUTE, 0);
                deadlineCal.set(Calendar.SECOND, 0);
                deadlineCal.set(Calendar.MILLISECOND, 0);

                Calendar currentCal = Calendar.getInstance();
                currentCal.set(Calendar.HOUR_OF_DAY, 0);
                currentCal.set(Calendar.MINUTE, 0);
                currentCal.set(Calendar.SECOND, 0);
                currentCal.set(Calendar.MILLISECOND, 0);

                return deadlineCal.before(currentCal);
            } catch (ParseException e) {
                return false;
            }
        }

        private void applyStatusStyle(TaskStatus status) {
            switch (status) {
                case TODO:
                    tvStatus.setText(itemView.getContext().getString(R.string.status_todo));
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_todo);
                    tvStatus.setTextColor(itemView.getResources().getColor(R.color.status_todo_text));
                    break;
                case IN_PROGRESS:
                    tvStatus.setText(itemView.getContext().getString(R.string.status_in_progress));
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_in_progress);
                    tvStatus.setTextColor(itemView.getResources().getColor(R.color.status_in_progress_text));
                    break;
                case IN_REVIEW:
                    tvStatus.setText(itemView.getContext().getString(R.string.status_in_review));
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_in_review);
                    tvStatus.setTextColor(itemView.getResources().getColor(R.color.status_in_review_text));
                    break;
                case DONE:
                    tvStatus.setText(itemView.getContext().getString(R.string.status_done));
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_done);
                    tvStatus.setTextColor(itemView.getResources().getColor(R.color.status_done_text));
                    break;
            }
        }
    }

    private static final DiffUtil.ItemCallback<TaskResponse> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TaskResponse>() {
                @Override
                public boolean areItemsTheSame(@NonNull TaskResponse oldItem, @NonNull TaskResponse newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull TaskResponse oldItem, @NonNull TaskResponse newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle())
                            && ((oldItem.getStatus() == null && newItem.getStatus() == null) ||
                            (oldItem.getStatus() != null && newItem.getStatus() != null && oldItem.getStatus().equals(newItem.getStatus())));
                }
            };
}