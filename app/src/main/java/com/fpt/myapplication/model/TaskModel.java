package com.fpt.myapplication.model;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskModel {
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

    // Constructors

}
