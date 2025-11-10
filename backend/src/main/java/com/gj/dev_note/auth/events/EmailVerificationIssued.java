package com.gj.dev_note.auth.events;

public record EmailVerificationIssued(String email, String token) {
}
