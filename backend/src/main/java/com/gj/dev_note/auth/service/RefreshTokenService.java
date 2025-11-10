package com.gj.dev_note.auth.service;

import com.gj.dev_note.auth.domain.RefreshToken;
import com.gj.dev_note.auth.properties.AuthTokenProperties;
import com.gj.dev_note.auth.repository.RefreshTokenRepository;
import com.gj.dev_note.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;


@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final AuthTokenProperties props;

    private static final SecureRandom RNG = new SecureRandom();
    private String newTokenString() {
        byte[] buf = new byte[32];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    @Transactional
    public String issue(Member member, String ip, String userAgent) {
        var rt = RefreshToken.builder()
                .member(member)
                .token(newTokenString())
                .expiresAt(Instant.now().plusSeconds(props.getRefreshDays() * 24L * 3600L))
                .createdByIp(ip)
                .userAgent(userAgent)
                .build();
        repo.save(rt);
        return rt.getToken();
    }

    @Transactional
    public String rotate(String oldToken, String ip, String userAgent) {
        var rt = repo.findByToken(oldToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰"));
        if (!rt.isActive()) throw new IllegalArgumentException("만료되었거나 폐기된 토큰");

        String newT = newTokenString();
        var next = RefreshToken.builder()
                .member(rt.getMember())
                .token(newT)
                .expiresAt(Instant.now().plusSeconds(props.getRefreshDays() * 24L * 3600L))
                .createdByIp(ip)
                .userAgent(userAgent)
                .build();
        repo.save(next);

        rt.setRevokedAt(Instant.now());
        rt.setReplacedByToken(newT);
        return newT;
    }

    @Transactional(readOnly = true)
    public Member requireValidOwner(String token) {
        var rt = repo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰"));
        if (!rt.isActive()) throw new IllegalArgumentException("만료되었거나 폐기된 토큰");
        return rt.getMember();
    }

    @Transactional
    public void revokeAll(Member member) {
        repo.findAllByMember(member).forEach(rt -> {
            if (rt.isActive()) rt.setRevokedAt(Instant.now());
        });
    }
}

