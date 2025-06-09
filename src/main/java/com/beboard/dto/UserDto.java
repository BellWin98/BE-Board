package com.beboard.dto;

import com.beboard.entity.Role;
import com.beboard.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

public class UserDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String email;
        private String nickname;
        private String profileImage;
        private Role role;
        private String createdAt;
        private String updatedAt;

        public static UserDto.Response from(User user) {
            return Response.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .profileImage(user.getProfileImage())
                    .role(user.getRole())
                    .createdAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(user.getCreatedAt()))
                    .updatedAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(user.getUpdatedAt()))
                    .build();
        }
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "사용자명은 필수입니다")
        @Size(min = 2, max = 20, message = "사용자명은 2-20자 사이여야 합니다")
        private String nickname;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다")
        private String password;
    }

    @Data
    public static class UpdateProfileRequest {
        private String email;
        private String password;
        private String nickname;
        private String profileImage;
    }

    @Data
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }
}
