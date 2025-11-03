package com.gj.dev_note.deck.domain;

import com.gj.dev_note.note.domain.Note;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="note_deck_item",
        uniqueConstraints = @UniqueConstraint(name="uk_note_deck_item",
                columnNames = {"deck_id","note_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NoteDeckItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="deck_id", nullable=false)
    private NoteDeck deck;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="note_id", nullable=false)
    private Note note;

    @Column(nullable=false)
    @Builder.Default
    private int displayOrder = 0;

    @Column(nullable=false)
    @Builder.Default
    private Instant addedAt = Instant.now();
}
