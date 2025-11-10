package com.gj.dev_note.auth.listener;

import com.gj.dev_note.auth.events.EmailVerificationIssued;
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
public class EmailVerificationListener {

    private final MailService mail;
    private final VerificationLinkBuilder links;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIssued(EmailVerificationIssued e) {
        String link = links.emailVerifyLink(e.token());
        mail.sendEmailVerification(e.email(), link);
        log.info("Email verification mail sent to {} (token len={})", e.email(), e.token().length());
    }
}
