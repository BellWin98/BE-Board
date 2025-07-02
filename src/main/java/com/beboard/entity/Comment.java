package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"post", "commenter", "parent", "children"})
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id", nullable = false)
    private User commenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Column(nullable = false)
    private boolean deleted = false;

    /**
     * 댓글 생성자
     * @param content 댓글 내용
     * @param post 댓글이 속한 게시글
     * @param commenter 댓글 작성자
     * @param parent 부모 댓글 (답글인 경우)
     */
    @Builder
    public Comment(String content, Post post, User commenter, Comment parent) {
        this.content = content;
        this.post = post;
        this.commenter = commenter;
        this.parent = parent;

        // 부모 댓글이 있는 경우 자식 댓글로 추가
        if (parent != null) {
            parent.getChildren().add(this);
        }
    }

    /**
     * 댓글 내용 업데이트
     * @param content 업데이트할 내용
     */
    public void updateContent(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    /**
     * 댓글 삭제 처리 (소프트 삭제)
     * 자식 댓글이 있는 경우 내용만 삭제 표시, 없는 경우 완전 삭제
     */
    public void markAsDeleted() {
        this.deleted = true;

        // 자식 댓글이 없고 부모가 있는 경우 부모에서 제거
        if (children.isEmpty() && parent != null) {
            parent.getChildren().remove(this);
        }
    }

    /**
     * 자식 댓글 추가
     * @param child 추가할 자식 댓글
     */
    public void addChild(Comment child) {
        this.children.add(child);
        child.setParent(this);
    }

    /**
     * 자식 댓글 제거
     * @param child 제거할 자식 댓글
     */
    public void removeChild(Comment child) {
        this.children.remove(child);
        child.setParent(null);
    }

    /**
     * 게시글 설정
     * @param post 설정할 게시글
     */
    public void setPost(Post post) {
        this.post = post;
    }

    /**
     * 부모 댓글 설정
     * @param parent 설정할 부모 댓글
     */
    public void setParent(Comment parent) {
        this.parent = parent;
    }

    /**
     * 댓글 작성자인지 확인
     * @param userId 확인할 사용자 ID
     * @return 작성자 여부
     */
    public boolean isAuthor(Long userId) {
        return this.commenter.getId().equals(userId);
    }
}


