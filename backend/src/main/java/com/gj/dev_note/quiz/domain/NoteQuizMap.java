package com.gj.dev_note.quiz.domain;

import com.gj.dev_note.note.domain.Note;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="note_quiz_map",
        uniqueConstraints = @UniqueConstraint(name="uk_note_quiz", columnNames = {"note_id","quiz_id"}),
        indexes = {
                @Index(name="idx_nqm_note", columnList = "note_id"),
                @Index(name="idx_nqm_quiz", columnList = "quiz_id")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class NoteQuizMap {

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "note_id", nullable = false)
        private Note note;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "quiz_id", nullable = false)
        private Quiz quiz;

        @Column(nullable=false)
        @Builder.Default
        private int displayOrder = 0;

        @Column(nullable=false)
        @Builder.Default
        private double relevance = 1.0;
}
