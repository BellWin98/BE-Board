
package com.beboard.controller;

import com.beboard.dto.FriendDto;
import com.beboard.dto.UserDto;
import com.beboard.entity.User;
import com.beboard.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public ResponseEntity<Page<FriendDto.Response>> getFriends(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("친구 목록 조회 요청 - 사용자: {}", currentUser.getNickname());
        Page<FriendDto.Response> friends = friendService.getFriends(currentUser, pageable);
        
        return ResponseEntity.ok(friends);
    }

    @PostMapping("/request")
    public ResponseEntity<FriendDto.Response> sendFriendRequest(
            @Valid @RequestBody FriendDto.SendRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("친구 요청 전송 요청 - 요청자: {}, 수신자 이메일: {}", 
                currentUser.getNickname(), request.getEmail());
        FriendDto.Response friendRequest = friendService.sendFriendRequest(request, currentUser);
        
        return ResponseEntity.ok(friendRequest);
    }

    @GetMapping("/requests/received")
    public ResponseEntity<Page<FriendDto.Response>> getReceivedRequests(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("받은 친구 요청 목록 조회 요청 - 사용자: {}", currentUser.getNickname());
        Page<FriendDto.Response> requests = friendService.getReceivedRequests(currentUser, pageable);
        
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<Page<FriendDto.Response>> getSentRequests(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("보낸 친구 요청 목록 조회 요청 - 사용자: {}", currentUser.getNickname());
        Page<FriendDto.Response> requests = friendService.getSentRequests(currentUser, pageable);
        
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendDto.Response> acceptFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("친구 요청 수락 요청 - 요청ID: {}, 사용자: {}", requestId, currentUser.getNickname());
        FriendDto.Response friendRequest = friendService.acceptFriendRequest(requestId, currentUser);
        
        return ResponseEntity.ok(friendRequest);
    }

    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("친구 요청 거절 요청 - 요청ID: {}, 사용자: {}", requestId, currentUser.getNickname());
        friendService.rejectFriendRequest(requestId, currentUser);
        
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long friendId,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("친구 삭제 요청 - 친구ID: {}, 사용자: {}", friendId, currentUser.getNickname());
        friendService.removeFriend(friendId, currentUser);
        
        return ResponseEntity.noContent().build();
    }
}
