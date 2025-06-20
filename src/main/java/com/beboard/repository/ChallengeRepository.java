
package com.beboard.repository;

import com.beboard.entity.Challenge;
import com.beboard.entity.ChallengeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // 내가 생성한 챌린지 조회
    Page<Challenge> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);

    // 내가 참여한 챌린지 조회
    @Query("SELECT DISTINCT c FROM Challenge c JOIN c.participants p WHERE p.user.id = :userId ORDER BY c.createdAt DESC")
    Page<Challenge> findByParticipantUserId(@Param("userId") Long userId, Pageable pageable);

    // 카테고리별 챌린지 조회
    Page<Challenge> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);

    // 상태별 챌린지 조회
    Page<Challenge> findByStatusOrderByCreatedAtDesc(ChallengeStatus status, Pageable pageable);

    // 카테고리와 상태로 챌린지 조회
    Page<Challenge> findByCategoryAndStatusOrderByCreatedAtDesc(String category, ChallengeStatus status, Pageable pageable);

    // 시작 예정인 챌린지들 (자동 시작을 위함)
    @Query("SELECT c FROM Challenge c WHERE c.status = 'RECRUITING' AND c.startDate <= :date")
    List<Challenge> findChallengesToStart(@Param("date") LocalDate date);

    // 종료 예정인 챌린지들 (자동 완료를 위함)
    @Query("SELECT c FROM Challenge c WHERE c.status = 'IN_PROGRESS' AND c.endDate < :date")
    List<Challenge> findChallengesToComplete(@Param("date") LocalDate date);
}
