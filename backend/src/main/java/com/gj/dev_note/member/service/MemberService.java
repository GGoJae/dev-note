package com.gj.dev_note.member.service;

import com.gj.dev_note.auth.request.SignupRequest;
import com.gj.dev_note.common.error.Errors;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.domain.MemberStatus;
import com.gj.dev_note.member.domain.Role;
import com.gj.dev_note.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepo;
    private final PasswordEncoder passwordEncoder;

    public Member register(SignupRequest req) {
        String email = normalizeEmail(req.email());
        if (memberRepo.existsByEmail(email)) {
            throw Errors.emailTaken(email);
        }
        Member m = Member.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .nickname(req.nickname().trim())
                .roles(Set.of(Role.USER))
                .status(MemberStatus.ACTIVE)
                .build();
        return memberRepo.save(m);
    }

    public static String normalizeEmail(String raw) {
        if (raw == null) return null;
        String s = Normalizer.normalize(raw.trim(), Normalizer.Form.NFKC);
        return s.toLowerCase(Locale.ROOT);
    }
}
