
package com.beboard.repository;

import com.beboard.entity.ChallengeProgress;
import com.beboard.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, Long> {

    // 특정 참가자의 진행상황 조회
    List<ChallengeProgress> findByParticipantIdOrderByDateDesc(Long participantId);

    // 특정 챌린지의 모든 진행상황 조회
    @Query("SELECT p FROM ChallengeProgress p WHERE p.participant.challenge.id = :challengeId ORDER BY p.date DESC")
    List<ChallengeProgress> findByChallengeIdOrderByDateDesc(@Param("challengeId") Long challengeId);

    // 특정 날짜의 진행상황 조회
    Optional<ChallengeProgress> findByParticipantIdAndDate(Long participantId, LocalDate date);

    // 검증 대기중인 진행상황들
    @Query("SELECT p FROM ChallengeProgress p WHERE p.participant.challenge.id = :challengeId AND p.verificationStatus = 'PENDING'")
    List<ChallengeProgress> findPendingVerificationByChallengeId(@Param("challengeId") Long challengeId);

    // 특정 사용자가 검증해야 할 진행상황들 (상호 검증의 경우)
    @Query("SELECT p FROM ChallengeProgress p WHERE p.participant.challenge.id IN " +
           "(SELECT cp.challenge.id FROM ChallengeParticipant cp WHERE cp.user.id = :userId) " +
           "AND p.participant.user.id != :userId AND p.verificationStatus = 'PENDING'")
    List<ChallengeProgress> findProgressToVerifyByUserId(@Param("userId") Long userId);
}
