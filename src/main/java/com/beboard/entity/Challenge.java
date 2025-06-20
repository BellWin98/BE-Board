
package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenges", indexes = {
        @Index(name = "idx_challenge_status", columnList = "status"),
        @Index(name = "idx_challenge_category", columnList = "category"),
        @Index(name = "idx_challenge_creator", columnList = "creatorId"),
        @Index(name = "idx_challenge_dates", columnList = "startDate, endDate")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"participants"})
public class Challenge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal goalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal betAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status = ChallengeStatus.RECRUITING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationMethod verificationMethod;

    @Column(nullable = false)
    private Integer maxParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeParticipant> participants = new ArrayList<>();

    @Builder
    public Challenge(String title, String description, String category, 
                    BigDecimal goalAmount, BigDecimal betAmount, 
                    LocalDate startDate, LocalDate endDate,
                    VerificationMethod verificationMethod, Integer maxParticipants, 
                    User creator) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.goalAmount = goalAmount;
        this.betAmount = betAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.verificationMethod = verificationMethod;
        this.maxParticipants = maxParticipants;
        this.creator = creator;
    }

    // 비즈니스 메서드
    public BigDecimal getTotalPot() {
        return participants.stream()
                .map(ChallengeParticipant::getBetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public double getSuccessRate() {
        if (participants.isEmpty()) return 0.0;
        
        long successCount = participants.stream()
                .filter(p -> p.getStatus() == ParticipantStatus.SUCCESS)
                .count();
        
        return (double) successCount / participants.size() * 100.0;
    }

    public boolean canJoin() {
        return status == ChallengeStatus.RECRUITING && 
               participants.size() < maxParticipants &&
               startDate.isAfter(LocalDate.now());
    }

    public void startChallenge() {
        if (status == ChallengeStatus.RECRUITING && 
            !startDate.isAfter(LocalDate.now())) {
            this.status = ChallengeStatus.IN_PROGRESS;
        }
    }

    public void completeChallenge() {
        this.status = ChallengeStatus.COMPLETED;
    }

    public void cancelChallenge() {
        this.status = ChallengeStatus.CANCELLED;
    }
}
