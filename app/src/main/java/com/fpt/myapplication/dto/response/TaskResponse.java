package com.fpt.myapplication.dto.response;

import com.fpt.myapplication.constant.TaskStatus;
import com.google.gson.annotations.SerializedName;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    @SerializedName("id")
    private Integer id;

    private String description;

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

    private String dueDate;

}
