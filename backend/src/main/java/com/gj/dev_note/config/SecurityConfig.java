package com.gj.dev_note.config;

import com.gj.dev_note.security.JwtProperties;
import com.gj.dev_note.security.MemberPrincipal;
import com.gj.dev_note.security.PasswordProps;              // ← 추가
import com.gj.dev_note.security.PepperedPasswordEncoder;   // ← 추가
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProperties props;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           Converter<Jwt, ? extends AbstractAuthenticationToken> memberJwtAuthConverter) throws Exception{

        http.cors(cors -> cors.configurationSource(request -> {
            var c = new CorsConfiguration();
            c.setAllowedOrigins(List.of("http://localhost:5173"));
            c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
            c.setExposedHeaders(List.of("Authorization"));
            c.setAllowCredentials(true);
            return c;
        }));

        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/quiz-sets/**").permitAll()
                .anyRequest().authenticated());

        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(memberJwtAuthConverter)));

        http.exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(401);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\":\"unauthorized\"}");
                })
                .accessDeniedHandler((req, res, ex) -> {
                    res.setStatus(403);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\":\"forbidden\"}");
                }));

        return http.build();
    }

    // === JWT Decoder: Base64 지원 + 키 길이 보장 ===
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec key = hmacKey(props.secret);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(props.issuer);
        DelegatingOAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(
                withIssuer, new JwtTimestampValidator(Duration.ofSeconds(30))
        );
        decoder.setJwtValidator(withClockSkew);
        return decoder;
    }

    private SecretKeySpec hmacKey(String secret) {
        // 운영에선 Base64로 넣는 걸 권장. Base64 같으면 decode, 아니면 UTF-8 바이트 사용.
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(secret);
            if (raw.length < 32) throw new IllegalArgumentException("jwt.secret(Base64)가 32바이트 미만");
        } catch (IllegalArgumentException e) {
            raw = secret.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 32) {
                throw new IllegalArgumentException("jwt.secret가 32바이트 미만입니다. 더 긴 시크릿을 사용하세요.");
            }
        }
        return new SecretKeySpec(raw, "HmacSHA256");
    }

    // === PasswordEncoder: pepper 적용 ===
    @Bean
    public PasswordEncoder passwordEncoder(PasswordProps props) {
        return new PepperedPasswordEncoder(new BCryptPasswordEncoder(), props.getPepper());
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> memberJwtAuthConverter() {
        return (Jwt jwt) -> {
            List<SimpleGrantedAuthority> authorities =
                    Optional.ofNullable(jwt.getClaimAsStringList("roles"))
                            .orElseGet(List::of)
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

            Long uid = Optional.ofNullable(jwt.getClaim("uid"))
                    .map(Object::toString)
                    .flatMap(this::parseLong)
                    .orElseGet(() -> parseLong(jwt.getSubject()).orElse(null));

            String username = Optional.ofNullable(jwt.getClaimAsString("email"))
                    .orElse(jwt.getSubject());

            var principal = MemberPrincipal.fromJwt(uid, username, authorities);

            return new JwtAuthenticationToken(jwt, authorities, username) {
                @Override public Object getPrincipal() { return principal; }
            };
        };
    }

    private Optional<Long> parseLong(String s) {
        try { return Optional.of(Long.parseLong(s)); } catch (Exception e) { return Optional.empty(); }
    }
}
