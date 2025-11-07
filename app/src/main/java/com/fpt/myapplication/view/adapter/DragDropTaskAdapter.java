package com.fpt.myapplication.view.adapter;


import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fpt.myapplication.R;
import com.fpt.myapplication.model.TaskModel;
import com.fpt.myapplication.model.TaskStatus;
import com.fpt.myapplication.model.User;
import java.util.List;

public class DragDropTaskAdapter extends RecyclerView.Adapter<DragDropTaskAdapter.TaskViewHolder> {

    private List<TaskModel> tasks;
    private OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onTaskClick(TaskModel task);
        void onTaskStartDrag(TaskViewHolder viewHolder);
        void onTaskMoved(TaskModel task, TaskStatus newStatus);
    }

    public DragDropTaskAdapter(List<TaskModel> tasks) {
        this.tasks = tasks;
    }

    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card_draggable, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<TaskModel> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    public void removeTask(TaskModel task) {
        int position = tasks.indexOf(task);
        if (position != -1) {
            tasks.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addTask(TaskModel task) {
        tasks.add(task);
        notifyItemInserted(tasks.size() - 1);
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskTitle;
        private TextView tvTaskDescription;
        private TextView tvTaskDueDate;
        private TextView tvAssignees;
        private ImageView ivDragHandle;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvTaskDueDate = itemView.findViewById(R.id.tv_task_due_date);
            tvAssignees = itemView.findViewById(R.id.tv_assignees);
            ivDragHandle = itemView.findViewById(R.id.iv_drag_handle);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTaskClick(tasks.get(position));
                    }
                }
            });

            ivDragHandle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (listener != null) {
                        listener.onTaskStartDrag(this);
                    }
                }
                return false;
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