package com.beboard.service;

import com.beboard.dto.DashboardStatsDto;
import com.beboard.entity.*;
import com.beboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 대시보드 메인 통계 조회
     * 15분 마다 캐시 갱신, 실시간성이 중요한 일부 지표는
     * 별도의 Redis 스트림을 통해 실시간 업데이트
     */
    @Cacheable(value = "admin:dashboard:stats", unless = "#result == null")
    public DashboardStatsDto getDashboardStats() {
        log.info("관리자 대시보드 통계 조회 시작");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);

        // 기본 사용자 통계 (병렬 처리 최적화)
        Long totalUsers = userRepository.count();
        Long newUsersToday = userRepository.countByCreatedAtAfterSince(todayStart);
        Long activeUsers = getActiveUserCount(thirtyMinutesAgo);
        Long activeUserDaily = getActiveUserCount(todayStart);

        return null;

    }

    /**
     * 활성 사용자 수 조회 (Redis 기반 실시간 추적)
     */
    private Long getActiveUserCount(LocalDateTime since) {
        try {
            // Redis에서 활성 사용자 세션 수 조회
            return (Long) redisTemplate.opsForValue().get("admin:stats:active_users:" + since.toEpochSecond(ZoneOffset.UTC));
        } catch (Exception e) {
            log.warn("Redis에서 활성 사용자 수 조회 실패, 데이터베이스 조회로 fallback", e);
            return userRepository.countActiveUsersSince(since);
        }
    }
}
