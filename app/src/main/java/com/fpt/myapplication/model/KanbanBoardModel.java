package com.fpt.myapplication.model;


import java.util.List;

public class KanbanBoardModel {
    private Integer projectId;
    private String projectName;
    private List<TaskModel> todoTasks;
    private List<TaskModel> inProgressTasks;
    private List<TaskModel> inReviewTasks;
    private List<TaskModel> doneTasks;

    // Constructors
    public KanbanBoardModel() {}

    public KanbanBoardModel(Integer projectId, String projectName,
                            List<TaskModel> todoTasks, List<TaskModel> inProgressTasks,
                            List<TaskModel> inReviewTasks, List<TaskModel> doneTasks) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.todoTasks = todoTasks;
        this.inProgressTasks = inProgressTasks;
        this.inReviewTasks = inReviewTasks;
        this.doneTasks = doneTasks;
    }

    // Getters and Setters
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public List<TaskModel> getTodoTasks() { return todoTasks; }
    public void setTodoTasks(List<TaskModel> todoTasks) { this.todoTasks = todoTasks; }

    public List<TaskModel> getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(List<TaskModel> inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public List<TaskModel> getInReviewTasks() { return inReviewTasks; }
    public void setInReviewTasks(List<TaskModel> inReviewTasks) { this.inReviewTasks = inReviewTasks; }

    public List<TaskModel> getDoneTasks() { return doneTasks; }
    public void setDoneTasks(List<TaskModel> doneTasks) { this.doneTasks = doneTasks; }
}
