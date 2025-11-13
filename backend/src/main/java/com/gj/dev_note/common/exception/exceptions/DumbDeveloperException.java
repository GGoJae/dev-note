package com.gj.dev_note.common.exception.exceptions;

public class DumbDeveloperException extends RuntimeException {
    public static final String message = "죄송합니다... 서버 에러입니다.";
    public DumbDeveloperException() {
        super(message);
    }
}
