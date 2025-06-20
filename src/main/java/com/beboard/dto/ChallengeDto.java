
package com.beboard.dto;

import com.beboard.entity.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ChallengeDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다")
        private String title;

        @Size(max = 1000, message = "설명은 1000자 이내여야 합니다")
        private String description;

        @NotBlank(message = "카테고리는 필수입니다")
        @Size(max = 50, message = "카테고리는 50자 이내여야 합니다")
        private String category;

        @NotNull(message = "목표 금액은 필수입니다")
        @DecimalMin(value = "0.0", inclusive = false, message = "목표 금액은 0보다 커야 합니다")
        private BigDecimal goalAmount;

        @NotNull(message = "베팅 금액은 필수입니다")
        @DecimalMin(value = "1000.0", message = "베팅 금액은 최소 1000원 이상이어야 합니다")
        private BigDecimal betAmount;

        @NotNull(message = "시작일은 필수입니다")
        @Future(message = "시작일은 미래 날짜여야 합니다")
        private LocalDate startDate;

        @NotNull(message = "종료일은 필수입니다")
        private LocalDate endDate;

        @NotNull(message = "검증 방법은 필수입니다")
        private VerificationMethod verificationMethod;

        @NotNull(message = "최대 참가자 수는 필수입니다")
        @Min(value = 2, message = "최소 2명 이상 참가 가능해야 합니다")
        @Max(value = 50, message = "최대 50명까지 참가 가능합니다")
        private Integer maxParticipants;

        private List<Long> invitedFriends;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class JoinRequest {
        @NotNull(message = "베팅 금액은 필수입니다")
        @DecimalMin(value = "1000.0", message = "베팅 금액은 최소 1000원 이상이어야 합니다")
        private BigDecimal betAmount;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private String category;
        private BigDecimal goalAmount;
        private BigDecimal betAmount;
        private LocalDate startDate;
        private LocalDate endDate;
        private ChallengeStatus status;
        private VerificationMethod verificationMethod;
        private Integer maxParticipants;
        private UserDto.Response creator;
        private List<ParticipantResponse> participants;
        private BigDecimal totalPot;
        private Double successRate;
        private String createdAt;
        private String updatedAt;

        public static Response from(Challenge challenge) {
            List<ParticipantResponse> participantResponses = challenge.getParticipants().stream()
                    .map(ParticipantResponse::from)
                    .collect(Collectors.toList());

            return new Response(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getCategory(),
                    challenge.getGoalAmount(),
                    challenge.getBetAmount(),
                    challenge.getStartDate(),
                    challenge.getEndDate(),
                    challenge.getStatus(),
                    challenge.getVerificationMethod(),
                    challenge.getMaxParticipants(),
                    UserDto.Response.from(challenge.getCreator()),
                    participantResponses,
                    challenge.getTotalPot(),
                    challenge.getSuccessRate(),
                    challenge.getCreatedAt().toString(),
                    challenge.getUpdatedAt().toString()
            );
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class ParticipantResponse {
        private Long id;
        private Long challengeId;
        private UserDto.Response user;
        private BigDecimal betAmount;
        private ParticipantStatus status;
        private String joinedAt;

        public static ParticipantResponse from(ChallengeParticipant participant) {
            return new ParticipantResponse(
                    participant.getId(),
                    participant.getChallenge().getId(),
                    UserDto.Response.from(participant.getUser()),
                    participant.getBetAmount(),
                    participant.getStatus(),
                    participant.getJoinedAt().toString()
            );
        }
    }
}
