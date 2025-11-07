package com.fpt.myapplication.view.adapter;





import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fpt.myapplication.R;
import com.fpt.myapplication.model.TaskModel;
import com.fpt.myapplication.model.User; // Đổi tên từ UserModel thành User
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskModel> tasks;
    private OnTaskClickListener onTaskClickListener;

    public interface OnTaskClickListener {
        void onTaskClick(TaskModel task);
        void onTaskLongClick(TaskModel task);
    }

    public TaskAdapter(List<TaskModel> tasks) {
        this.tasks = tasks;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.onTaskClickListener = listener;
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
        TaskModel task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        int count = tasks != null ? tasks.size() : 0;
        Log.d("TaskAdapter", "getItemCount: " + count);
        return count;
    }


    public void updateTasks(List<TaskModel> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskTitle;
        private TextView tvTaskDescription;
        private TextView tvTaskDueDate;
        private TextView tvAssignees;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvTaskDueDate = itemView.findViewById(R.id.tv_task_due_date);
            tvAssignees = itemView.findViewById(R.id.tv_assignees);

            itemView.setOnClickListener(v -> {
                if (onTaskClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onTaskClickListener.onTaskClick(tasks.get(position));
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (onTaskClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onTaskClickListener.onTaskLongClick(tasks.get(position));
                    }
                }
                return true;
            });
        }

        public void bind(TaskModel task) {
            tvTaskTitle.setText(task.getTitle());
            tvTaskDescription.setText(task.getDescription());

            if (task.getDueDate() != null) {
                tvTaskDueDate.setText("Due: " + task.getDueDate().toString());
                tvTaskDueDate.setVisibility(View.VISIBLE);
            } else {
                tvTaskDueDate.setVisibility(View.GONE);
            }

            if (task.getAssignees() != null && !task.getAssignees().isEmpty()) {
                // Sử dụng StringBuilder thay vì Stream API
                StringBuilder assigneeNames = new StringBuilder();
                for (int i = 0; i < task.getAssignees().size(); i++) {
                    if (i > 0) {
                        assigneeNames.append(", ");
                    }
                    assigneeNames.append(task.getAssignees().get(i).getDisplayName());
                }
                tvAssignees.setText(assigneeNames.toString());
            } else {
                tvAssignees.setText("Unassigned");
            }
        }
    }
}
