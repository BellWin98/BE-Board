package com.beboard.service;

import com.beboard.dto.CommentDto;
import com.beboard.entity.Comment;
import com.beboard.entity.Post;
import com.beboard.entity.User;
import com.beboard.repository.CommentRepository;
import com.beboard.repository.PostRepository;
import com.beboard.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Page<CommentDto.Response> getCommentsByPostId(Long postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
        }
        Page<Comment> comments = commentRepository.findByPostIdAndParentIsNullAndNotDeleted(postId, pageable);

        return comments.map(CommentDto.Response::from);
    }

    @Transactional
    public CommentDto.Response createComment(CommentDto.CreateRequest request, User author) {
        Post post = postRepository.findByIdAndNotDeleted(request.getPostId())
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. ID: " + request.getPostId()));
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new NoSuchElementException("부모 댓글을 찾을 수 없습니다. ID: " + request.getParentId()));

            // 부모 댓글이 같은 게시글에 속하는지 확인
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new IllegalArgumentException("부모 댓글이 다른 게시글에 속해 있습니다");
            }
        }
        Comment createdComment = Comment.builder()
                .content(request.getContent())
                .parent(parent)
                .author(author)
                .post(post)
                .build();
        Comment savedComment = commentRepository.save(createdComment);
        log.info("댓글 작성 완료 - ID: {}, 작성자: {}, 게시글: {}",
                savedComment.getId(), author.getNickname(), post.getId());

        return CommentDto.Response.from(savedComment);
    }

    public Page<CommentDto.Response> getMyComments(Pageable pageable, Long userId) {
        Page<Comment> myComments = commentRepository.findByAuthorIdAndNotDeleted(userId, pageable);

        return myComments.map(CommentDto.Response::from);
    }

    @Transactional
    public CommentDto.Response updateComment(Long commentId, CommentDto.UpdateRequest request, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. ID: " + commentId));
        if (!comment.isAuthor(currentUser.getId())) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }
        if (comment.isDeleted()) {
            throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다");
        }
        comment.updateContent(request.getContent());
        log.info("댓글 수정 완료 - ID: {}, 작성자: {}", commentId, currentUser.getNickname());

        return CommentDto.Response.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. ID: " + commentId));
        if (!comment.isAuthor(currentUser.getId())) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        comment.markAsDeleted();
        log.info("댓글 삭제 완료 - ID: {}, 삭제자: {}", commentId, currentUser.getNickname());
    }
}
