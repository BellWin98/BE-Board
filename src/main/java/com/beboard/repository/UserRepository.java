package com.beboard.repository;

import com.beboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     * @param email 조회할 이메일
     * @return 사용자 Optional
     */
    Optional<User> findByEmail(String email);

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
}
