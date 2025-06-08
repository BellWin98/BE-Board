package com.beboard.dto;

import com.beboard.entity.Category;
import com.beboard.entity.Post;
import com.beboard.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostDto {

    @Getter
    @Builder
    public static class ListResponse {
        private Long id;
        private Long authorId;
        private String title;
        private int viewCount;
        private int commentCount;
        private UserDto.Response author;
        private CategoryDto.Response category;
        private String createdAt;

        public static ListResponse from(Post post) {
            User author = post.getAuthor();
            Category category = post.getCategory();
            return ListResponse.builder()
                    .id(post.getId())
                    .authorId(author.getId())
                    .title(post.getTitle())
                    .viewCount(post.getViewCount())
                    .commentCount(post.getCommentCount())
                    .author(UserDto.Response.from(author))
                    .category(CategoryDto.Response.from(category))
                    .createdAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(post.getCreatedAt()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long id;
        private Long categoryId;
        private Long authorId;
        private String title;
        private String content;
        private int viewCount;
        private int commentCount;
        private boolean bookmarked;
        private CategoryDto.Response category;
        private UserDto.Response author;
        private List<CommentDto> comments;
        private String createdAt;
        private String updatedAt;

        public static DetailResponse from(Post post, boolean bookmarked) {
            Category category = post.getCategory();
            User author = post.getAuthor();

            return DetailResponse.builder()
                    .id(post.getId())
                    .categoryId(category.getId())
                    .authorId(author.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .viewCount(post.getViewCount())
                    .commentCount(post.getCommentCount())
                    .bookmarked(bookmarked)
                    .category(CategoryDto.Response.from(category))
                    .author(UserDto.Response.from(author))
                    .createdAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(post.getCreatedAt()))
                    .updatedAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(post.getUpdatedAt()))
                    .build();
        }
    }

    @Data
    public static class Request {
        @NotBlank(message = "제목은 필수입니다")
        @Size(min = 1, max = 200, message = "제목은 1-200자 사이여야 합니다")
        private String title;

        @NotBlank(message = "내용은 필수입니다")
        @Size(min = 1, max = 10000, message = "내용은 1-10000자 사이여야 합니다")
        private String content;

        @NotNull(message = "카테고리는 필수입니다")
        private Long categoryId;
    }
}
