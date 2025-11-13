package com.gj.dev_note.practice.domain;

import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.quiz.domain.Quiz;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="quiz_attempt",
        indexes = {
                @Index(name="idx_attempt_session", columnList = "session_id"),
                @Index(name="idx_attempt_item", columnList = "session_item_id"),
                @Index(name="idx_attempt_owner", columnList = "owner_id")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class QuizAttempt {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="session_id", nullable=false)
    private PracticeSession session;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="session_item_id", nullable=false)
    private PracticeSessionItem sessionItem;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="quiz_id", nullable=false)
    private Quiz quiz;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="owner_id", nullable=false)
    private Member owner;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private boolean correct;

    @OneToMany(mappedBy="attempt", cascade=CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AttemptChoice> selected = new ArrayList<>();

    public void addChoice(AttemptChoice c) {
        c.setAttempt(this);
        selected.add(c);
    }
}
