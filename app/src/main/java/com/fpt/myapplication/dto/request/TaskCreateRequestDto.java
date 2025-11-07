package com.fpt.myapplication.dto.request;



import java.time.LocalDate;
import java.util.List;

public class TaskCreateRequestDto {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer projectId;
    private List<Long> assigneeIds;

    // Constructors
    public TaskCreateRequestDto() {}

    public TaskCreateRequestDto(String title, String description, LocalDate dueDate,
                                Integer projectId, List<Long> assigneeIds) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.projectId = projectId;
        this.assigneeIds = assigneeIds;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public List<Long> getAssigneeIds() { return assigneeIds; }
    public void setAssigneeIds(List<Long> assigneeIds) { this.assigneeIds = assigneeIds; }
}
