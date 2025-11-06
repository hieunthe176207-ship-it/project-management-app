package com.fpt.myapplication.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private Integer id;
    private String name;
    private String description;
    private String deadline;
    private UserResponse createdBy;
    private int isPublic;
    private int countJoinRequest;
    private List<UserResponse> members;
}
