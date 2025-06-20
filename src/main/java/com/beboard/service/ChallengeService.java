
package com.beboard.service;

import com.beboard.dto.ChallengeDto;
import com.beboard.entity.*;
import com.beboard.repository.ChallengeParticipantRepository;
import com.beboard.repository.ChallengeRepository;
import com.beboard.repository.FriendRepository;
import com.beboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    /**
     * 챌린지 목록 조회
     */
    public Page<ChallengeDto.Response> getChallenges(Pageable pageable, String category, String status) {
        Page<Challenge> challenges;
        
        if (category != null && status != null) {
            ChallengeStatus challengeStatus = ChallengeStatus.valueOf(status.toUpperCase());
            challenges = challengeRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, challengeStatus, pageable);
        } else if (category != null) {
            challenges = challengeRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        } else if (status != null) {
            ChallengeStatus challengeStatus = ChallengeStatus.valueOf(status.toUpperCase());
            challenges = challengeRepository.findByStatusOrderByCreatedAtDesc(challengeStatus, pageable);
        } else {
            challenges = challengeRepository.findAll(pageable);
        }

        return challenges.map(ChallengeDto.Response::from);
    }

    /**
     * 내 챌린지 목록 조회 (생성한 챌린지 + 참여한 챌린지)
     */
    public Page<ChallengeDto.Response> getMyChallenges(Long userId, Pageable pageable) {
        Page<Challenge> challenges = challengeRepository.findByParticipantUserId(userId, pageable);
        return challenges.map(ChallengeDto.Response::from);
    }

    /**
     * 챌린지 상세 조회
     */
    public ChallengeDto.Response getChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NoSuchElementException("챌린지를 찾을 수 없습니다. ID: " + challengeId));

        return ChallengeDto.Response.from(challenge);
    }

    /**
     * 챌린지 생성
     */
    @Transactional
    public ChallengeDto.Response createChallenge(ChallengeDto.CreateRequest request, User creator) {
        // 종료일이 시작일보다 이후인지 검증
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 이후여야 합니다.");
        }

        Challenge challenge = Challenge.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .goalAmount(request.getGoalAmount())
                .betAmount(request.getBetAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .verificationMethod(request.getVerificationMethod())
                .maxParticipants(request.getMaxParticipants())
                .creator(creator)
                .build();

        Challenge savedChallenge = challengeRepository.save(challenge);

        // 생성자를 첫 번째 참가자로 자동 등록
        ChallengeParticipant creatorParticipant = ChallengeParticipant.builder()
                .challenge(savedChallenge)
                .user(creator)
                .betAmount(request.getBetAmount())
                .build();
        participantRepository.save(creatorParticipant);

        log.info("챌린지 생성 완료 - ID: {}, 제목: {}, 생성자: {}", 
                savedChallenge.getId(), savedChallenge.getTitle(), creator.getNickname());

        return ChallengeDto.Response.from(savedChallenge);
    }

    /**
     * 챌린지 참가
     */
    @Transactional
    public ChallengeDto.ParticipantResponse joinChallenge(Long challengeId, ChallengeDto.JoinRequest request, User user) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NoSuchElementException("챌린지를 찾을 수 없습니다. ID: " + challengeId));

        // 참가 가능 여부 검증
        if (!challenge.canJoin()) {
            throw new IllegalStateException("참가할 수 없는 챌린지입니다.");
        }

        // 이미 참가했는지 확인
        if (participantRepository.existsByChallengeIdAndUserId(challengeId, user.getId())) {
            throw new IllegalStateException("이미 참가한 챌린지입니다.");
        }

        // 현재 참가자 수 확인
        long currentParticipants = participantRepository.countActiveByChallengeId(challengeId);
        if (currentParticipants >= challenge.getMaxParticipants()) {
            throw new IllegalStateException("참가자 수가 초과되었습니다.");
        }

        ChallengeParticipant participant = ChallengeParticipant.builder()
                .challenge(challenge)
                .user(user)
                .betAmount(request.getBetAmount())
                .build();

        ChallengeParticipant savedParticipant = participantRepository.save(participant);

        log.info("챌린지 참가 완료 - 챌린지ID: {}, 사용자: {}, 베팅금액: {}", 
                challengeId, user.getNickname(), request.getBetAmount());

        return ChallengeDto.ParticipantResponse.from(savedParticipant);
    }

    /**
     * 챌린지 나가기
     */
    @Transactional
    public void leaveChallenge(Long challengeId, User user) {
        ChallengeParticipant participant = participantRepository.findByChallengeIdAndUserId(challengeId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("참가 정보를 찾을 수 없습니다."));

        Challenge challenge = participant.getChallenge();
        
        // 시작된 챌린지는 나갈 수 없음
        if (challenge.getStatus() != ChallengeStatus.RECRUITING) {
            throw new IllegalStateException("시작된 챌린지는 나갈 수 없습니다.");
        }

        // 생성자는 나갈 수 없음
        if (challenge.getCreator().getId().equals(user.getId())) {
            throw new IllegalStateException("챌린지 생성자는 나갈 수 없습니다.");
        }

        participantRepository.delete(participant);

        log.info("챌린지 나가기 완료 - 챌린지ID: {}, 사용자: {}", challengeId, user.getNickname());
    }

    /**
     * 챌린지 완료 처리
     */
    @Transactional
    public ChallengeDto.Response completeChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NoSuchElementException("챌린지를 찾을 수 없습니다. ID: " + challengeId));

        if (challenge.getStatus() != ChallengeStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 챌린지만 완료할 수 있습니다.");
        }

        challenge.completeChallenge();
        Challenge savedChallenge = challengeRepository.save(challenge);

        // TODO: 성과에 따른 보상 분배 로직 추가

        log.info("챌린지 완료 처리 - ID: {}, 제목: {}", challengeId, challenge.getTitle());

        return ChallengeDto.Response.from(savedChallenge);
    }

    /**
     * 챌린지 취소
     */
    @Transactional
    public ChallengeDto.Response cancelChallenge(Long challengeId, User user) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NoSuchElementException("챌린지를 찾을 수 없습니다. ID: " + challengeId));

        // 생성자만 취소 가능
        if (!challenge.getCreator().getId().equals(user.getId())) {
            throw new IllegalStateException("챌린지 생성자만 취소할 수 있습니다.");
        }

        // 모집 중일 때만 취소 가능
        if (challenge.getStatus() != ChallengeStatus.RECRUITING) {
            throw new IllegalStateException("모집 중인 챌린지만 취소할 수 있습니다.");
        }

        challenge.cancelChallenge();
        Challenge savedChallenge = challengeRepository.save(challenge);

        log.info("챌린지 취소 완료 - ID: {}, 제목: {}, 취소자: {}", 
                challengeId, challenge.getTitle(), user.getNickname());

        return ChallengeDto.Response.from(savedChallenge);
    }
}
