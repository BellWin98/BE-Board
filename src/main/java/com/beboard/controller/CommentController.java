package com.beboard.controller;

import com.beboard.dto.CommentDto;
import com.beboard.entity.User;
import com.beboard.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto.Response> createComment(
            @Valid @RequestBody CommentDto.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("댓글 작성 요청 - 게시글 ID: {}, 부모 댓글 ID: {}, 작성자: {}",
                request.getPostId(), request.getParentId(), currentUser.getNickname());

        CommentDto.Response comment = commentService.createComment(request, currentUser);

        return ResponseEntity.ok(comment);
    }

    @GetMapping("/me")
    public ResponseEntity<Page<CommentDto.Response>> getMyComments(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        log.info("내 댓글 목록 조회 요청 - 사용자: {}", currentUser.getNickname());
        Page<CommentDto.Response> myComments = commentService.getMyComments(pageable, currentUser.getId());

        return ResponseEntity.ok(myComments);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto.Response> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDto.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("댓글 수정 요청 - 댓글 ID: {}, 작성자: {}", commentId, currentUser.getNickname());
        CommentDto.Response response = commentService.updateComment(commentId, request, currentUser);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {
        log.info("댓글 삭제 요청 - 댓글 ID: {}, 작성자: {}", commentId, currentUser.getNickname());
        commentService.deleteComment(commentId, currentUser);

        return ResponseEntity.noContent().build();
    }
}
