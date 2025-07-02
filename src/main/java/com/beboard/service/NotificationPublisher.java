package com.beboard.service;

import com.beboard.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    // Redis 채널(토픽)로 메시지를 발행하는 메서드
    public void sendNotification(NotificationMessage message) {
        // 지정된 채널(토픽)로 메시지 발행
        // RedisConfig 에서 설정한 직렬화 방식(Jackson2JsonRedisSerializer)에 따라 메시지가 JSON 문자열로 변환되어 저장됨
        String topic = channelTopic.getTopic();
        redisTemplate.convertAndSend(topic, message);
    }
}

