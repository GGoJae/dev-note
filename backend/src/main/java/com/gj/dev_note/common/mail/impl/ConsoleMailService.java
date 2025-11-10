package com.gj.dev_note.common.mail.impl;

import com.gj.dev_note.common.mail.MailService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleMailService implements MailService {
    @Override
    public void send(String to, String subject, String body) {
        log.info("=== MAIL ===\nTO: {}\nSUBJECT: {}\nBODY:\n{}\n============", to, subject, body);
    }
}
