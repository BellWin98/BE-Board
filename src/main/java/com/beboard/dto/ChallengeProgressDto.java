
package com.beboard.dto;

import com.beboard.entity.ChallengeProgress;
import com.beboard.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class ChallengeProgressDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class SubmitRequest {
        @NotNull(message = "완료 여부는 필수입니다")
        private Boolean completed;

        private String proof; // 인증 자료
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class VerifyRequest {
        @NotNull(message = "검증 결과는 필수입니다")
        private Boolean verified;

        private String comment;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long participantId;
        private ChallengeDto.ParticipantResponse participant;
        private LocalDate date;
        private Boolean completed;
        private String proof;
        private UserDto.Response verifiedBy;
        private VerificationStatus verificationStatus;
        private String verificationComment;
        private String createdAt;

        public static Response from(ChallengeProgress progress) {
            return new Response(
                    progress.getId(),
                    progress.getParticipant().getId(),
                    ChallengeDto.ParticipantResponse.from(progress.getParticipant()),
                    progress.getDate(),
                    progress.getCompleted(),
                    progress.getProof(),
                    progress.getVerifiedBy() != null ? UserDto.Response.from(progress.getVerifiedBy()) : null,
                    progress.getVerificationStatus(),
                    progress.getVerificationComment(),
                    progress.getCreatedAt().toString()
            );
        }
    }
}
