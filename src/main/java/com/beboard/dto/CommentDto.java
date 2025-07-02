package com.beboard.dto;

import com.beboard.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long parentId;
        private Long postId;
        private Long authorId;
        private String content;
        private Boolean deleted;
        private UserDto.Response author;
        private List<Response> children;
        private String createdAt;
        private String updatedAt;

        public static Response from(Comment comment) {
            return Response.builder()
                    .id(comment.getId())
                    .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                    .postId(comment.getPost().getId())
                    .authorId(comment.getCommenter().getId())
                    .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                    .deleted(comment.isDeleted())
                    .author(UserDto.Response.from(comment.getCommenter()))
                    .children(comment.getChildren().stream()
                            .map(Response::from)
                            .collect(Collectors.toList()))
                    .createdAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(comment.getCreatedAt()))
                    .updatedAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(comment.getUpdatedAt()))
                    .build();
        }
    }

    @Data
    public static class CreateRequest {
        @NotNull(message = "게시글 ID는 필수입니다")
        private Long postId;

        @NotBlank(message = "댓글 내용은 필수입니다")
        @Size(min = 1, max = 1000, message = "댓글은 1-1000자 사이여야 합니다")
        private String content;

        // 답글인 경우 부모 댓글 ID
        private Long parentId;
    }

    @Data
    public static class UpdateRequest {
        @NotBlank(message = "댓글 내용은 필수입니다")
        @Size(min = 1, max = 1000, message = "댓글은 1-1000자 사이여야 합니다")
        private String content;
    }
}
