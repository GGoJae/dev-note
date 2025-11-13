package com.gj.dev_note.common.exception;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String until) {
        super("계정이 잠겼습니다. 해제 시간 " + until);
    }
}
