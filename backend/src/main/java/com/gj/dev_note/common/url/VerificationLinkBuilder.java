package com.gj.dev_note.common.url;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationLinkBuilder {

    private final AppUrlProperties props;

    public String emailVerifyLink(String token) {
        return props.getPublicBaseUrl() + props.getVerifyEmailPath() + "?token=" + token;
    }

    public String passwordResetLink(String token) {
        return props.getPublicBaseUrl() + props.getResetPasswordPath() + "?token=" + token;
    }
}
