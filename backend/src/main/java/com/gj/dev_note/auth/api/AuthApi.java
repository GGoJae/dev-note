package com.gj.dev_note.auth.api;

import com.gj.dev_note.auth.request.*;
import com.gj.dev_note.auth.response.AuthResponse;
import com.gj.dev_note.auth.facade.PasswordResetFacade;
import com.gj.dev_note.auth.facade.RefreshFacade;
import com.gj.dev_note.auth.facade.RegistrationFacade;
import com.gj.dev_note.auth.service.AuthService;
import com.gj.dev_note.auth.service.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApi {

    private final RegistrationFacade registrationFacade;
    private final PasswordResetFacade passwordResetFacade;
    private final RefreshFacade refreshFacade;
    private final EmailVerificationService emailVerificationService;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest req) {
        registrationFacade.signup(req);
        return ResponseEntity.created(URI.create("/api/auth/login")).build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        emailVerificationService.verify(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(authService.login(req, http));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest http) {
        String ip = clientIp(http);
        String ua = http.getHeader("User-Agent");
        return ResponseEntity.ok(refreshFacade.refresh(req, ip, ua));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetFacade.issue(req.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetFacade.reset(req);
        return ResponseEntity.noContent().build();
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
