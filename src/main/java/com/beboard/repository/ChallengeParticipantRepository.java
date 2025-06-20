
package com.beboard.repository;

import com.beboard.entity.ChallengeParticipant;
import com.beboard.entity.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {

    // 특정 챌린지의 참가자 조회
    List<ChallengeParticipant> findByChallengeIdOrderByJoinedAtAsc(Long challengeId);

    // 사용자가 특정 챌린지에 참여했는지 확인
    Optional<ChallengeParticipant> findByChallengeIdAndUserId(Long challengeId, Long userId);

    // 사용자가 특정 챌린지에 참여했는지 확인 (boolean)
    boolean existsByChallengeIdAndUserId(Long challengeId, Long userId);

    // 특정 챌린지의 활성 참가자 수
    @Query("SELECT COUNT(p) FROM ChallengeParticipant p WHERE p.challenge.id = :challengeId AND p.status = 'ACTIVE'")
    long countActiveByChallengeId(@Param("challengeId") Long challengeId);

    // 사용자의 참가 상태별 챌린지 참가 정보
    List<ChallengeParticipant> findByUserIdAndStatusOrderByJoinedAtDesc(Long userId, ParticipantStatus status);
}
