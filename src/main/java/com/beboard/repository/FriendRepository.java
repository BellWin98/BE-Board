
package com.beboard.repository;

import com.beboard.entity.Friend;
import com.beboard.entity.FriendStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 친구 목록 조회 (승인된 친구들만)
    @Query("SELECT f FROM Friend f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED' ORDER BY f.updatedAt DESC")
    Page<Friend> findAcceptedFriendsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 받은 친구 요청 목록
    Page<Friend> findByAddresseeIdAndStatusOrderByCreatedAtDesc(Long addresseeId, FriendStatus status, Pageable pageable);

    // 보낸 친구 요청 목록
    Page<Friend> findByRequesterIdAndStatusOrderByCreatedAtDesc(Long requesterId, FriendStatus status, Pageable pageable);

    // 두 사용자간의 친구 관계 확인
    @Query("SELECT f FROM Friend f WHERE ((f.requester.id = :userId1 AND f.addressee.id = :userId2) OR (f.requester.id = :userId2 AND f.addressee.id = :userId1))")
    Optional<Friend> findFriendRelation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 두 사용자가 친구인지 확인
    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE ((f.requester.id = :userId1 AND f.addressee.id = :userId2) OR (f.requester.id = :userId2 AND f.addressee.id = :userId1)) AND f.status = 'ACCEPTED'")
    boolean areFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 사용자의 친구 ID 목록 조회 (챌린지 초대 시 사용)
    @Query("SELECT CASE WHEN f.requester.id = :userId THEN f.addressee.id ELSE f.requester.id END FROM Friend f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED'")
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);
}
