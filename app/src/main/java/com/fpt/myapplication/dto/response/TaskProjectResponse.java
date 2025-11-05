package com.fpt.myapplication.dto.response;

import com.google.gson.annotations.SerializedName;
public class TaskProjectResponse {
    @SerializedName("id")
    private Integer id; // Dùng Integer (như đã thống nhất)

    @SerializedName("name")
    private String name;

    // Getters
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
