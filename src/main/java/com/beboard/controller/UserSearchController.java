
package com.beboard.controller;

import com.beboard.dto.UserDto;
import com.beboard.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class UserSearchController {

    private final FriendService friendService;

    @GetMapping("/search")
    public ResponseEntity<UserDto.Response> searchUserByEmail(@RequestParam String email) {
        log.info("사용자 검색 요청 - 이메일: {}", email);
        
        UserDto.Response user = friendService.searchUserByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user);
    }
}
