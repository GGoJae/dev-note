package com.gj.dev_note.note.domain;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.tag.domain.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="note",
        indexes = {
                @Index(name="idx_note_owner", columnList = "owner_id"),
                @Index(name="idx_note_category", columnList = "category_id"),
                @Index(name="idx_note_visibility", columnList = "visibility")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private long viewCount = 0L;

    @ManyToMany
    @JoinTable(name="note_tag",
            joinColumns = @JoinColumn(name="note_id"),
            inverseJoinColumns = @JoinColumn(name="tag_id"),
            uniqueConstraints = @UniqueConstraint(name="uk_note_tag", columnNames={"note_id","tag_id"})
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

}
