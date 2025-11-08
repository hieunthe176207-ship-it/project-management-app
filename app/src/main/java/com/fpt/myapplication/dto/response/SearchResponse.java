package com.fpt.myapplication.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchResponse {
    List<ProjectResponse> projects;
    List<TaskResponseDto> tasks;
}
