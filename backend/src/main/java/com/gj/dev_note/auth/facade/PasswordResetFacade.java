package com.gj.dev_note.auth.facade;

import com.gj.dev_note.auth.events.PasswordResetIssued;
import com.gj.dev_note.auth.request.ResetPasswordRequest;
import com.gj.dev_note.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetFacade {

    private final PasswordResetService passwordResetService;
    private final ApplicationEventPublisher events;

    @Transactional
    public void issue(String emailRaw) {
        Optional<String> tokenOpt = passwordResetService.issueToken(emailRaw);
        tokenOpt.ifPresent(token ->
                events.publishEvent(new PasswordResetIssued(emailRaw.trim().toLowerCase(), token)));
    }

    @Transactional
    public void reset(ResetPasswordRequest req) {
        passwordResetService.reset(req.token(), req.newPassword());
    }
}
