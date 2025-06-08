package com.beboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class CommentDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long parentId;
        private Long postId;
        private Long authorId;
        private String content;
        private UserDto.Response author;
        private List<CommentDto> children;
        private String createdAt;
        private String updatedAt;
    }
}
