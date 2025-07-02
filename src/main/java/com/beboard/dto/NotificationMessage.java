package com.beboard.dto;

import lombok.*;

@Getter
@Builder
public class NotificationMessage {
    private Long recipientId; // 알림을 받을 사용자의 ID
    private String content; // 알림 내용
    private String url;     // 클릭 시 이동할 URL
    private String type;    // 알림 타입
}
