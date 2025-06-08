package com.beboard.repository;

import com.beboard.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * 사용자와 게시글로 북마크 조회
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 북마크 Optional
     */
    Optional<Bookmark> findByUserIdAndPostId(Long userId, Long postId);

    /**
     * 사용자가 게시글을 북마크했는지 확인
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 북마크 여부
     */
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    /**
     * 사용자의 북마크 수 조회
     * @param userId 사용자 ID
     * @return 북마크 수
     */
    long countByUserId(Long userId);

    /**
     * 게시글의 북마크 수 조회
     * @param postId 게시글 ID
     * @return 북마크 수
     */
    long countByPostId(Long postId);

    /**
     * 사용자의 북마크 삭제
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 삭제된 행 수
     */
    long deleteByUserIdAndPostId(Long userId, Long postId);

    /**
     * 게시글에 대한 모든 북마크 삭제
     * @param postId 게시글 ID
     * @return 삭제된 행 수
     */
    long deleteByPostId(Long postId);
}
