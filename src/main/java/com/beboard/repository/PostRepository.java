package com.beboard.repository;

import com.beboard.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 게시글 ID로 조회 (삭제되지 않은 게시글만)
     * @param id 게시글 ID
     * @return 게시글 Optional
     */
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deleted = false")
    Optional<Post> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * 카테고리별 게시글 목록 조회 (삭제되지 않은 게시글만)
     * @param categoryId 카테고리 ID
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND p.deleted = false")
    Page<Post> findByCategoryIdAndNotDeleted(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 검색어로 게시글 목록 조회 (삭제되지 않은 게시글만)
     * @param searchTerm 검색어 (제목, 내용, 작성자 이름)
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:searchTerm% OR p.content LIKE %:searchTerm% OR p.author.nickname LIKE %:searchTerm%) AND p.deleted = false")
    Page<Post> searchPosts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * 카테고리별 & 검색어로 게시글 목록 조회 (삭제되지 않은 게시글만)
     * @param categoryId 카테고리 ID
     * @param searchTerm 검색어 (제목, 내용, 작성자 이름)
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND (p.title LIKE %:searchTerm% OR p.content LIKE %:searchTerm% OR p.author.nickname LIKE %:searchTerm%) AND p.deleted = false")
    Page<Post> searchPostsByCategory(@Param("categoryId") Long categoryId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * 사용자가 작성한 게시글 목록 조회 (삭제되지 않은 게시글만)
     * @param authorId 작성자 ID
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Query("SELECT p FROM Post p WHERE p.author.id = :authorId AND p.deleted = false")
    Page<Post> findByAuthorIdAndNotDeleted(@Param("authorId") Long authorId, Pageable pageable);

    /**
     * 북마크된 게시글 목록 조회 (삭제되지 않은 게시글만)
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Query("SELECT p FROM Post p JOIN Bookmark b ON p.id = b.post.id WHERE b.user.id = :userId AND p.deleted = false")
    Page<Post> findBookmarkedByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    /**
     * 게시글에 북마크 여부 확인
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return 북마크 여부
     */
    @Query("SELECT COUNT(b) > 0 FROM Bookmark b WHERE b.post.id = :postId AND b.user.id = :userId")
    boolean isBookmarkedByUser(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 최근 게시글 수 조회 (삭제되지 않은 게시글만)
     * @param since 기준 시간
     * @return 최근 게시글 수
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :since AND p.deleted = false")
    long countPostsSince(@Param("since") LocalDateTime since);

    /**
     * 인기 게시글 목록 조회 (조회수 기준, 삭제되지 않은 게시글만)
     * @param pageable 조회할 게시글 수
     * @return 인기 게시글 목록
     */
    @Query("SELECT p FROM Post p WHERE p.deleted = false ORDER BY p.viewCount DESC, p.createdAt DESC")
    List<Post> findPopularPosts(Pageable pageable);
}
