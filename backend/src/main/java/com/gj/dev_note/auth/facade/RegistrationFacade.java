package com.gj.dev_note.auth.facade;

import com.gj.dev_note.auth.events.EmailVerificationIssued;
import com.gj.dev_note.auth.service.EmailVerificationService;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.service.MemberService;
import com.gj.dev_note.auth.request.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationFacade {

    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;
    private final ApplicationEventPublisher events;

    @Transactional
    public Long signup(SignupRequest cmd) {
        Member m = memberService.register(cmd);                 // 멤버 저장
        String token = emailVerificationService.issueToken(m);  // 토큰 저장
        // TODO 지금은 동기처리하는데 추후 비동기 처리 혹은 메세지큐 적용하기
        events.publishEvent(new EmailVerificationIssued(m.getEmail(), token));
        return m.getId();
    }
}
