package com.beboard.repository;

import com.beboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     * @param email 조회할 이메일
     * @return 사용자 Optional
     */
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     * 사용자명으로 사용자 조회
     * @param nickname 조회할 사용자명
     * @return 사용자 Optional
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 이메일 존재 여부 확인
     * @param email 확인할 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 사용자명 존재 여부 확인
     * @param nickname 확인할 사용자명
     * @return 존재 여부
     */
    boolean existsByNickname(String nickname);

    /**
     * 검색어로 사용자 검색
     * @param searchTerm 검색어 (이메일 또는 사용자명)
     * @param pageable 페이징 정보
     * @return 검색된 사용자 페이지
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:searchTerm% OR u.nickname LIKE %:searchTerm%")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * 활성 사용자 수 조회
     * @return 활성 사용자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();

    /**
     * 특정 시점 이후 가입한 신규 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since AND u.deleted = false")
    Long countByCreatedAtAfterSince(@Param("since")LocalDateTime since);

    /**
     * 활성 사용자 수 조회
     * 활성 사용자 정의:
     * 1. 최근 로그인 (기본 조건)
     * 2. 최근 활동 이력 (게시글, 댓글, 반응 등)
     * 3. 세션 유지 상태 (실시간 접속 여부)
     *
     * 성능 고려사항:
     * - 복합 인덱스: last_login_at, deleted, status
     * - 파티셔닝: 대용량 사용자 테이블의 경우 시간 기반 파티셔닝
     * - 캐시 전략: 자주 조회되는 시간 구간은 Redis에 결과 캐싱
     */
    @Query("""
            SELECT COUNT(DISTINCT u.id)
            FROM User u
            WHERE u.deleted = false
            AND u.status = 'ACTIVE'
            AND u.lastLoginAt >= :since
    """)
    Long countActiveUsersSince(@Param("since") LocalDateTime since);

    /**
     * 상세한 활성 사용자 목록 조회
     * 관리자가 실제 활성 사용자들이 누구인지 확인하고 싶을 때 사용합니다.
     * 단순한 통계를 넘어서 구체적인 사용자 정보와 활동 패턴을 제공합니다.
     */
    @Query("""
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN FETCH u.userProfile up
        WHERE u.deleted = false
        AND u.status = 'ACTIVE'
        AND u.lastLoginAt >= :since
    """)
    Page<User> findActiveUsersSince(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 휴면 사용자 식별
     * 커뮤니티 운영에서 휴면 사용자 관리는 매우 중요합니다.
     * 이들을 재활성화시키는 것은 신규 사용자 유치보다 비용 효율적이며,
     * 기존 사용자의 이탈 패턴을 분석하는 데도 활용됩니다.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.deleted = false
        AND u.status = 'ACTIVE'
        AND u.lastLoginAt < :threshold
        ORDER BY u.lastLoginAt ASC
    """)
    Page<User> findDormantUsers(@Param("threshold") LocalDateTime threshold, Pageable pageable);
}
