package com.gj.dev_note.practice.domain;

import com.gj.dev_note.quiz.domain.Quiz;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="practice_session_item",
        indexes = @Index(name="idx_psi_session", columnList = "session_id"))
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class PracticeSessionItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="session_id", nullable=false)
    private PracticeSession session;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="quiz_id", nullable=false)
    private Quiz quiz;

    @Column(nullable=false)
    private int orderIndex;
}
