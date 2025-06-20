
package com.beboard.service;

import com.beboard.dto.FriendDto;
import com.beboard.dto.UserDto;
import com.beboard.entity.Friend;
import com.beboard.entity.FriendStatus;
import com.beboard.entity.User;
import com.beboard.repository.FriendRepository;
import com.beboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    /**
     * 친구 목록 조회
     */
    public Page<FriendDto.Response> getFriends(User user, Pageable pageable) {
        Page<Friend> friends = friendRepository.findAcceptedFriendsByUserId(user.getId(), pageable);
        return friends.map(FriendDto.Response::from);
    }

    /**
     * 친구 요청 보내기
     */
    @Transactional
    public FriendDto.Response sendFriendRequest(FriendDto.SendRequest request, User requester) {
        // 요청 받을 사용자 조회
        User addressee = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. 이메일: " + request.getEmail()));

        // 자기 자신에게 요청할 수 없음
        if (requester.getId().equals(addressee.getId())) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        // 이미 친구 관계가 있는지 확인
        Optional<Friend> existingRelation = friendRepository.findFriendRelation(requester.getId(), addressee.getId());
        if (existingRelation.isPresent()) {
            Friend existing = existingRelation.get();
            if (existing.getStatus() == FriendStatus.ACCEPTED) {
                throw new IllegalStateException("이미 친구 관계입니다.");
            } else if (existing.getStatus() == FriendStatus.PENDING) {
                throw new IllegalStateException("이미 친구 요청이 대기 중입니다.");
            }
        }

        Friend friendRequest = Friend.builder()
                .requester(requester)
                .addressee(addressee)
                .message(request.getMessage())
                .build();

        Friend savedRequest = friendRepository.save(friendRequest);

        log.info("친구 요청 전송 완료 - 요청자: {}, 수신자: {}", 
                requester.getNickname(), addressee.getNickname());

        return FriendDto.Response.from(savedRequest);
    }

    /**
     * 받은 친구 요청 목록
     */
    public Page<FriendDto.Response> getReceivedRequests(User user, Pageable pageable) {
        Page<Friend> requests = friendRepository.findByAddresseeIdAndStatusOrderByCreatedAtDesc(
                user.getId(), FriendStatus.PENDING, pageable);
        return requests.map(FriendDto.Response::from);
    }

    /**
     * 보낸 친구 요청 목록
     */
    public Page<FriendDto.Response> getSentRequests(User user, Pageable pageable) {
        Page<Friend> requests = friendRepository.findByRequesterIdAndStatusOrderByCreatedAtDesc(
                user.getId(), FriendStatus.PENDING, pageable);
        return requests.map(FriendDto.Response::from);
    }

    /**
     * 친구 요청 수락
     */
    @Transactional
    public FriendDto.Response acceptFriendRequest(Long requestId, User user) {
        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("친구 요청을 찾을 수 없습니다. ID: " + requestId));

        // 요청을 받은 사용자만 수락할 수 있음
        if (!friendRequest.isAddressee(user.getId())) {
            throw new IllegalStateException("친구 요청을 수락할 권한이 없습니다.");
        }

        // 이미 처리된 요청인지 확인
        if (friendRequest.getStatus() != FriendStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 친구 요청입니다.");
        }

        friendRequest.accept();
        Friend savedRequest = friendRepository.save(friendRequest);

        log.info("친구 요청 수락 완료 - 요청자: {}, 수락자: {}", 
                friendRequest.getRequester().getNickname(), user.getNickname());

        return FriendDto.Response.from(savedRequest);
    }

    /**
     * 친구 요청 거절
     */
    @Transactional
    public void rejectFriendRequest(Long requestId, User user) {
        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("친구 요청을 찾을 수 없습니다. ID: " + requestId));

        // 요청을 받은 사용자만 거절할 수 있음
        if (!friendRequest.isAddressee(user.getId())) {
            throw new IllegalStateException("친구 요청을 거절할 권한이 없습니다.");
        }

        // 이미 처리된 요청인지 확인
        if (friendRequest.getStatus() != FriendStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 친구 요청입니다.");
        }

        friendRequest.reject();
        friendRepository.save(friendRequest);

        log.info("친구 요청 거절 완료 - 요청자: {}, 거절자: {}", 
                friendRequest.getRequester().getNickname(), user.getNickname());
    }

    /**
     * 친구 삭제
     */
    @Transactional
    public void removeFriend(Long friendId, User user) {
        Friend friendship = friendRepository.findById(friendId)
                .orElseThrow(() -> new NoSuchElementException("친구 관계를 찾을 수 없습니다. ID: " + friendId));

        // 친구 관계에 포함된 사용자만 삭제할 수 있음
        if (!friendship.isRequester(user.getId()) && !friendship.isAddressee(user.getId())) {
            throw new IllegalStateException("친구 관계를 삭제할 권한이 없습니다.");
        }

        // 수락된 친구 관계만 삭제 가능
        if (friendship.getStatus() != FriendStatus.ACCEPTED) {
            throw new IllegalStateException("수락된 친구 관계만 삭제할 수 있습니다.");
        }

        friendRepository.delete(friendship);

        String friendName = friendship.isRequester(user.getId()) ? 
                friendship.getAddressee().getNickname() : 
                friendship.getRequester().getNickname();

        log.info("친구 삭제 완료 - 사용자: {}, 삭제된 친구: {}", user.getNickname(), friendName);
    }

    /**
     * 사용자 검색 (이메일로)
     */
    public UserDto.Response searchUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        return user != null ? UserDto.Response.from(user) : null;
    }
}
