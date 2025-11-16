package com.gj.dev_note.security;

import com.sun.security.auth.UserPrincipal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurrentUser {

    public static Optional<Long> idOpt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) return Optional.empty();

        Object principal = auth.getPrincipal();
        if (principal instanceof MemberPrincipal mp && mp.id() != null) {
            return Optional.of(mp.id());
        }
        return Optional.empty();
    }

    public static Long idOrNull() {
        return idOpt().orElse(null);
    }

    public static Long id() {
        return idOpt().orElseThrow(() -> new AuthenticationCredentialsNotFoundException("인증되지 않음"));
    }
}
