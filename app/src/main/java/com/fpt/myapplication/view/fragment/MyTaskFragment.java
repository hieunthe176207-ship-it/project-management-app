package com.fpt.myapplication.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.controller.TaskActivity;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.TaskResponseDto;
import com.fpt.myapplication.model.TaskModel;
import com.fpt.myapplication.view.adapter.SearchTaskAdapter;

import java.util.*;

public class MyTaskFragment extends Fragment {

    private Spinner spFilter;
    private RecyclerView rv;
    private TextView tvEmpty;
    private View progress;

    private SearchTaskAdapter adapter;

    private TaskModel taskModel;

    // Dữ liệu
    private final List<TaskResponseDto> allTasks = new ArrayList<>();
    private final List<TaskResponseDto> working  = new ArrayList<>();

    // ------ KHÔNG DÙNG strings.xml: labels (VN) & values (key server) ------
    private final List<String> filterLabels = Arrays.asList(
            "Tất cả",        // ""
            "Chờ làm",       // TODO
            "Đang làm",      // IN_PROGRESS
            "Chờ duyệt",     // IN_REVIEW
            "Hoàn thành"     // DONE
    );
    private final List<String> filterValues = Arrays.asList(
            "", "TODO", "IN_PROGRESS", "IN_REVIEW", "DONE"
    );
    // ----------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_task_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        taskModel = new TaskModel(requireContext());

        spFilter  = v.findViewById(R.id.spFilter);
        rv        = v.findViewById(R.id.rvTasks);
        tvEmpty   = v.findViewById(R.id.tvEmpty);
        progress  = v.findViewById(R.id.progress);

        // Adapter
        adapter = new SearchTaskAdapter(item -> {
            Intent i = new Intent(requireContext(), TaskActivity.class);
            if (item.getId() != null) i.putExtra("task_id", item.getId());
            startActivity(i);
        });
        SearchTaskAdapter.setupAsTwoColumns(rv);
        rv.setAdapter(adapter);

        // Spinner: tạo adapter trực tiếp từ filterLabels
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filterLabels
        );
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilter.setAdapter(spAdapter);

        spFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = (position >= 0 && position < filterValues.size()) ? filterValues.get(position) : "";
                applyFilter(value);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        fetchData();
    }



    private void fetchData(){
        taskModel.getMyTasks(new TaskModel.GetMyTasksCallBack() {
            @Override
            public void onSuccess(List<TaskResponseDto> data) {
                allTasks.clear();
                allTasks.addAll(data);
                applyFilter("");
                showLoading(false);
            }

            @Override
            public void onError(ResponseError error) {

            }

            @Override
            public void onLoading() {
                showLoading(true);
            }
        });
    }

    private void applyFilter(String key) {
        working.clear();

        if (TextUtils.isEmpty(key)) {
            working.addAll(allTasks);
        } else {
            for (TaskResponseDto t : allTasks) {
                if (key.equals(normalize(t.getStatus()))) working.add(t);
            }
        }

        adapter.submitList(new ArrayList<>(working));
        tvEmpty.setVisibility(working.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showLoading(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String normalize(String raw) {
        if (raw == null) return "";
        return raw.trim().toUpperCase(Locale.US).replace(' ', '_').replace('-', '_');
    }

    // DEMO helper
    private TaskResponseDto fake(Integer id, String title, String pj, String due, String status) {
        TaskResponseDto t = new TaskResponseDto();
        t.setId(id); t.setTitle(title); t.setProjectName(pj);
        t.setDueDate(due); t.setStatus(status);
        return t;
    }
}
