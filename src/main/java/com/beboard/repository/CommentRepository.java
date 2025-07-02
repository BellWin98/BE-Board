package com.beboard.repository;

import com.beboard.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 게시글별 상위 댓글 목록 조회 (삭제되지 않은 댓글만)
     * @param postId 게시글 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL AND c.deleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findByPostIdAndParentIsNullAndNotDeleted(@Param("postId") Long postId, Pageable pageable);

    /**
     * 게시글별 모든 댓글 목록 조회
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    List<Comment> findByPostId(Long postId);

    /**
     * 사용자가 작성한 댓글 목록 조회
     * @param commenterId 작성자 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    @Query("SELECT c FROM Comment c WHERE c.commenter.id = :commenterId AND c.deleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findByCommenterIdAndNotDeleted(@Param("commenterId") Long commenterId, Pageable pageable);

    /**
     * 특정 게시글의 댓글 수 조회
     * @param postId 게시글 ID
     * @return 댓글 수
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.deleted = false")
    long countByPostIdAndNotDeleted(@Param("postId") Long postId);

    /**
     * 최근 댓글 수 조회
     * @param since 기준 시간
     * @return 최근 댓글 수
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.createdAt >= :since AND c.deleted = false")
    long countCommentsSince(@Param("since") LocalDateTime since);
}
