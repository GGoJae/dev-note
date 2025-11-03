package com.gj.dev_note.quiz.domain;

import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="quiz",
        indexes = {
                @Index(name="idx_quiz_owner", columnList = "owner_id"),
                @Index(name="idx_quiz_visibility", columnList = "visibility")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Quiz {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_id", nullable=false)
    private Member owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    @Builder.Default
    private List<QuizChoice> choices = new ArrayList<>();

    @Column(name="correct_choice_id")
    private Long correctChoiceId;

    @Column(nullable = false)
    private int difficulty;     // 1 ~ 5 등급

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public void addChoice(QuizChoice c) {
        c.setQuiz(this);
        choices.add(c);
    }
}
