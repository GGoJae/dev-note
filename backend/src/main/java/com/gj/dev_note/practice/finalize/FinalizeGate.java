package com.gj.dev_note.practice.finalize;

import com.gj.dev_note.practice.domain.PracticeSession;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class FinalizeGate {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(10);

    public String ensureAndIssue(PracticeSession ps, boolean allowedNow, Instant now) {
        if (!allowedNow) return null;
        if (ps.getFinalizeToken() != null && ps.getFinalizeTokenExpiresAt() != null
                && now.isBefore(ps.getFinalizeTokenExpiresAt())) {
            return ps.getFinalizeToken();
        }
        String token = RandomStringUtils.randomAlphanumeric(48);
        ps.issueFinalizeToken(token, now.plus(TOKEN_TTL));
        return token;
    }

    public boolean validate(PracticeSession ps, String token, Instant now) {
        return ps.canUseFinalizeToken(token, now);
    }
}
