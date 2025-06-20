package com.beboard.controller;

import com.beboard.dto.CommentDto;
import com.beboard.dto.PostDto;
import com.beboard.entity.User;
import com.beboard.service.CommentService;
import com.beboard.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<Page<PostDto.ListResponse>> getPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String search){

        log.info("게시글 목록 조회 요청 - 페이지: {}, 크기: {}, 카테고리: {}, 정렬: {}, 검색: {}",
                pageable.getPageNumber(), pageable.getPageSize(), categoryId, sort, search);
        Page<PostDto.ListResponse> posts = postService.getPosts(pageable, categoryId, sort, search);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.DetailResponse> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        log.info("게시글 상세 조회 요청 - ID: {}, 사용자: {}", id, currentUser != null ? currentUser.getNickname() : "anonymous");
        Long userId = currentUser != null ? currentUser.getId() : null;
        PostDto.DetailResponse post = postService.getPost(id, userId);

        return ResponseEntity.ok(post);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDto.DetailResponse> createPost(
            @Valid @RequestBody PostDto.Request request,
            @AuthenticationPrincipal User currentUser) {

        log.info("게시글 작성 요청 - 사용자: {}, 제목: {}",
                currentUser.getNickname(), request.getTitle());
        PostDto.DetailResponse post = postService.createPost(request, currentUser.getId());

        return ResponseEntity.ok(post);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDto.DetailResponse> updatePost(
        @PathVariable Long id,
        @Valid @RequestBody PostDto.Request request,
        @AuthenticationPrincipal User currentUser) {

        log.info("게시글 수정 요청 - ID: {}, 사용자: {}", id, currentUser.getNickname());
        PostDto.DetailResponse post = postService.updatePost(id, request, currentUser.getId());

        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("게시글 삭제 요청 - ID: {}, 사용자: {}", id, currentUser.getNickname());
        postService.deletePost(id, currentUser.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<Void> incrementViews(@PathVariable Long id) {
        postService.incrementViews(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PostDto.ListResponse>> getMyPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @AuthenticationPrincipal User currentUser) {
        log.info("내 게시글 목록 조회 요청 - 사용자: {}", currentUser.getNickname());
        Page<PostDto.ListResponse> posts = postService.getPostsByAuthor(currentUser.getId(), sort, pageable);

        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{postId}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> addBookmark(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser) {
        log.info("게시글 북마크 추가 요청 - 게시글ID: {}, 사용자: {}", postId, currentUser.getNickname());

        return ResponseEntity.ok(postService.addBookmark(postId, currentUser.getId()));
    }

    @DeleteMapping("/{postId}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> removeBookmark(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser) {
        log.info("게시글 북마크 제거 요청 - 게시글ID: {}, 사용자: {}", postId, currentUser.getNickname());

        return ResponseEntity.ok(postService.removeBookmark(postId, currentUser.getId()));
    }

    @GetMapping("/bookmarks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PostDto.ListResponse>> getBookmarkedPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @AuthenticationPrincipal User currentUser) {
        log.info("북마크 게시글 목록 조회 요청 - 사용자: {}", currentUser.getNickname());
        Page<PostDto.ListResponse> bookmarkedPosts = postService.getBookmarkedPosts(currentUser.getId(), sort, pageable);

        return ResponseEntity.ok(bookmarkedPosts);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentDto.Response>> getCommentsByPostId(
            @PathVariable Long postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("게시글 댓글 목록 조회 요청 - 게시글 ID: {}, 페이지: {}", postId, pageable.getPageNumber());
        Page<CommentDto.Response> comments = commentService.getCommentsByPostId(postId, pageable);

        return ResponseEntity.ok(comments);
    }
}
