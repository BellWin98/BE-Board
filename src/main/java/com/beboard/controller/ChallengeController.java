
package com.beboard.controller;

import com.beboard.dto.ChallengeDto;
import com.beboard.dto.ChallengeProgressDto;
import com.beboard.entity.User;
import com.beboard.service.ChallengeProgressService;
import com.beboard.service.ChallengeService;
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

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Slf4j
public class ChallengeController {

    private final ChallengeService challengeService;
    private final ChallengeProgressService progressService;

    @GetMapping
    public ResponseEntity<Page<ChallengeDto.Response>> getChallenges(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        
        log.info("챌린지 목록 조회 요청 - 카테고리: {}, 상태: {}", category, status);
        Page<ChallengeDto.Response> challenges = challengeService.getChallenges(pageable, category, status);
        
        return ResponseEntity.ok(challenges);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ChallengeDto.Response>> getMyChallenges(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("내 챌린지 목록 조회 요청 - 사용자: {}", currentUser.getNickname());
        Page<ChallengeDto.Response> challenges = challengeService.getMyChallenges(currentUser.getId(), pageable);
        
        return ResponseEntity.ok(challenges);
    }

    @GetMapping("/{challengeId}")
    public ResponseEntity<ChallengeDto.Response> getChallenge(@PathVariable Long challengeId) {
        log.info("챌린지 상세 조회 요청 - ID: {}", challengeId);
        ChallengeDto.Response challenge = challengeService.getChallenge(challengeId);
        
        return ResponseEntity.ok(challenge);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChallengeDto.Response> createChallenge(
            @Valid @RequestBody ChallengeDto.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("챌린지 생성 요청 - 제목: {}, 생성자: {}", request.getTitle(), currentUser.getNickname());
        ChallengeDto.Response challenge = challengeService.createChallenge(request, currentUser);
        
        return ResponseEntity.ok(challenge);
    }

    @PostMapping("/{challengeId}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChallengeDto.ParticipantResponse> joinChallenge(
            @PathVariable Long challengeId,
            @Valid @RequestBody ChallengeDto.JoinRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("챌린지 참가 요청 - 챌린지ID: {}, 사용자: {}", challengeId, currentUser.getNickname());
        ChallengeDto.ParticipantResponse participant = challengeService.joinChallenge(challengeId, request, currentUser);
        
        return ResponseEntity.ok(participant);
    }

    @DeleteMapping("/{challengeId}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> leaveChallenge(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("챌린지 나가기 요청 - 챌린지ID: {}, 사용자: {}", challengeId, currentUser.getNickname());
        challengeService.leaveChallenge(challengeId, currentUser);
        
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{challengeId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChallengeProgressDto.Response> submitProgress(
            @PathVariable Long challengeId,
            @Valid @RequestBody ChallengeProgressDto.SubmitRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("진행상황 제출 요청 - 챌린지ID: {}, 사용자: {}", challengeId, currentUser.getNickname());
        ChallengeProgressDto.Response progress = progressService.submitProgress(challengeId, request, currentUser);
        
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{challengeId}/progress")
    public ResponseEntity<List<ChallengeProgressDto.Response>> getChallengeProgress(@PathVariable Long challengeId) {
        log.info("챌린지 진행상황 조회 요청 - 챌린지ID: {}", challengeId);
        List<ChallengeProgressDto.Response> progressList = progressService.getChallengeProgress(challengeId);
        
        return ResponseEntity.ok(progressList);
    }

    @PutMapping("/progress/{progressId}/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChallengeProgressDto.Response> verifyProgress(
            @PathVariable Long progressId,
            @Valid @RequestBody ChallengeProgressDto.VerifyRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("진행상황 검증 요청 - 진행상황ID: {}, 검증자: {}", progressId, currentUser.getNickname());
        ChallengeProgressDto.Response progress = progressService.verifyProgress(progressId, request, currentUser);
        
        return ResponseEntity.ok(progress);
    }

    @PutMapping("/{challengeId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChallengeDto.Response> completeChallenge(@PathVariable Long challengeId) {
        log.info("챌린지 완료 처리 요청 - 챌린지ID: {}", challengeId);
        ChallengeDto.Response challenge = challengeService.completeChallenge(challengeId);
        
        return ResponseEntity.ok(challenge);
    }

    @PutMapping("/{challengeId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChallengeDto.Response> cancelChallenge(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("챌린지 취소 요청 - 챌린지ID: {}, 사용자: {}", challengeId, currentUser.getNickname());
        ChallengeDto.Response challenge = challengeService.cancelChallenge(challengeId, currentUser);
        
        return ResponseEntity.ok(challenge);
    }
}
