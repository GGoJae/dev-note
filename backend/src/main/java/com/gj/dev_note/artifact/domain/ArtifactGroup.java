package com.gj.dev_note.artifact.domain;

import com.gj.dev_note.note.domain.Note;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="artifact_group",
        indexes = {
                @Index(name="idx_art_group_note", columnList = "note_id"),
                @Index(name="idx_art_group_type", columnList = "groupType"),
                @Index(name="idx_art_group_key", columnList = "logicalKey")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class ArtifactGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private ArtifactGroupType groupType;

    @Column(nullable=false, length=120)
    private String logicalKey; // ex) "refactor-1", "example-2"

    @Column(nullable=false)
    @Builder.Default
    private int displayOrder = 0;
}
