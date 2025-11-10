package com.gj.dev_note.auth.events;

public record PasswordResetIssued(String email, String token) {
}
