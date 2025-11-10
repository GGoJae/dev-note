package com.gj.dev_note.auth.repository;

import com.gj.dev_note.auth.domain.RefreshToken;
import com.gj.dev_note.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByMember(Member member);
}
