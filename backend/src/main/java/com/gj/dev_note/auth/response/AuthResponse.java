package com.gj.dev_note.auth.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
