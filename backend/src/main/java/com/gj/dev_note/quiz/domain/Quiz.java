package com.gj.dev_note.quiz.domain;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.quiz.model.AnswerPolicy;
import com.gj.dev_note.tag.domain.QuizTagMap;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="quiz",
        indexes = {
                @Index(name="idx_quiz_owner", columnList = "owner_id"),
                @Index(name="idx_quiz_visibility", columnList = "visibility"),
                @Index(name="idx_quiz_category", columnList="category_id"),
                @Index(name="idx_quiz_created_at", columnList="createdAt")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Quiz {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="owner_id", nullable=false)
    private Member owner;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="owner_note_id")
    private Note ownerNote;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Column(columnDefinition="text")
    private String explanation;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    @Builder.Default
    private List<QuizChoice> choices = new ArrayList<>();

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    @Builder.Default private AnswerPolicy answerPolicy = AnswerPolicy.SINGLE_ANSWER;

    @Column(nullable = false)
    private int difficulty = 3;     // 1 ~ 5 등급

    @OneToMany(mappedBy="quiz", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default
    private Set<QuizTagMap> tagMaps = new HashSet<>();

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
