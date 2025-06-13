package com.beboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    // 사용자 관련 통계
    private Long totalUsers;
    private Long newUsersToday;
    private Long activeUsers;  // 최근 30분 내 활동
    private Long activeUsersDaily;  // 오늘 활동한 사용자

    // 콘텐츠 관련 통계
    private Long totalPosts;
    private Long newPostsToday;
    private Long totalComments;
    private Long newCommentsToday;

    // 시스템 건강도 지표
    private Double serverCpuUsage;
    private Double serverMemoryUsage;
    private Long databaseConnections;
    private Boolean redisStatus;

    // 인기 카테고리 (최근 24시간 기준)
    private List<PopularCategoryDto> popularCategories;

    // 최근 활동 (실시간)
    private List<RecentActivityDto> recentActivities;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularCategoryDto {
        private Long id;
        private String name;
        private Long postCount;
        private Long viewCount;
        private Double growthRate;  // 전일 대비 증가율
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityDto {
        private String type;  // POST, COMMENT, USER_REGISTER, LOGIN
        private String userNickname;
        private String action;
        private String targetTitle;  // 게시글 제목 등
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;
        private String ipAddress;  // 마스킹 처리된 IP
    }
}
