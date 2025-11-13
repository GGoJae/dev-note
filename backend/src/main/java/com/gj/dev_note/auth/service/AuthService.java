package com.gj.dev_note.auth.service;

import com.gj.dev_note.auth.request.LoginRequest;
import com.gj.dev_note.auth.response.AuthResponse;
import com.gj.dev_note.common.exception.exceptions.AccountLockedException;
import com.gj.dev_note.common.exception.exceptions.BadRequestException;
import com.gj.dev_note.common.exception.exceptions.InvalidCredentialsException;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.domain.MemberStatus;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.security.JwtIssuer;
import com.gj.dev_note.security.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;
    private final JwtProperties props;
    private final RefreshTokenService refreshTokenService;

    private static final int LOCK_THRESHOLD = 5;
    private static final int LOCK_MINUTES = 15;

    @Transactional
    public AuthResponse login(LoginRequest req, HttpServletRequest http) {
        String email = normalizeEmail(req.email());
        Member m = memberRepo.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        if (m.getStatus() != MemberStatus.ACTIVE) throw new BadRequestException("활성화되지 않은 계정입니다.");
        if (m.isLockedNow()) throw new AccountLockedException(String.valueOf(m.getLockUntil()));

        if (!passwordEncoder.matches(req.password(), m.getPasswordHash())) {
            m.onLoginFail(LOCK_THRESHOLD, LOCK_MINUTES);
            throw new InvalidCredentialsException();
        }
        m.onLoginSuccess();

        String access = jwtIssuer.issue(m.getId(), m.getEmail(), m.getRoles());
        String ip = clientIp(http);
        String ua = http.getHeader("User-Agent");
        String refresh = refreshTokenService.issue(m, ip, ua);

        return new AuthResponse(access, "Bearer", props.expirationSeconds, refresh);
    }

    private String normalizeEmail(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }
    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
