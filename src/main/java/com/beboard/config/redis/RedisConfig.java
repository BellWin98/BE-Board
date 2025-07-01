package com.beboard.config.redis;

import com.beboard.service.NotificationSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 캐시 설정 클래스
 * <p>
 * 카테고리 관련 데이터를 Redis에 캐싱하여 성능 향상
 * 각 캐시 영역별로 TTL을 다르게 설정하여 최적화
 * <p>
 * 캐시전략:
 * - categories: 10분 (자주 조회되는 카테고리 목록)
 * - category: 30분 (개별 카테고리 상세 정보)
 * - categoryPostCount: 5분 (변경 빈도가 높은 게시글 수)
 * - categoryStatistics: 1시간 (관리자 통계, 상대적으로 덜 중요)
 */
@Configuration
@EnableRedisRepositories
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

/*    @Value("${spring.data.redis.password}")
    private String redisPassword;*/

/*    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }*/

    // Redis에 메시지를 발행(publish)하거나 키-값을 저장하는 데 사용될 RedisTemplate 설정
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key는 String, Value는 JSON형태로 직렬화함
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return redisTemplate;
    }

/*    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(redisPort);
        redisStandaloneConfiguration.setPassword(redisPassword);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }*/

    @Bean
    public RedisMessageListenerContainer redisMessageListener(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            ChannelTopic channelTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 특정 토픽(채널)에 대한 리스너 추가
        container.addMessageListener(listenerAdapter, channelTopic);

        return container;
    }

    // 실제 메시지를 처리할 Subscriber 클래스를 리스너로 등록
    @Bean
    public MessageListenerAdapter listenerAdapter(NotificationSubscriber subscriber) {
        // NotificationSubscriber의 onMessage 메서드가 메시지를 처리하도록 설정
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    // 사용할 Redis 채널(토픽)을 빈으로 등록
    // 여기서는 "notifications" 라는 이름의 채널을 사용
    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("notifications");
    }
}
