package com.fpt.myapplication.dto.response;

import com.fpt.myapplication.dto.response.SubTaskResponse;
import com.fpt.myapplication.dto.response.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TaskDetailResponse {
    Integer id;
    String title;
    String description;
    String dueDate;
    String status;
    List<SubTaskResponse> subTasks;
    List<UserResponse> assignees;
    Integer projectId;
}
