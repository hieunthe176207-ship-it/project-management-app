package com.fpt.myapplication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectCreateRequest {
    private String name;
    private String description;
    private String deadline;
    private int isPublic;
}

