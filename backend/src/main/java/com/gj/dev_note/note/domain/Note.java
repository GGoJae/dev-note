package com.gj.dev_note.note.domain;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.tag.domain.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "note",
        indexes = {
        @Index(name = "idx_note_category", columnList = "category_id"),
                @Index(name = "idx_note_created", columnList = "createdAt")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long viewCount = 0;

    @ManyToMany
    @JoinTable(
            name = "note_tag",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_note_tag", columnNames = {"note_id", "tag_id"}),
            indexes = {
                    @Index(name = "idx_note_tag_note", columnList = "note_id"),
                    @Index(name = "idx_note_tag_tag", columnList = "tag_id")
            }
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
