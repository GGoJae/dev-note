package com.gj.dev_note.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurrentUser {

    public static Optional<Long> idOpt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return Optional.empty();

        try {
            return Optional.of(Long.valueOf(auth.getName()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Long id() {
        return idOpt().orElseThrow(() -> new IllegalArgumentException("인증되지 않음"));
    }
}
