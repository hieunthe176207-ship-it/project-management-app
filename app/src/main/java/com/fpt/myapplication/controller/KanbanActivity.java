package com.fpt.myapplication.controller;

import android.content.ClipData;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;
import android.content.Intent;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.fpt.myapplication.R;
import com.fpt.myapplication.api.TaskApi;
import com.fpt.myapplication.api.ProjectApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.dto.request.TaskUpdateStatusRequestDto;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.response.UpdateTaskReponse;
import com.fpt.myapplication.model.KanbanBoardModel;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.model.TaskStatus;
import com.fpt.myapplication.util.CrossColumnDropZone;
// import com.fpt.project.util.DragDropHelper; // <-- KHÔNG CẦN NỮA
import com.fpt.myapplication.util.Util;
import com.fpt.myapplication.view.adapter.KanbanDragDropAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// LOẠI BỎ 'DragDropHelper.DragDropListener'
public class KanbanActivity extends AppCompatActivity implements
        KanbanDragDropAdapter.OnTaskActionListener {

    private static final String TAG = "KanbanActivity";

    private RecyclerView rvTodoTasks, rvInProgressTasks, rvInReviewTasks, rvDoneTasks;
    private KanbanDragDropAdapter todoAdapter, inProgressAdapter, inReviewAdapter, doneAdapter;

    // KHÔNG CẦN ItemTouchHelper NỮA
    // private ItemTouchHelper todoItemTouchHelper, ...

    private TaskApi taskApi;
    private ProjectApi projectApi;
    private Integer projectId;
    private ProjectModel projectModel;
    private TextView tvGreeting, tvSubtitle;
    private FloatingActionButton fabAddTask;

    private Map<RecyclerView, TaskStatus> recyclerViewToStatusMap;
    private Map<TaskStatus, KanbanDragDropAdapter> statusToAdapterMap;
    private Map<TaskStatus, RecyclerView> statusToRecyclerViewMap;

    // Drop zones
    private CrossColumnDropZone.DropZoneOverlay todoDropZone, inProgressDropZone,
            inReviewDropZone, doneDropZone;

    private UpdateTaskReponse currentDraggedTask;
    private TaskStatus currentDraggedFromStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanban_drag_drop);

        projectModel = new ProjectModel(this);

        projectId = getIntent().getIntExtra("projectId", -1);
        Log.d(TAG, "Project ID received: " + projectId);

        if (projectId == -1) {
            Toast.makeText(this, "Invalid project ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupMaps();
        setupRecyclerViews();
        // setupDragAndDrop(); // <-- KHÔNG CẦN NỮA
        setupDropZones(); // Đây là phương thức thiết lập kéo thả chính
        loadProjectInfo();
        loadKanbanBoard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from CreateTaskActivity or TaskActivity
        loadKanbanBoard();
    }

    private void initViews() {
        rvTodoTasks = findViewById(R.id.rv_todo_tasks);
        rvInProgressTasks = findViewById(R.id.rv_in_progress_tasks);
        rvInReviewTasks = findViewById(R.id.rv_in_review_tasks);
        rvDoneTasks = findViewById(R.id.rv_done_tasks);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        fabAddTask = findViewById(R.id.fab_add_task);

        taskApi = ApiClient.getRetrofit(this).create(TaskApi.class);
        projectApi = ApiClient.getRetrofit(this).create(ProjectApi.class);

        // Setup FAB click listener
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTaskActivity.class);
            intent.putExtra("project_id", projectId);
            startActivity(intent);
        });
    }

    private void setupMaps() {
        // Map RecyclerView to TaskStatus
        recyclerViewToStatusMap = new HashMap<>();
        recyclerViewToStatusMap.put(rvTodoTasks, TaskStatus.TODO);
        recyclerViewToStatusMap.put(rvInProgressTasks, TaskStatus.IN_PROGRESS);
        recyclerViewToStatusMap.put(rvInReviewTasks, TaskStatus.IN_REVIEW);
        recyclerViewToStatusMap.put(rvDoneTasks, TaskStatus.DONE);

        // Map TaskStatus to RecyclerView
        statusToRecyclerViewMap = new HashMap<>();
        statusToRecyclerViewMap.put(TaskStatus.TODO, rvTodoTasks);
        statusToRecyclerViewMap.put(TaskStatus.IN_PROGRESS, rvInProgressTasks);
        statusToRecyclerViewMap.put(TaskStatus.IN_REVIEW, rvInReviewTasks);
        statusToRecyclerViewMap.put(TaskStatus.DONE, rvDoneTasks);
    }

    private void setupRecyclerViews() {
        // Setup adapters
        todoAdapter = new KanbanDragDropAdapter(new ArrayList<>());
        inProgressAdapter = new KanbanDragDropAdapter(new ArrayList<>());
        inReviewAdapter = new KanbanDragDropAdapter(new ArrayList<>());
        doneAdapter = new KanbanDragDropAdapter(new ArrayList<>());

        // Map TaskStatus to Adapter
        statusToAdapterMap = new HashMap<>();
        statusToAdapterMap.put(TaskStatus.TODO, todoAdapter);
        statusToAdapterMap.put(TaskStatus.IN_PROGRESS, inProgressAdapter);
        statusToAdapterMap.put(TaskStatus.IN_REVIEW, inReviewAdapter);
        statusToAdapterMap.put(TaskStatus.DONE, doneAdapter);

        // Setup RecyclerViews
        setupRecyclerView(rvTodoTasks, todoAdapter);
        setupRecyclerView(rvInProgressTasks, inProgressAdapter);
        setupRecyclerView(rvInReviewTasks, inReviewAdapter);
        setupRecyclerView(rvDoneTasks, doneAdapter);

        // Set listeners
        todoAdapter.setOnTaskActionListener(this);
        inProgressAdapter.setOnTaskActionListener(this);
        inReviewAdapter.setOnTaskActionListener(this);
        doneAdapter.setOnTaskActionListener(this);
    }

    private void setupRecyclerView(RecyclerView recyclerView, KanbanDragDropAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    // private void setupDragAndDrop() { ... } // <-- CÓ THỂ XÓA PHƯƠNG THỨC NÀY

    private void setupDropZones() {
        // Create drop zone overlays
        FrameLayout todoColumn = findViewById(R.id.ll_todo_column);
        FrameLayout inProgressColumn = findViewById(R.id.ll_in_progress_column);
        FrameLayout inReviewColumn = findViewById(R.id.ll_in_review_column);
        FrameLayout doneColumn = findViewById(R.id.ll_done_column);

        todoDropZone = new CrossColumnDropZone.DropZoneOverlay(this, new CrossColumnDropZone(this));
        inProgressDropZone = new CrossColumnDropZone.DropZoneOverlay(this, new CrossColumnDropZone(this));
        inReviewDropZone = new CrossColumnDropZone.DropZoneOverlay(this, new CrossColumnDropZone(this));
        doneDropZone = new CrossColumnDropZone.DropZoneOverlay(this, new CrossColumnDropZone(this));

        // Add drop zones to columns
        if (todoColumn != null) todoColumn.addView(todoDropZone);
        if (inProgressColumn != null) inProgressColumn.addView(inProgressDropZone);
        if (inReviewColumn != null) inReviewColumn.addView(inReviewDropZone);
        if (doneColumn != null) doneColumn.addView(doneDropZone);

        // Setup drop listeners
        setupDropListener(rvTodoTasks, todoDropZone, TaskStatus.TODO);
        setupDropListener(rvInProgressTasks, inProgressDropZone, TaskStatus.IN_PROGRESS);
        setupDropListener(rvInReviewTasks, inReviewDropZone, TaskStatus.IN_REVIEW);
        setupDropListener(rvDoneTasks, doneDropZone, TaskStatus.DONE);
    }

    /**
     * Cập nhật setupDropListener để xử lý logic thả (cho cả 2 trường hợp).
     */
    private void setupDropListener(RecyclerView recyclerView,
                                   CrossColumnDropZone.DropZoneOverlay dropZone,
                                   TaskStatus targetStatus) {

        recyclerView.setOnDragListener((v, event) -> {
            RecyclerView targetRecyclerView = (RecyclerView) v;
            KanbanDragDropAdapter targetAdapter = statusToAdapterMap.get(targetStatus);
            if (targetAdapter == null) return false;

            // Lấy view gốc đang được kéo (đã được set INVISIBLE)
            View originalView = (View) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // Chỉ hiển thị dropzone nếu kéo từ cột khác
                    if (currentDraggedTask != null && currentDraggedFromStatus != targetStatus) {
                        dropZone.showDropZone();
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    // Thêm hiệu ứng highlight nếu muốn
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    // Bỏ hiệu ứng highlight
                    return true;

                case DragEvent.ACTION_DROP:
                    dropZone.hideDropZone(); // Luôn ẩn khi thả
                    if (currentDraggedTask == null) {
                        return false;
                    }

                    KanbanDragDropAdapter sourceAdapter = statusToAdapterMap.get(currentDraggedFromStatus);
                    if (sourceAdapter == null) return false;

                    if (currentDraggedFromStatus == targetStatus) {
                        // ---- KỊCH BẢN 1: THẢ TRONG CÙNG 1 CỘT (SẮP XẾP) ----
                        int fromPosition = sourceAdapter.getTasks().indexOf(currentDraggedTask);

                        // Tìm vị trí 'to' dựa trên tọa độ Y
                        View viewUnder = targetRecyclerView.findChildViewUnder(event.getX(), event.getY());
                        int toPosition = (viewUnder != null)
                                ? targetRecyclerView.getChildAdapterPosition(viewUnder)
                                : targetAdapter.getItemCount(); // Thả ở cuối nếu không tìm thấy

                        if(toPosition < 0) toPosition = targetAdapter.getItemCount();
                        if(fromPosition == -1) return false; // Lỗi không tìm thấy task

                        if (fromPosition != toPosition) {
                            targetAdapter.moveItem(fromPosition, toPosition);
                        }

                    } else {
                        // ---- KỊCH BẢN 2: THẢ SANG CỘT KHÁC (CHUYỂN STATUS) ----

                        // 1. Xóa khỏi adapter nguồn
                        int sourcePosition = sourceAdapter.getTasks().indexOf(currentDraggedTask);
                        if (sourcePosition != -1) {
                            sourceAdapter.removeItem(sourcePosition);
                        }

                        // 2. Tìm vị trí thả trong adapter đích
                        View viewUnder = targetRecyclerView.findChildViewUnder(event.getX(), event.getY());
                        int targetPosition = (viewUnder != null)
                                ? targetRecyclerView.getChildAdapterPosition(viewUnder)
                                : targetAdapter.getItemCount(); // Thả ở cuối

                        if(targetPosition < 0) targetPosition = targetAdapter.getItemCount();

                        // 3. Thêm vào adapter đích tại vị trí
                        targetAdapter.addItem(currentDraggedTask, targetPosition);

                        // 4. Cập nhật status cho model
                        currentDraggedTask.setStatus(targetStatus);

                        // 5. Gọi API
                        updateTaskStatusViaApi(currentDraggedTask, targetStatus);
                    }

                    // Bất kể thả ở đâu, làm cho view gốc hiện lại
                    if (originalView != null) {
                        originalView.setVisibility(View.VISIBLE);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    dropZone.hideDropZone(); // Dọn dẹp

                    // Nếu kéo thất bại (ví dụ: thả ra ngoài),
                    // hiển thị lại view gốc.
                    if (originalView != null && !event.getResult()) {
                        originalView.setVisibility(View.VISIBLE);
                    }

                    // Dọn dẹp biến tạm
                    currentDraggedTask = null;
                    currentDraggedFromStatus = null;
                    return true;

                default:
                    return false;
            }
        });
    }

    // --- CÁC PHƯƠNG THỨC LISTENER CŨ (DragDropHelper) ĐƯỢC XÓA ---
    // @Override public void onItemMoved(...) { ... }
    // @Override public void onItemStartDrag(...) { ... }
    // @Override public void onItemEndDrag(...) { ... }
    // @Override public boolean canDropOver(...) { ... }


    // --- Implement KanbanDragDropAdapter.OnTaskActionListener ---
    @Override
    public void onTaskClick(UpdateTaskReponse task) {
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("task_id", task.getId().intValue());
        intent.putExtra("project_id", projectId);
        startActivity(intent);
    }

    /**
     * Đây là phương thức onTaskStartDrag MỚI,
     * được gọi từ adapter sau khi bạn thay đổi interface.
     */
    @Override
    public void onTaskStartDrag(UpdateTaskReponse task, View dragView) {
        // 1. Lưu lại task và cột nguồn
        currentDraggedTask = task;
        RecyclerView sourceRecyclerView = (RecyclerView) dragView.getParent();
        currentDraggedFromStatus = recyclerViewToStatusMap.get(sourceRecyclerView);

        if (currentDraggedFromStatus == null) {
            Log.e(TAG, "Không tìm thấy status cho RecyclerView nguồn");
            return;
        }

        // 2. Tạo ClipData (để mang dữ liệu)
        ClipData clipData = ClipData.newPlainText("TASK_DRAG", task.getTitle());

        // 3. Tạo bóng kéo (shadow)
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(dragView);

        // 4. Bắt đầu kéo thả chuẩn của hệ thống
        // Chúng ta truyền 'dragView' làm 'localState' để có thể lấy lại sau này
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dragView.startDragAndDrop(clipData, shadowBuilder, dragView, 0);
        } else {
            dragView.startDrag(clipData, shadowBuilder, dragView, 0);
        }

        // 5. Ẩn view gốc đi
        dragView.setVisibility(View.INVISIBLE);
    }

    // PHƯƠNG THỨC CŨ (có thể xóa hoặc giữ lại để gọi API)
    @Override
    public void onTaskMoved(UpdateTaskReponse task, int fromPosition, int toPosition) {
        Log.d(TAG, "Task moved within column: " + task.getTitle());
        // TODO: Gọi API để cập nhật thứ tự task TRONG CÙNG MỘT CỘT (nếu cần)
    }

    // PHƯƠNG THỨC NÀY GIỜ ĐÃ THỪA, VÌ LOGIC ĐÃ NẰM TRONG ACTION_DROP
    // private void moveTaskToColumn(TaskModel task, TaskStatus fromStatus, TaskStatus toStatus) { ... }


    private void updateTaskStatusViaApi(UpdateTaskReponse task, TaskStatus newStatus) {
        TaskUpdateStatusRequestDto request = new TaskUpdateStatusRequestDto(newStatus);

        Call<ResponseSuccess<UpdateTaskReponse>> call = taskApi.updateTaskStatus(task.getId().intValue(), request);
        call.enqueue(new Callback<ResponseSuccess<UpdateTaskReponse>>() {


            @Override
            public void onResponse(Call<ResponseSuccess<UpdateTaskReponse>> call,
                                   Response<ResponseSuccess<UpdateTaskReponse>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Task status updated successfully via API");
                    Toast.makeText(KanbanActivity.this, "Task moved to " + newStatus.name(), Toast.LENGTH_SHORT).show();
                    // ... bên trong hàm onResponse()
                } else {
                    // ---- BẮT ĐẦU PHẦN DEBUG ----
                    ResponseError error = Util.parseError(response);
                    new SweetAlertDialog(KanbanActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Thông báo")
                            .setContentText(error.message)
                            .setConfirmText("OK")
                            .show();

                    // Revert the move
                    loadKanbanBoard();
                    // ---- KẾT THÚC PHẦN DEBUG ----
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<UpdateTaskReponse>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(KanbanActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                // Revert the move
                loadKanbanBoard();
            }
        });
    }

    private void loadKanbanBoard() {
        Log.d(TAG, "Loading kanban board for project: " + projectId);

        Call<ResponseSuccess<KanbanBoardModel>> call = taskApi.getKanbanBoard(projectId);
        call.enqueue(new Callback<ResponseSuccess<KanbanBoardModel>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<KanbanBoardModel>> call,
                                   Response<ResponseSuccess<KanbanBoardModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    KanbanBoardModel kanbanBoard = response.body().getData();
                    updateKanbanBoard(kanbanBoard);
                } else {
                    Toast.makeText(KanbanActivity.this, "Failed to load kanban board", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<KanbanBoardModel>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(KanbanActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateKanbanBoard(KanbanBoardModel kanbanBoard) {
        if (kanbanBoard.getTodoTasks() != null) {
            todoAdapter.updateTasks(kanbanBoard.getTodoTasks());
        }
        if (kanbanBoard.getInProgressTasks() != null) {
            inProgressAdapter.updateTasks(kanbanBoard.getInProgressTasks());
        }
        if (kanbanBoard.getInReviewTasks() != null) {
            inReviewAdapter.updateTasks(kanbanBoard.getInReviewTasks());
        }
        if (kanbanBoard.getDoneTasks() != null) {
            doneAdapter.updateTasks(kanbanBoard.getDoneTasks());
        }
    }

    // ------------------------------
    // Hiển thị tên dự án ở kanban header
    // ------------------------------
    private void loadProjectInfo() {
        if (projectApi == null || tvGreeting == null) return;
        Call<ResponseSuccess<ProjectResponse>> call = projectApi.getProjectById(projectId);
        call.enqueue(new Callback<ResponseSuccess<ProjectResponse>>() {
            @Override
            public void onResponse(Call<ResponseSuccess<ProjectResponse>> call,
                                   Response<ResponseSuccess<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProjectResponse project = response.body().getData();
                    if (project != null && tvGreeting != null) {
                        tvGreeting.setText(project.getName());
                        if (tvSubtitle != null) {
                            tvSubtitle.setText("Kanban Board");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess<ProjectResponse>> call, Throwable t) {
                // Log failure or leave default header
            }
        });
    }
}