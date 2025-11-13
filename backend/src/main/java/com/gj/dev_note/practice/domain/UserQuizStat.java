package com.gj.dev_note.practice.domain;

import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.quiz.domain.Quiz;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="user_quiz_stat",
        uniqueConstraints = @UniqueConstraint(name="uk_uqs_user_quiz", columnNames = {"owner_id","quiz_id"}),
        indexes = {
                @Index(name="idx_uqs_owner", columnList = "owner_id"),
                @Index(name="idx_uqs_quiz", columnList = "quiz_id"),
                @Index(name="idx_uqs_last", columnList = "lastTriedAt")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class UserQuizStat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="owner_id", nullable=false)
    private Member owner;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="quiz_id", nullable=false)
    private Quiz quiz;

    @Column(nullable=false)
    private int tries;

    @Column(nullable=false)
    private int corrects;

    private Instant lastTriedAt;

    @Column(nullable=false)
    private int recentWrongCount;

    public void onAttempt(boolean correct, Instant now) {
        tries++;
        if (correct) {
            corrects++;
            recentWrongCount = 0;
        } else {
            recentWrongCount++;
        }
        lastTriedAt = now;
    }
}
