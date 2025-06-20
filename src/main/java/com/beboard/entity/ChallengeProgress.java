
package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "challenge_progress", indexes = {
        @Index(name = "idx_progress_participant", columnList = "participantId"),
        @Index(name = "idx_progress_date", columnList = "date"),
        @Index(name = "idx_progress_verification", columnList = "verificationStatus")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"participant", "verifiedBy"})
public class ChallengeProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private ChallengeParticipant participant;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Boolean completed;

    @Column(columnDefinition = "TEXT")
    private String proof; // 인증 자료 (사진 URL 또는 텍스트)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String verificationComment;

    @Builder
    public ChallengeProgress(ChallengeParticipant participant, LocalDate date, 
                           Boolean completed, String proof) {
        this.participant = participant;
        this.date = date;
        this.completed = completed;
        this.proof = proof;
    }

    public void verify(User verifier, boolean verified, String comment) {
        this.verifiedBy = verifier;
        this.verificationStatus = verified ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED;
        this.verificationComment = comment;
    }
}
