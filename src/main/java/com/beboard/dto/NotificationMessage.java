package com.beboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String content; // 알림 내용
    private String type;    // 알림 타입 (예: ORDER, CHAT)
}
