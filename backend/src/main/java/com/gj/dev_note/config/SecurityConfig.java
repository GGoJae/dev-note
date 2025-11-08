package com.gj.dev_note.config;

import com.gj.dev_note.security.JwtProperties;
import com.gj.dev_note.security.MemberPrincipal;
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
            c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
            c.setAllowedHeaders(List.of("*"));
            c.setAllowCredentials(true);
            c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
            c.setExposedHeaders(List.of("Authorization"));
            return c;
        }));

        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(sm -> sm.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notes/**").permitAll()
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


    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secret = props.secret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(secret, "HmacSHA256");

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    private JwtAuthenticationConverter jwtAuthConverter() {
//        var converter = new JwtAuthenticationConverter();
//        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
//            List<String> roles = jwt.getClaimAsStringList("roles");
//            if (roles == null) return List.of();
//            return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
//        });
//        return converter;
//    }

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

            MemberPrincipal principal = MemberPrincipal.fromJwt(uid, username, authorities);

            return new JwtAuthenticationToken(jwt, authorities, username) {
                @Override
                public Object getPrincipal() {
                    return principal;
                }
            };
        };
    }

    private Optional<Long> parseLong(String s) {
        try { return Optional.of(Long.parseLong(s)); } catch (Exception e) { return Optional.empty(); }
    }
}
