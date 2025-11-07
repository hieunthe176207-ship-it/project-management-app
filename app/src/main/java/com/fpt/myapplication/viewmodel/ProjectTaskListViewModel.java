package com.fpt.myapplication.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.fpt.myapplication.api.TaskApi;
import com.fpt.myapplication.config.ApiClient;
import com.fpt.myapplication.dto.request.CreateTaskRequest;
import com.fpt.myapplication.dto.response.TaskResponse;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectTaskListViewModel extends AndroidViewModel {
    private final TaskApi taskApi;
    private final MutableLiveData<List<TaskResponse>> _tasks = new MutableLiveData<>();
    public LiveData<List<TaskResponse>> tasks = _tasks;
    private final MutableLiveData<String> _error = new MutableLiveData<>(null);
    public LiveData<String> error = _error;

    public ProjectTaskListViewModel(@NonNull Application app) {
        super(app);
        taskApi = ApiClient.getRetrofit(app).create(TaskApi.class);
    }

    public void fetchForProject(int projectId) {
        taskApi.getTasksByProject(projectId).enqueue(new Callback<List<TaskResponse>>() {
            @Override public void onResponse(@NonNull Call<List<TaskResponse>> call, @NonNull Response<List<TaskResponse>> response) {
                if (response.isSuccessful()) {
                    List<TaskResponse> data = response.body();
                    if (data == null || data.isEmpty()) {
                        _tasks.postValue(generateMock(projectId));
                    } else {
                        _tasks.postValue(data);
                    }
                    _error.postValue(null);
                } else {
                    _tasks.postValue(generateMock(projectId));

                }
            }
            @Override public void onFailure(@NonNull Call<List<TaskResponse>> call, @NonNull Throwable t) {
                _tasks.postValue(generateMock(projectId));
            }
        });
    }

    public void createTask(String title, String desc, String dueDate, int projectId) {
        CreateTaskRequest req = new CreateTaskRequest(title, desc, dueDate, projectId, Collections.emptyList());
        taskApi.createTask(req).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchForProject(projectId); // refresh list sau khi tạo
                } else {
                    _error.postValue("Create task failed: HTTP " + response.code());
                    Log.w("ProjectTaskVM", "create task failed code=" + response.code());
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                _error.postValue("Network error: " + t.getMessage());
                Log.e("ProjectTaskVM", "create onFailure", t);
            }
        });
    }

    private List<TaskResponse> generateMock(int projectId) {
        // TODO: XÓA HÀM NÀY khi backend /task/project/{id} hoạt động.
        List<TaskResponse> list = new java.util.ArrayList<>();
        addMock(list, projectId, 1001, "Create Design Thinking", com.fpt.myapplication.constant.TaskStatus.TODO);
        addMock(list, projectId, 1002, "Create Wireframe", com.fpt.myapplication.constant.TaskStatus.IN_PROGRESS);
        addMock(list, projectId, 1003, "Write Requirement Doc", com.fpt.myapplication.constant.TaskStatus.IN_REVIEW);
        addMock(list, projectId, 1004, "Final Presentation", com.fpt.myapplication.constant.TaskStatus.DONE);
        return list;
    }

    private void addMock(List<TaskResponse> out, int projectId, int taskId, String title, com.fpt.myapplication.constant.TaskStatus status) {
        TaskResponse task = new TaskResponse();
        try {
            java.lang.reflect.Field fid = TaskResponse.class.getDeclaredField("id"); fid.setAccessible(true); fid.set(task, taskId);
            java.lang.reflect.Field ftitle = TaskResponse.class.getDeclaredField("title"); ftitle.setAccessible(true); ftitle.set(task, title);
            java.lang.reflect.Field fstatus = TaskResponse.class.getDeclaredField("status"); fstatus.setAccessible(true); fstatus.set(task, status);
            com.fpt.myapplication.dto.response.TaskProjectResponse project = new com.fpt.myapplication.dto.response.TaskProjectResponse();
            java.lang.reflect.Field pid = com.fpt.myapplication.dto.response.TaskProjectResponse.class.getDeclaredField("id"); pid.setAccessible(true); pid.set(project, projectId);
            java.lang.reflect.Field pname = com.fpt.myapplication.dto.response.TaskProjectResponse.class.getDeclaredField("name"); pname.setAccessible(true); pname.set(project, "Project " + projectId);
            java.lang.reflect.Field fproj = TaskResponse.class.getDeclaredField("project"); fproj.setAccessible(true); fproj.set(task, project);
            com.fpt.myapplication.dto.response.UserResponse owner = new com.fpt.myapplication.dto.response.UserResponse(taskId, "User " + taskId, "user"+taskId+"@mail.com", null, null, "USER");
            java.lang.reflect.Field fcreated = TaskResponse.class.getDeclaredField("createdBy"); fcreated.setAccessible(true); fcreated.set(task, owner);
        } catch (Exception ignored) {}
        out.add(task);
    }
}
