package com.gj.dev_note.quiz.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="quiz_choice",
        indexes = @Index(name="idx_quiz_choice_quiz", columnList = "quiz_id")
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class QuizChoice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="quiz_id", nullable=false)
    private Quiz quiz;

    @Column(nullable=false, length=300)
    private String text;

    @Column(nullable=false) @Builder.Default
    private boolean correct = false;

    @Column(nullable=false)
    @Builder.Default
    private int displayOrder = 0;
}
