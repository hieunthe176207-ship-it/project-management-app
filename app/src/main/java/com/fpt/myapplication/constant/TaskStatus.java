package com.fpt.myapplication.constant;

import com.google.gson.annotations.SerializedName;
public enum TaskStatus {
    @SerializedName("TODO")
    TODO,

    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,

    @SerializedName("DONE")
    DONE,

    @SerializedName("IN_REVIEW")
    IN_REVIEW
}
