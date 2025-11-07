//package com.fpt.myapplication.view.fragment;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider; // Import
//import androidx.recyclerview.widget.LinearLayoutManager; // Import
//import androidx.recyclerview.widget.RecyclerView; // Import
//
//import com.fpt.myapplication.R;
//import com.fpt.myapplication.constant.TaskStatus; // Import enum
//import com.fpt.myapplication.dto.response.TaskResponse;
//import com.fpt.myapplication.view.adapter.TaskAdapter; // Import Adapter
//import com.fpt.myapplication.viewmodel.MyTaskViewModel; // Import ViewModel
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class TaskPageFragment extends Fragment {
//
//    private static final String ARG_STATUS = "task_status";
//    private String status; // (e.g., "TODO")
//
//    // 1. Khai báo UI
//    private RecyclerView recyclerView;
//    private TaskAdapter taskAdapter; // Dùng Adapter mới
//    private TextView emptyView;
//
//    // 2. Khai báo "Bộ não" (Shared ViewModel)
//    private MyTaskViewModel myTaskViewModel;
//
//    // (Hàm newInstance giữ nguyên)
//    public static TaskPageFragment newInstance(String status) {
//        TaskPageFragment fragment = new TaskPageFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_STATUS, status);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            status = getArguments().getString(ARG_STATUS);
//        }
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        // (Dùng layout fragment_task_page.xml)
//        View view = inflater.inflate(R.layout.fragment_task_page, container, false);
//
//        // 3. Ánh xạ UI
//        emptyView = view.findViewById(R.id.empty_view);
//        recyclerView = view.findViewById(R.id.recycler_view_tasks);
//
//        // 4. Setup RecyclerView
//        setupRecyclerView();
//
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // 5. Lấy "Shared ViewModel" (Lấy từ Cha của nó là MyTaskFragment)
//        // Đây là "phép thuật"
//        myTaskViewModel = new ViewModelProvider(requireParentFragment()).get(MyTaskViewModel.class);
//
//        // 6. Lắng nghe (Observe) dữ liệu
//        observeTasks();
//    }
//
//    private void setupRecyclerView() {
//        taskAdapter = new TaskAdapter();
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(taskAdapter);
//    }
//
//    private void observeTasks() {
//        // Lắng nghe "myTasks" (chứa TẤT CẢ task) từ ViewModel
//        myTaskViewModel.myTasks.observe(getViewLifecycleOwner(), allTasks -> {
//
//            if (allTasks == null) {
//                // Xử lý lỗi (ví dụ: mất mạng)
//                emptyView.setText("Error loading tasks");
//                emptyView.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.GONE);
//                return;
//            }
//
//            // 7. PHẦN LỌC (Filter)
//            List<TaskResponse> filteredList = new ArrayList<>();
//            for (TaskResponse task : allTasks) {
//                // (task.getStatus().name() sẽ là "TODO", ...)
//                if (task.getStatus() != null && task.getStatus().name().equals(status)) {
//                    filteredList.add(task);
//                }
//            }
//
//            // 8. Cập nhật UI
//            if (filteredList.isEmpty()) {
//                // Không có task nào cho tab này
//                emptyView.setText("No tasks in " + status);
//                emptyView.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.GONE);
//            } else {
//                // Có task -> đưa cho Adapter
//                emptyView.setVisibility(View.GONE);
//                recyclerView.setVisibility(View.VISIBLE);
//                taskAdapter.submitList(filteredList);
//            }
//        });
//    }
//}