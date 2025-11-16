package com.gj.dev_note.auth.service;

import com.gj.dev_note.auth.domain.EmailVerificationToken;
import com.gj.dev_note.auth.properties.AuthTokenProperties;
import com.gj.dev_note.auth.repository.EmailVerificationTokenRepository;
import com.gj.dev_note.common.error.Errors;
import com.gj.dev_note.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final AuthTokenProperties props;

    /** 토큰만 저장하고 문자열 반환 (메일은 이벤트 리스너가 커밋 후 발송) */
    @Transactional
    public String issueToken(Member member) {
        String token = UUID.randomUUID().toString();
        var evt = EmailVerificationToken.builder()
                .member(member)
                .token(token)
                .expiresAt(Instant.now().plusSeconds(props.getEmailVerifyMinutes() * 60))
                .build();
        tokenRepo.save(evt);
        return token;
    }

    @Transactional
    public void verify(String token) {
        var t = tokenRepo.findByToken(token)
                .orElseThrow(() -> Errors.badRequest("유효하지 않은 토큰"));
        if (!t.isUsable()) throw Errors.badRequest("만료되었거나 사용된 토큰입니다.");
        t.getMember().setEmailVerifiedAt(Instant.now());
        t.setUsedAt(Instant.now());
    }
}
