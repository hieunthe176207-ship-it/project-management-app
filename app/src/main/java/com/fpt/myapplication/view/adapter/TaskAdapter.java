package com.fpt.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.TaskResponse;

// Dùng ListAdapter (thay vì Adapter) vì nó "thông minh" hơn
public class TaskAdapter extends ListAdapter<TaskResponse, TaskAdapter.TaskViewHolder> {

    public TaskAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    // 3. Đổ data vào UI
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponse task = getItem(position);
        holder.bind(task);
    }

    // 4. Lớp ViewHolder (Giữ các ID)
    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvProject, tvDueDate;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvProject = itemView.findViewById(R.id.tv_project_name);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
        }

        public void bind(TaskResponse task) {
            if (task == null) return;

            tvTitle.setText(task.getTitle());

            if (task.getProject() != null) {
                tvProject.setText("Project: " + task.getProject().getName());
                tvProject.setVisibility(View.VISIBLE);
            } else {
                tvProject.setVisibility(View.GONE);
            }

            // Lấy Due Date
            tvDueDate.setText("Due: " + task.getStatus());
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
                    // Bạn có thể so sánh thêm title, status...
                    return oldItem.getTitle().equals(newItem.getTitle());
                }
            };
}