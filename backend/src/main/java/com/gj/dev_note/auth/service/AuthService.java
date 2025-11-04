package com.gj.dev_note.auth.service;

import com.gj.dev_note.auth.dto.response.AuthResponse;
import com.gj.dev_note.auth.dto.request.LoginRequest;
import com.gj.dev_note.auth.dto.request.SignupRequest;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.domain.Role;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.security.JwtIssuer;
import com.gj.dev_note.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;
    private final JwtProperties props;

    @Transactional
    public void signup(SignupRequest req) {
        if (memberRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일");
        }
        Member m = Member.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .nickname(req.nickname())
                .roles(Set.of(Role.USER))
                .build();
        memberRepo.save(m);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Member m = memberRepo.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        if (!passwordEncoder.matches(req.password(), m.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않음");
        }

        String token = jwtIssuer.issue(m.getId(), m.getEmail(), m.getRoles());
        return new AuthResponse(token, "Bearer", props.expirationSeconds);
    }
}
