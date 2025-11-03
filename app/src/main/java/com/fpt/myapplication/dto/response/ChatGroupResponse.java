package com.fpt.myapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroupResponse {
    private Integer id;
    private String name;
    private String avatar;
    private UserResponse lastUser;
    private String lastMessage;
}
