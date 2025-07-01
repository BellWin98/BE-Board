package com.beboard.service;

import com.beboard.dto.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    // Redis에서 메시지가 발행되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리
    public void onMessage(String publishedMessage) {
        try {
            // 받은 메시지를 NotificationMessage 객체로 변환
            NotificationMessage notificationMessage = objectMapper.readValue(publishedMessage, NotificationMessage.class);

            // "/topic/notifications"를 구독하고 있는 클라이언트에게 메시지를 보냄
            messagingTemplate.convertAndSend("/topic/notifications", notificationMessage);
            log.info("메시지 전송 완료: {}", notificationMessage.getContent());
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}
