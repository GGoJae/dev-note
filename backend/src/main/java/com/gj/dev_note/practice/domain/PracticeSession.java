package com.gj.dev_note.practice.domain;

import com.gj.dev_note.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "practice_session",
        indexes = {
                @Index(name = "idx_ps_owner", columnList = "owner_id"),
                @Index(name = "idx_ps_created", columnList = "createdAt")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class PracticeSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="owner_id", nullable=false)
    private Member owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private FeedbackMode feedbackMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Ordering ordering;

    @Column(nullable=false)
    private int totalCount;

    @Column(nullable=false)
    private long seed; // RANDOM 재현용

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    private Instant expiresAt;
    private Instant finalizedAt;

    private String finalizeToken;
    private Instant finalizeTokenExpiresAt;

    @OneToMany(mappedBy="session", cascade=CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC, id ASC")
    @Builder.Default
    private List<PracticeSessionItem> items = new ArrayList<>();

    public void addItem(PracticeSessionItem it) {
        it.setSession(this);
        items.add(it);
    }

    public boolean isFinalized() { return finalizedAt != null; }
    public void issueFinalizeToken(String token, Instant exp) {
        this.finalizeToken = token;
        this.finalizeTokenExpiresAt = exp;
    }
    public boolean canUseFinalizeToken(String token, Instant now) {
        if (finalizeToken == null || !finalizeToken.equals(token)) return false;
        if (finalizeTokenExpiresAt != null && now.isAfter(finalizeTokenExpiresAt)) return false;
        return true;
    }
}
