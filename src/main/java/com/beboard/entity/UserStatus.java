package com.beboard.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    PENDING("가입 승인 대기"),           // 관리자 승인이 필요한 경우
    ACTIVE("활성"),                    // 정상적으로 서비스 이용 가능
    SUSPENDED("일시 정지"),            // 규정 위반으로 일시 정지
    DORMANT("휴면"),                  // 장기간 미접속으로 휴면 상태
    DELETED("삭제"),                  // 탈퇴 또는 삭제된 계정
    BANNED("영구 차단")               // 심각한 규정 위반으로 영구 차단

    ;

    private final String description;
}
