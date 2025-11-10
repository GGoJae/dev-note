package com.gj.dev_note.auth.service;

import com.gj.dev_note.auth.request.LoginRequest;
import com.gj.dev_note.auth.request.SignupRequest;
import com.gj.dev_note.auth.response.AuthResponse;
import com.gj.dev_note.common.exception.AccountLockedException;
import com.gj.dev_note.common.exception.BadRequestException;
import com.gj.dev_note.common.exception.EmailAlreadyUsedException;
import com.gj.dev_note.common.exception.InvalidCredentialsException;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.domain.MemberStatus;
import com.gj.dev_note.member.domain.Role;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.security.JwtIssuer;
import com.gj.dev_note.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;
    private final JwtProperties props;

    // 정책 상수 (원하면 yml로 뺄 수 있음)
    private static final int LOCK_THRESHOLD = 5;
    private static final int LOCK_MINUTES = 15;

    @Transactional
    public void signup(SignupRequest req) {
        String email = normalizeEmail(req.email());
        String nickname = req.nickname().trim();

        if (memberRepo.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        String hash = passwordEncoder.encode(req.password());

        Member m = Member.builder()
                .email(email)
                .passwordHash(hash)
                .nickname(nickname)
                .roles(Set.of(Role.USER))
                .status(MemberStatus.ACTIVE)
                .build();

        memberRepo.save(m);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = normalizeEmail(req.email());

        Member m = memberRepo.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (m.getStatus() != MemberStatus.ACTIVE) {
            throw new BadRequestException("활성화되지 않은 계정입니다.");
        }
        if (m.isLockedNow()) {
            throw new AccountLockedException(String.valueOf(m.getLockUntil()));
        }

        if (!passwordEncoder.matches(req.password(), m.getPasswordHash())) {
            m.onLoginFail(LOCK_THRESHOLD, LOCK_MINUTES);
            returnAuthFail();
        }

        m.onLoginSuccess();

        String token = jwtIssuer.issue(m.getId(), m.getEmail(), m.getRoles());
        return new AuthResponse(token, "Bearer", props.expirationSeconds);
    }

    private void returnAuthFail() {
        throw new InvalidCredentialsException();
    }

    private String normalizeEmail(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }
}
