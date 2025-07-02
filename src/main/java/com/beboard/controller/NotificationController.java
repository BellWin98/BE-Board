package com.beboard.controller;

import com.beboard.dto.NotificationMessage;
import com.beboard.service.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationPublisher notificationPublisher;

    // 테스트용으로 알림 생성하고 Redis에 발행하는 API
    @PostMapping("/api/notifications")
    public String sendNotification(@RequestBody NotificationMessage message) {
        try {
            notificationPublisher.sendNotification(message);
            return "알림이 성공적으로 전송되었습니다.";
        } catch (Exception e) {
            // 실무에서는 좀 더 정교한 예외 처리가 필요합니다.
            return "알림 전송에 실패했습니다: " + e.getMessage();
        }
    }
}
