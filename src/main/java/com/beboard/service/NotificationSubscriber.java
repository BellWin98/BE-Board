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
//            messagingTemplate.convertAndSend("/topic/notifications", notificationMessage);
            sendNotificationToUser(notificationMessage);
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 특정 사용자에게 WebSocket을 통해 알림을 전송합니다.
     * STOMP 프로토콜의 개인 메시징 기능을 활용하여,
     * 오직 대상 사용자에게만 알림이 전달되도록 보장합니다.
     *
     * 경로 구조 설명:
     * - "/user/{userId}/notifications"
     * - STOMP는 이를 자동으로 해당 사용자의 세션으로 라우팅
     * - 사용자가 여러 탭을 열어도 모든 탭에서 알림 수신 가능
     *
     */
    private void sendNotificationToUser(NotificationMessage notificationMessage) {
        String recipientId = String.valueOf(notificationMessage.getRecipientId());
        messagingTemplate.convertAndSendToUser(
                recipientId,
                "/notifications", // 사용자가 구독할 경로
                notificationMessage
        );
        log.info("사용자에게 알림 전송 완료: (수신자 ID: {}, 내용: {})",
                recipientId, notificationMessage.getContent());
    }

    /**
     * 브로드캐스트 알림 전송 (모든 연결된 사용자에게)
     * 시스템 공지사항이나 전체 알림이 필요한 경우 사용할 수 있는 메서드입니다.
     * 현재 댓글 알림 시스템에서는 사용하지 않지만, 확장성을 위해 제공됩니다.
     */
    public void broadcastNotification(NotificationMessage notificationMessage) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", notificationMessage);
            log.info("전체 브로드캐스트 알림을 전송했습니다. 알림타입: {}", notificationMessage.getType());
        } catch (Exception e) {
            log.error("브로드캐스트 알림 전송 중 오류가 발생했습니다", e);
        }
    }
}
