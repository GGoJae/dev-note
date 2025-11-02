package com.gj.dev_note.quiz.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "quiz_option",
        indexes = {
                @Index(name = "idx_quiz_option_quiz", columnList = "quiz_id"),
                @Index(name = "idx_quiz_option_order", columnList = "displayOrder"),
                @Index(name = "idx_quiz_option_correct", columnList = "correct")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuizOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int displayOrder;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
