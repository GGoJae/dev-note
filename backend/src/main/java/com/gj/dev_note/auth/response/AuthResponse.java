package com.gj.dev_note.auth.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
