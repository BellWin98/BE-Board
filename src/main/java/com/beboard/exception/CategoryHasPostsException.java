package com.beboard.exception;

/**
 * 카테고리에 게시글이 있어서 삭제할 수 없을 때 발생하는 예외
 */
public class CategoryHasPostsException extends RuntimeException {
    public CategoryHasPostsException(String message) {
        super(message);
    }

    public CategoryHasPostsException(String message, Throwable cause) {
        super(message, cause);
    }
}
