
package com.beboard.service;

import com.beboard.dto.ChallengeProgressDto;
import com.beboard.entity.ChallengeParticipant;
import com.beboard.entity.ChallengeProgress;
import com.beboard.entity.User;
import com.beboard.repository.ChallengeParticipantRepository;
import com.beboard.repository.ChallengeProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChallengeProgressService {

    private final ChallengeProgressRepository progressRepository;
    private final ChallengeParticipantRepository participantRepository;

    /**
     * 챌린지 진행상황 조회
     */
    public List<ChallengeProgressDto.Response> getChallengeProgress(Long challengeId) {
        List<ChallengeProgress> progressList = progressRepository.findByChallengeIdOrderByDateDesc(challengeId);
        
        return progressList.stream()
                .map(ChallengeProgressDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 내 진행상황 제출
     */
    @Transactional
    public ChallengeProgressDto.Response submitProgress(Long challengeId, ChallengeProgressDto.SubmitRequest request, User user) {
        // 참가자 정보 확인
        ChallengeParticipant participant = participantRepository.findByChallengeIdAndUserId(challengeId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("챌린지 참가 정보를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();
        
        // 오늘 이미 제출했는지 확인
        if (progressRepository.findByParticipantIdAndDate(participant.getId(), today).isPresent()) {
            throw new IllegalStateException("오늘은 이미 진행상황을 제출했습니다.");
        }

        ChallengeProgress progress = ChallengeProgress.builder()
                .participant(participant)
                .date(today)
                .completed(request.getCompleted())
                .proof(request.getProof())
                .build();

        ChallengeProgress savedProgress = progressRepository.save(progress);

        log.info("진행상황 제출 완료 - 챌린지ID: {}, 사용자: {}, 완료여부: {}", 
                challengeId, user.getNickname(), request.getCompleted());

        return ChallengeProgressDto.Response.from(savedProgress);
    }

    /**
     * 진행상황 검증
     */
    @Transactional
    public ChallengeProgressDto.Response verifyProgress(Long progressId, ChallengeProgressDto.VerifyRequest request, User verifier) {
        ChallengeProgress progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new NoSuchElementException("진행상황을 찾을 수 없습니다. ID: " + progressId));

        // 자신의 진행상황은 검증할 수 없음
        if (progress.getParticipant().getUser().getId().equals(verifier.getId())) {
            throw new IllegalStateException("자신의 진행상황은 검증할 수 없습니다.");
        }

        // TODO: 검증 권한 확인 로직 추가 (상호 검증, 관리자 검증 등)

        progress.verify(verifier, request.getVerified(), request.getComment());
        ChallengeProgress savedProgress = progressRepository.save(progress);

        log.info("진행상황 검증 완료 - 진행상황ID: {}, 검증자: {}, 결과: {}", 
                progressId, verifier.getNickname(), request.getVerified());

        return ChallengeProgressDto.Response.from(savedProgress);
    }

    /**
     * 검증 대기 중인 진행상황 목록 조회
     */
    public List<ChallengeProgressDto.Response> getPendingVerifications(Long challengeId) {
        List<ChallengeProgress> pendingList = progressRepository.findPendingVerificationByChallengeId(challengeId);
        
        return pendingList.stream()
                .map(ChallengeProgressDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 내가 검증해야 할 진행상황 목록 조회
     */
    public List<ChallengeProgressDto.Response> getProgressToVerify(User user) {
        List<ChallengeProgress> progressList = progressRepository.findProgressToVerifyByUserId(user.getId());
        
        return progressList.stream()
                .map(ChallengeProgressDto.Response::from)
                .collect(Collectors.toList());
    }
}
