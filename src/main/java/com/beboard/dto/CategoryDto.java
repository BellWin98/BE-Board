package com.beboard.dto;

import com.beboard.entity.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

public class CategoryDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String createdAt;
        private String updatedAt;

        public static Response from(Category category) {
            return Response.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .createdAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(category.getCreatedAt()))
                    .updatedAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(category.getUpdatedAt()))
                    .build();
        }
    }
}
