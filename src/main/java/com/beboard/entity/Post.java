package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"author", "category", "comments"})
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(200)", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 댓글 수를 계산하는 데이터베이스 수식 (성능 최적화)
    @Formula("(SELECT COUNT(c.id) FROM comments c WHERE c.post_id = id)")
    private int commentCount;

    @Column(nullable = false)
    private boolean deleted = false;

    @Builder
    public Post(User author,  Category category, String title, String content) {
        this.author = author;
        this.category = category;
        this.title = title;
        this.content = content;
    }

    /**
     * 게시글 정보 업데이트
     * @param title 업데이트할 제목
     * @param content 업데이트할 내용
     * @param category 업데이트할 카테고리
     */
    public void update(String title, String content, Category category) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        if (category != null) {
            this.category = category;
        }
    }

    /**
     * 게시글 조회수 증가
     */
    public void incrementViews() {
        this.viewCount++;
    }

    /**
     * 게시글 삭제 처리 (소프트 삭제)
     */
    public void markAsDeleted() {
        this.deleted = true;
    }

    /**
     * 게시글 복구 처리
     */
    public void restore() {
        this.deleted = false;
    }

    /**
     * 댓글 추가
     * @param comment 추가할 댓글
     */
    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    /**
     * 댓글 삭제
     * @param comment 삭제할 댓글
     */
    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setPost(null);
    }

    /**
     * 게시글 작성자인지 확인
     * @param userId 확인할 사용자 ID
     * @return 작성자 여부
     */
    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }
}
