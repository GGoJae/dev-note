package com.gj.dev_note.auth.facade;

import com.gj.dev_note.auth.request.RefreshRequest;
import com.gj.dev_note.auth.response.AuthResponse;
import com.gj.dev_note.auth.service.RefreshTokenService;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.security.JwtIssuer;
import com.gj.dev_note.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshFacade {

    private final RefreshTokenService refreshTokenService;
    private final JwtIssuer jwtIssuer;
    private final JwtProperties jwtProps;

    @Transactional
    public AuthResponse refresh(RefreshRequest req, String ip, String userAgent) {
        Member owner = refreshTokenService.requireValidOwner(req.refreshToken());

        String newRefresh = refreshTokenService.rotate(req.refreshToken(), ip, userAgent);

        String newAccess = jwtIssuer.issue(owner.getId(), owner.getEmail(), owner.getRoles());

        return new AuthResponse(newAccess, "Bearer", jwtProps.expirationSeconds, newRefresh);
    }
}
