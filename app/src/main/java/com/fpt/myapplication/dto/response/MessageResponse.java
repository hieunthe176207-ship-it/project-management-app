package com.fpt.myapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    public Integer id;
    public Integer senderId;
    public String senderEmail;
    public String senderName;
    public String avatarUrl;
    public String content;
    private int groupId;
    public String timestamp;
}
