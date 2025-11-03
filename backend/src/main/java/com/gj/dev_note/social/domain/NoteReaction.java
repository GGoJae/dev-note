package com.gj.dev_note.social.domain;

import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.note.domain.Note;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name="note_reaction",
        uniqueConstraints = @UniqueConstraint(name="uk_note_reaction",
                columnNames = {"member_id","note_id","type"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NoteReaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="member_id", nullable=false)
    private Member member;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="note_id", nullable=false)
    private Note note;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private ReactionType type;

    @Column(length=500)
    private String comment; // 선택적 코멘트

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant createdAt;
}
