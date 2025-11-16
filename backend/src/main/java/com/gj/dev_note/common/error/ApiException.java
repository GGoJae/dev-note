package com.gj.dev_note.common.error;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode code;
    private final ErrorContexts contexts;

    public ApiException(ErrorCode code) {
        super(code.defaultMessage);
        this.code = code;
        this.contexts = ErrorContexts.empty();
    }

    public ApiException(ErrorCode code, String message) {
        super(message);
        this.code = code;
        this.contexts = ErrorContexts.empty();
    }

    public ApiException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.contexts = ErrorContexts.empty();
    }

    public ApiException(ErrorCode code, String message, Throwable cause, ErrorContexts contexts) {
        super(message, cause);
        this.code = code;
        this.contexts = (contexts == null) ? ErrorContexts.empty() : contexts;
    }

    public ApiException with(String field, Object value) {
        return new ApiException(code, getMessage(), getCause(),
                contexts.addContext(field, value));
    }

    public ApiException withCause(Throwable cause) {
        return new ApiException(code, getMessage(), cause, contexts);
    }

    public static ApiException of(ErrorCode code) { return new ApiException(code); }
    public static ApiException of(ErrorCode code, String msg) { return new ApiException(code, msg); }
    public static ApiException of(ErrorCode code, String msg, Throwable cause) {
        return new ApiException(code, msg, cause);
    }
    public static ApiException of(ErrorCode code, String msg, Throwable cause, ErrorContexts ctx) {
        return new ApiException(code, msg, cause, ctx);
    }

    public record ErrorContexts (
            List<ErrorContext> contextList
    ) {

        public ErrorContexts addContext(String field, Object context) {
            List<ErrorContext> out = new ArrayList<>(contextList);
            out.add(new ErrorContext(field, context));
            return new ErrorContexts(List.copyOf(out));
        }

        public boolean isEmpty() {
            return contextList.isEmpty();
        }

        public boolean hasContext() {
            return !contextList.isEmpty();
        }

        public static ErrorContexts empty() {
            return new ErrorContexts(List.of());
        }

        public static ErrorContexts context(String field, Object context) {
            return new ErrorContexts(List.of(new ErrorContext(field, context)));
        }

        public record ErrorContext(
                String field,
                Object context
        ) {
        }
    }
}
