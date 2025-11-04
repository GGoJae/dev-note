package com.gj.dev_note.tag.domain;

import com.gj.dev_note.quiz.domain.Quiz;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "quiz_tag_map",
        uniqueConstraints = @UniqueConstraint(name="uk_quiz_tag", columnNames = {"quiz_id","tag_id"}),
        indexes = {
                @Index(name="idx_qtm_quiz", columnList="quiz_id"),
                @Index(name="idx_qtm_tag", columnList="tag_id")
        }
)
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class QuizTagMap {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="quiz_id", nullable=false)
    private Quiz quiz;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="tag_id", nullable=false)
    private Tag tag;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @Builder.Default
    private TagBindingSource source = TagBindingSource.MANUAL;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant createdAt;
}
