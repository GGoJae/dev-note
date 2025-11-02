package com.gj.dev_note.quiz.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;

@Entity
@Table(name = "quiz",
        indexes = @Index(name = "idx_quiz_created", columnList = "createdAt"))
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Quiz {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizType type;  // 단일 정답, 복수 정답

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(nullable = false)
    private int difficulty;     // 1 ~ 5 등급

    @Column(nullable = false)
    private boolean shuffleOptions;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    @Builder.Default
    private List<QuizOption> options = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
