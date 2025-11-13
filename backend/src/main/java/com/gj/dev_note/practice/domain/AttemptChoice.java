package com.gj.dev_note.practice.domain;

import com.gj.dev_note.quiz.domain.QuizChoice;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="attempt_choice",
        indexes = @Index(name="idx_ac_attempt", columnList = "attempt_id"))
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class AttemptChoice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="attempt_id", nullable=false)
    private QuizAttempt attempt;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="choice_id", nullable=false)
    private QuizChoice choice;
}
