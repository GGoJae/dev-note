package com.gj.dev_note.common.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Errors {

    public static ApiException notFound(String resource, Object id) {
        return ApiException.of(ErrorCode.NOT_FOUND, resource + "를 찾을 수 없습니다.")
                .with("resource", resource)
                .with("id", id);
    }

    public static ApiException badRequest(String message) {
        return ApiException.of(ErrorCode.BAD_REQUEST, message);
    }

    public static ApiException emailTaken(String email) {
        return ApiException.of(ErrorCode.USER_EMAIL_TAKEN, "이미 사용 중인 이메일입니다.")
                .with("email", email);
    }

    public static ApiException invalidCredential() {
        return ApiException.of(ErrorCode.INVALID_CREDENTIAL);
    }

    public static ApiException forbidden(String reason) {
        return ApiException.of(ErrorCode.FORBIDDEN, reason);
    }

    public static ApiException internal() {
        return ApiException.of(ErrorCode.INTERNAL_ERROR);
    }

    public static ApiException internal(Throwable cause) {
        return ApiException.of(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.defaultMessage, cause);
    }
}
