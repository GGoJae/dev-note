package com.gj.dev_note.deck.domain;

import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name="note_deck",
        indexes = {
                @Index(name="idx_note_deck_owner", columnList = "owner_id"),
                @Index(name="idx_note_deck_visibility", columnList = "visibility")
        },
        uniqueConstraints = @UniqueConstraint(name="uk_note_deck_owner_name",
                columnNames = {"owner_id","name"})
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class NoteDeck {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_id", nullable=false)
    private Member owner;

    @Column(nullable=false, length=120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant createdAt;
}
