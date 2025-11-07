package com.fpt.myapplication.dto.response;

import com.fpt.myapplication.constant.TaskStatus;
import com.google.gson.annotations.SerializedName;
import java.util.Set;
public class TaskResponse {
    @SerializedName("id")
    private Integer id;

    @SerializedName("title")
    private String title;

    @SerializedName("status")
    private TaskStatus status;

    @SerializedName("project")
    private TaskProjectResponse project;

    @SerializedName("createdBy")
    private UserResponse createdBy;

    @SerializedName("assignees")
    private Set<UserResponse> assignees;

    public Integer getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public TaskStatus getStatus() {
        return status;
    }
    public TaskProjectResponse getProject() {
        return project;
    }
    public UserResponse getCreatedBy() {
        return createdBy;
    }
    public Set<UserResponse> getAssignees() {
        return assignees;
    }
}
