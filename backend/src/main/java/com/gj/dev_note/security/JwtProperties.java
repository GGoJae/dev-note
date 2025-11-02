package com.gj.dev_note.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {
    @Value("${jwt.issuer:dev-note}")
    public String issuer;

    @Value("${jwt.expiration-seconds:36000}")
    public long expirationSeconds;

    @Value("${JWT_SECRET}")
    public String secret;
}
