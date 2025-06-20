
package com.beboard.dto;

import com.beboard.entity.Friend;
import com.beboard.entity.FriendStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FriendDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class SendRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @Size(max = 200, message = "메시지는 200자 이내여야 합니다")
        private String message;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private UserDto.Response requester;
        private UserDto.Response addressee;
        private FriendStatus status;
        private String message;
        private String createdAt;
        private String updatedAt;

        public static Response from(Friend friend) {
            return new Response(
                    friend.getId(),
                    UserDto.Response.from(friend.getRequester()),
                    UserDto.Response.from(friend.getAddressee()),
                    friend.getStatus(),
                    friend.getMessage(),
                    friend.getCreatedAt().toString(),
                    friend.getUpdatedAt().toString()
            );
        }
    }
}
