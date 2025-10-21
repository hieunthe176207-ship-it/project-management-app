package com.fpt.myapplication.dto;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

public class ResponseError {
    @SerializedName("code") public int code;
    @SerializedName("message") public String message;
    public @Nullable String raw;

    public ResponseError(int code, String message) {
        this.code = code;
        this.message = message;
        this.raw = message;
    }
}
