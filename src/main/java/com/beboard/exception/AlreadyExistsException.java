package com.beboard.exception;

import com.beboard.util.ErrorCode;

public class AlreadyExistsException extends BusinessException{
    public AlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
}