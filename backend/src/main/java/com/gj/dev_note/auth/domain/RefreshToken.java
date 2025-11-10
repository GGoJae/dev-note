package com.gj.dev_note.auth.domain;

import com.gj.dev_note.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "refresh_token",
        indexes = {
                @Index(name="idx_rt_token", columnList="token", unique = true),
                @Index(name="idx_rt_member", columnList="member_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable=false)
    private Member member;

    @Column(nullable=false, length=300, unique=true)
    private String token;

    @Column(nullable=false)
    private Instant expiresAt;

    private Instant revokedAt;

    private String replacedByToken;

    private String createdByIp;
    private String userAgent;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    public boolean isActive() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }
}
