package com.fpt.myapplication.dto.response;

import com.fpt.myapplication.model.TaskStatus;
import com.fpt.myapplication.model.User;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateTaskReponse {
    private Long id;
    private String title;
    private String description;
    private String dueDate;
    private TaskStatus status;
    private Long projectId;
    private User createdBy; // Đổi từ UserModel thành User
    private List<User> assignees; // Đổi từ UserModel thành User
    private String createdAt;
    private String updatedAt;
}
