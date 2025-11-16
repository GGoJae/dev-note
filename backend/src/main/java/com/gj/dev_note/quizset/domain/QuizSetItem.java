package com.gj.dev_note.quizset.domain;

import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.quiz.domain.Quiz;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "quiz_set_item",
        uniqueConstraints = @UniqueConstraint(name="uk_qsi_set_quiz", columnNames = {"set_id","quiz_id"}),
        indexes = {
                @Index(name="idx_qsi_set", columnList = "set_id"),
                @Index(name="idx_qsi_set_order", columnList = "set_id,orderIndex")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class QuizSetItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="set_id", nullable=false)
    private QuizSet set;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="quiz_id", nullable=false)
    private Quiz quiz;

    @Column(nullable=false)
    private int orderIndex;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="added_by", nullable=false)
    private Member addedBy;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant addedAt;

    @Column(length=200)
    private String aliasTitle;

    @Column(nullable=false)
    @Builder.Default
    private boolean pinned = false;

    @Column(nullable=false)
    @Builder.Default
    private int weight = 0;

    @Column(name="quiz_version_id")
    private Long quizVersionId;
}
