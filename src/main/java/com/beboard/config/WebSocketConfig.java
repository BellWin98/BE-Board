package com.beboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // STOMP 사용을 위한 어노테이션 선언
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 클라이언트가 WebSocket 연결을 시작할 엔드포인트 설정
        // "/ws" 라는 경로로 SockJS를 통해 연결할 수 있도록 허용
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // // CORS 문제를 해결하기 위해 모든 출처를 허용
                .withSockJS(); // SockJS는 WebSocket을 지원하지 않는 브라우저를 위한 대체 옵션
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 메시지 브로커가 "/topic"으로 시작하는 경로를 구독하는 클라이언트에게 메시지를 전달하도록 설정
        registry.enableSimpleBroker("/topic");

        // 클라이언트가 서버로 메시지를 보낼 때 사용하는 경로의 접두사를 "/app"으로 설정
        // 예를 들어, 클라이언트는 "/app/notify"로 메시지를 보내고,
        // 서버의 컨트롤러는 @MessageMapping("/notify")로 이 메시지를 받음
        registry.setApplicationDestinationPrefixes("/app");
    }
}
