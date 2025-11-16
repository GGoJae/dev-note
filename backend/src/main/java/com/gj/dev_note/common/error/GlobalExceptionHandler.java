package com.gj.dev_note.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // TODO 도메인 생기면 수정하기
    private static final String TYPE_BASE = "https://dev-note/errors/";

    private ProblemDetail problem(HttpStatus st, String title, String detail,
                                  HttpServletRequest req, String typeSlug) {
        var pd = ProblemDetail.forStatus(st);
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setType(URI.create(TYPE_BASE + typeSlug));
        pd.setProperty("timestamp", Instant.now().toString());

        String traceId = MDC.get("traceId");
        if (traceId != null) pd.setProperty("traceId", traceId);

        return pd;
    }

    private ProblemDetail problem(HttpStatus st, String title, String detail,
                                  HttpServletRequest req, String typeSlug,
                                  Map<String, ?> extras) {
        var pd = problem(st, title, detail, req, typeSlug);
        if (extras != null) extras.forEach(pd::setProperty);
        return pd;
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApi(ApiException e, HttpServletRequest req) {
        var extras = new LinkedHashMap<String, Object>();
        if (e.getContexts() != null && e.getContexts().hasContext()) {
            extras.put("contexts", e.getContexts());
        }
        return problem(e.getCode().status, e.getCode().name(), e.getMessage(), req,
                e.getCode().name().toLowerCase(Locale.ROOT), extras);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalid(MethodArgumentNotValidException e, HttpServletRequest req) {
        var errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField, LinkedHashMap::new,
                        Collectors.mapping(
                                fe -> fe.getDefaultMessage() == null ? "" : fe.getDefaultMessage(),
                                Collectors.toList()
                        )));

        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "입력값을 확인하세요.", req, "validation_failed", Map.of("errors", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraint(ConstraintViolationException e, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION",
                e.getMessage(), req, "constraint_violation");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIAE(IllegalArgumentException e, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                e.getMessage(), req, "bad_request");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        String detail = "파라미터 타입이 올바르지 않습니다: %s=%s".formatted(e.getName(), e.getValue());
        return problem(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", detail, req, "type_mismatch");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, "MESSAGE_NOT_READABLE",
                "요청 본문을 읽을 수 없습니다.", req, "message_not_readable");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest req) {
        String detail = "필수 파라미터가 없습니다: " + e.getParameterName();
        return problem(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", detail, req, "missing_parameter");
    }

    @ExceptionHandler({DuplicateKeyException.class, DataIntegrityViolationException.class})
    public ProblemDetail handleDataConflict(Exception e, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, "DATA_CONFLICT",
                "무결성 제약 조건 위반 또는 중복 키입니다.", req, "data_conflict");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuth(AuthenticationException e, HttpServletRequest req) {
        return problem(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "인증이 필요합니다.", req, "unauthorized");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException e, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "접근 권한이 없습니다.", req, "forbidden");
    }

    @ExceptionHandler({ResponseStatusException.class, ErrorResponseException.class})
    public ProblemDetail handleResponseStatus(Exception e, HttpServletRequest req) {
        HttpStatus st;
        String title;
        String detail = e.getMessage();

        if (e instanceof ResponseStatusException rse) {
            st = HttpStatus.valueOf(rse.getStatusCode().value());
            title = rse.getStatusCode().toString();
        } else if (e instanceof ErrorResponseException ere) {
            st = HttpStatus.valueOf(ere.getStatusCode().value());
            title = ere.getStatusCode().toString();
        } else {
            st = HttpStatus.INTERNAL_SERVER_ERROR;
            title = "INTERNAL_ERROR";
        }
        return problem(st, title, detail, req, title.toLowerCase());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception e, HttpServletRequest req) {
        log.error("Unhandled exception", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "서버 에러가 발생했습니다.", req, "internal_error");
    }

}
