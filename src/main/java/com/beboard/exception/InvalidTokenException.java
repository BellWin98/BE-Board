package com.beboard.exception;

import com.beboard.util.ErrorCode;

public class InvalidTokenException extends BusinessException{
    public InvalidTokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}