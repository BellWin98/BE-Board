
package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenge_participants", indexes = {
        @Index(name = "idx_participant_challenge", columnList = "challengeId"),
        @Index(name = "idx_participant_user", columnList = "userId"),
        @Index(name = "idx_participant_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"challenge", "user", "progressList"})
public class ChallengeParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal betAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status = ParticipantStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeProgress> progressList = new ArrayList<>();

    @Builder
    public ChallengeParticipant(Challenge challenge, User user, BigDecimal betAmount) {
        this.challenge = challenge;
        this.user = user;
        this.betAmount = betAmount;
        this.joinedAt = LocalDateTime.now();
    }

    public void updateStatus(ParticipantStatus status) {
        this.status = status;
    }
}
