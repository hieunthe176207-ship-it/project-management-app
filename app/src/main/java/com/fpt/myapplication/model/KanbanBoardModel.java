package com.fpt.myapplication.model;


import com.fpt.myapplication.dto.response.UpdateTaskReponse;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class KanbanBoardModel {
    private Integer projectId;
    private String projectName;
    private List<UpdateTaskReponse> todoTasks;
    private List<UpdateTaskReponse> inProgressTasks;
    private List<UpdateTaskReponse> inReviewTasks;
    private List<UpdateTaskReponse> doneTasks;

    // Constructors
}
