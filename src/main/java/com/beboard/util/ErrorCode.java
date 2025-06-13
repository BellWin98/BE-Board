package com.beboard.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INPUT_VALUE_INVALID(400, "유효하지 않은 입력입니다."),
    EMAIL_ALREADY_USED(400, "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_USED(400, "이미 사용 중인 사용자명입니다."),
    CATEGORY_ALREADY_EXIST(400, "이미 존재하는 카테고리입니다."),
    INVALID_TOKEN(400, "유효하지 않은 토큰입니다."),
    ;

    private final int status;
    private final String message;
}
