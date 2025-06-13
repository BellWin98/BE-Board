package com.beboard.dto;

import com.beboard.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CategoryDto {

    @Getter
    @Builder
    public static class Response implements Serializable {
        private Long id;
        private String name;
        private String description;
        private Integer displayOrder;
        private Boolean active;
        private String createdAt;
        private String updatedAt;
        private Long postCount;

        public static Response from(Category category) {
            return Response.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .displayOrder(category.getDisplayOrder())
                    .active(category.isActive())
                    .createdAt(formatDateTime(category.getCreatedAt()))
                    .updatedAt(formatDateTime(category.getUpdatedAt()))
                    .build();
        }

        // Category 엔티티와 게시글 수를 포함한 Response DTO로 변환
        public static Response from(Category category, Long postCount) {
            return Response.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .displayOrder(category.getDisplayOrder())
                    .active(category.isActive())
                    .createdAt(formatDateTime(category.getCreatedAt()))
                    .updatedAt(formatDateTime(category.getUpdatedAt()))
                    .postCount(postCount)
                    .build();
        }

        private static String formatDateTime(LocalDateTime dateTime) {
            if (dateTime == null) return null;
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(dateTime);
        }
    }

    /**
     * 카테고리 생성 요청 DTO
     * 필수 필드에 대한 유효성 검사 어노테이션이 포함
     */
    @Data
    public static class CreateRequest {

        @NotBlank(message = "카테고리 이름은 필수입니다")
        @Size(min = 1, max = 50, message = "카테고리 이름은 1자 이상 50자 이하여야 합니다")
        private String name;

        @Size(max = 500, message = "카테고리 설명은 500자 이하여야 합니다")
        private String description;

        private Integer displayOrder;

        public Category toEntity() {
            return Category.builder()
                    .name(this.name.trim())
                    .description(this.description != null ? this.description.trim() : null)
                    .displayOrder(this.displayOrder)
                    .build();
        }
    }

    /**
     * 카테고리 수정 요청 DTO
     * 기존 카테고리를 수정할 때 클라이언트로부터 받을 데이터를 정의
     * 모든 필드가 선택사항이며, 제공된 필드만 업데이트
     */
    @Data
    public static class UpdateRequest {

        @NotBlank(message = "카테고리 이름은 필수입니다")
        @Size(min = 1, max = 50, message = "카테고리 이름은 1자 이상 50자 이하여야 합니다")
        private String name;

        @Size(max = 500, message = "카테고리 설명은 500자 이하여야 합니다")
        private String description;

        private Integer displayOrder;
        private Boolean active;
    }

    /**
     * 카테고리 통계 응답 DTO
     * 관리자 대시보드에서 카테고리 관련 통계를 제공할 때 사용
     */
    @Getter
    @Builder
    public static class StatisticsResponse {
        private Long totalCategories;
        private Long activeCategories;
        private Long inactiveCategories;
        private Long totalPosts;
        private List<CategoryPostCount> categoryPostCounts;
    }

    /**
     * 카테고리별 게시글 수 DTO
     */
    @Getter
    @Builder
    public static class CategoryPostCount {
        private Long categoryId;
        private String categoryName;
        private Long postCount;
    }

    /**
     * 카테고리 간단 정보 DTO
     * 게시글 작성 시 카테고리 선택을 위한 간단한 정보만 제공
     */
    @Getter
    @Builder
    public static class SimpleResponse {
        private Long id;
        private String name;
        private Integer displayOrder;

        public static SimpleResponse from(Category category) {
            return SimpleResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .displayOrder(category.getDisplayOrder())
                    .build();
        }
    }

    /**
     * 카테고리 목록 요청 DTO
     * 카테고리 목록을 조회할 때 필터링 및 정렬 옵션을 제공
     */
    @Data
    public static class ListRequest {
        private Boolean active;
        private String keyword;
        private String sortBy = "displayOrder";
        private String sortDirection = "ASC";
    }
}
