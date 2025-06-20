
package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friends", indexes = {
        @Index(name = "idx_friend_requester", columnList = "requesterId"),
        @Index(name = "idx_friend_addressee", columnList = "addresseeId"),
        @Index(name = "idx_friend_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"requester", "addressee"})
public class Friend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // 친구 요청을 보낸 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee; // 친구 요청을 받은 사용자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status = FriendStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String message; // 친구 요청 메시지

    @Builder
    public Friend(User requester, User addressee, String message) {
        this.requester = requester;
        this.addressee = addressee;
        this.message = message;
    }

    public void accept() {
        this.status = FriendStatus.ACCEPTED;
    }

    public void reject() {
        this.status = FriendStatus.REJECTED;
    }

    public boolean isRequester(Long userId) {
        return requester.getId().equals(userId);
    }

    public boolean isAddressee(Long userId) {
        return addressee.getId().equals(userId);
    }
}
