package com.gj.dev_note.tag.domain;

import com.gj.dev_note.note.domain.Note;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "note_tag_map",
        uniqueConstraints = @UniqueConstraint(name="uk_note_tag", columnNames = {"note_id","tag_id"}),
        indexes = {
                @Index(name="idx_ntm_note", columnList="note_id"),
                @Index(name="idx_ntm_tag", columnList="tag_id")
        }
)
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class NoteTagMap {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="note_id", nullable=false)
    private Note note;

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
