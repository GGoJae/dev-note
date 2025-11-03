package com.gj.dev_note.note.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="note_link",
        uniqueConstraints = @UniqueConstraint(name="uk_note_link", columnNames = {"src_id","dst_id","type"}),
        indexes = {
                @Index(name="idx_note_link_src", columnList = "src_id"),
                @Index(name="idx_note_link_dst", columnList = "dst_id")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class NoteLink {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_id", nullable = false)
    private Note source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_id", nullable = false)
    private Note target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NoteLinkType type;

    @Column(nullable=false)
    @Builder.Default
    private double weight = 1.0;
}
