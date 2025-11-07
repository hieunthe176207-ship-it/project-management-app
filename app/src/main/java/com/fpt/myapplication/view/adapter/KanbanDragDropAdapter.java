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
import com.fpt.myapplication.model.User;
import java.util.Collections;
import java.util.List;

public class KanbanDragDropAdapter extends RecyclerView.Adapter<KanbanDragDropAdapter.TaskViewHolder> {

    private List<TaskModel> tasks;
    private OnTaskActionListener listener;

    // BÊN TRONG KanbanDragDropAdapter.java

    public interface OnTaskActionListener {
        void onTaskClick(TaskModel task);
        // THAY ĐỔI DÒNG NÀY:
        // void onTaskStartDrag(TaskViewHolder viewHolder);
        void onTaskStartDrag(TaskModel task, View dragView); // Thay bằng dòng này

        void onTaskMoved(TaskModel task, int fromPosition, int toPosition);
    }

    public KanbanDragDropAdapter(List<TaskModel> tasks) {
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
        return tasks != null ? tasks.size() : 0;
    }

    public void updateTasks(List<TaskModel> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (tasks != null && fromPosition < tasks.size() && toPosition < tasks.size()) {
            Collections.swap(tasks, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);

            if (listener != null) {
                listener.onTaskMoved(tasks.get(toPosition), fromPosition, toPosition);
            }
        }
    }

    public TaskModel removeItem(int position) {
        if (tasks != null && position < tasks.size()) {
            TaskModel task = tasks.remove(position);
            notifyItemRemoved(position);
            return task;
        }
        return null;
    }

    public void addItem(TaskModel task) {
        if (tasks != null) {
            tasks.add(task);
            notifyItemInserted(tasks.size() - 1);
        }
    }

    public void addItem(TaskModel task, int position) {
        if (tasks != null) {
            tasks.add(position, task);
            notifyItemInserted(position);
        }
    }

    public List<TaskModel> getTasks() {
        return tasks;
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

            // SỬA LẠI: Đây là hành động CLICK, gọi onTaskClick
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // listener.onTaskStartDrag(tasks.get(position), itemView); // <-- SAI
                        listener.onTaskClick(tasks.get(position)); // <-- ĐÚNG
                    }
                }
            });

            // SỬA LẠI: Đây là hành động DRAG, gọi onTaskStartDrag với 2 tham số
            ivDragHandle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (listener != null) {
                        // listener.onTaskStartDrag(this); // <-- SAI

                        // Sửa thành thế này:
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            // 1. TaskModel
                            TaskModel taskToDrag = tasks.get(position);
                            // 2. View
                            View viewToDrag = itemView;

                            listener.onTaskStartDrag(taskToDrag, viewToDrag); // <-- ĐÚNG
                        }
                    }
                    return true;
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