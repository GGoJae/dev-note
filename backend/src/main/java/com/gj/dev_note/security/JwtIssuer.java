package com.gj.dev_note.security;

import com.gj.dev_note.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtIssuer {
    private final JwtProperties props;

    public String issue(Long userId, String email, Set<Role> roles) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.expirationSeconds, ChronoUnit.SECONDS);
        SecretKey key = Keys.hmacShaKeyFor(props.secret.getBytes(StandardCharsets.UTF_8));

        var roleChains = roles.stream()
                .map(Role::asAuthority)
                .collect(Collectors.toUnmodifiableSet());

        Claims claims = Jwts.claims()
                .subject(String.valueOf(userId))
                .add("uid", userId)
                .add("email", email)
                .add("roles", roleChains)
                .add("token_type", "access")
                .build();

        return Jwts.builder()
                .issuer(props.issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(claims)
                .signWith(key)
                .compact();

    }
}
