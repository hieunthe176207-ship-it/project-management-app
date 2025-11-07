package com.fpt.myapplication.dto.request;



import com.fpt.myapplication.model.TaskStatus;

public class TaskUpdateStatusRequestDto {
    private TaskStatus status;

    public TaskUpdateStatusRequestDto() {}

    public TaskUpdateStatusRequestDto(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
