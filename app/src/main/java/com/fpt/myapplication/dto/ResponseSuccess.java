package com.fpt.myapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ResponseSuccess<T>{
    private int code;
    private String message;
    private T data;
}
