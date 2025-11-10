package com.gj.dev_note.auth.service;

import com.gj.dev_note.auth.properties.AuthTokenProperties;
import com.gj.dev_note.auth.domain.PasswordResetToken;
import com.gj.dev_note.auth.repository.PasswordResetTokenRepository;
import com.gj.dev_note.auth.repository.RefreshTokenRepository;
import com.gj.dev_note.common.exception.InvalidCredentialsException;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final MemberRepository memberRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final RefreshTokenRepository refreshRepo;
    private final AuthTokenProperties props;
    private final PasswordEncoder encoder;

    /** 존재하면 토큰 발급, 없으면 빈 Optional */
    @Transactional
    public Optional<String> issueToken(String emailRaw) {
        if (emailRaw == null) return Optional.empty();
        String email = emailRaw.trim().toLowerCase(Locale.ROOT);
        Member m = memberRepo.findByEmail(email).orElse(null);
        if (m == null) return Optional.empty();

        String token = UUID.randomUUID().toString();
        var prt = PasswordResetToken.builder()
                .member(m)
                .token(token)
                .expiresAt(Instant.now().plusSeconds(props.getPasswordResetMinutes() * 60))
                .build();
        tokenRepo.save(prt);
        return Optional.of(token);
    }

    @Transactional
    public void reset(String token, String newPassword) {
        var prt = tokenRepo.findByToken(token)
                .orElseThrow(InvalidCredentialsException::new);
        if (!prt.isUsable()) throw new InvalidCredentialsException();

        var m = prt.getMember();
        m.setPasswordHash(encoder.encode(newPassword));
        prt.setUsedAt(Instant.now());

        // 보안: 모든 RT 무효화
        refreshRepo.findAllByMember(m).forEach(rt -> {
            rt.setRevokedAt(Instant.now());
            rt.setReplacedByToken(null);
        });
    }
}
