package com.gj.dev_note.auth.listener;

import com.gj.dev_note.auth.events.PasswordResetIssued;
import com.gj.dev_note.common.mail.MailService;
import com.gj.dev_note.common.url.VerificationLinkBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetListener {

    private final MailService mail;
    private final VerificationLinkBuilder links;

    // TODO 나중에 비동기 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIssued(PasswordResetIssued e) {
        String link = links.passwordResetLink(e.token());
        mail.sendPasswordReset(e.email(), link);
        log.info("Password reset mail sent to {} (token len={})", e.email(), e.token().length());
    }
}
