package com.gj.dev_note.note.domain;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.tag.domain.NoteTagMap;
import com.gj.dev_note.tag.domain.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name="note",
        indexes = {
                @Index(name="idx_note_owner", columnList = "owner_id"),
                @Index(name="idx_note_category", columnList = "category_id"),
                @Index(name="idx_note_visibility", columnList = "visibility"),
                @Index(name="idx_note_created_at", columnList="createdAt")
        }
)
@Getter @Setter(AccessLevel.PRIVATE)
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

    @OneToMany(mappedBy="note", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default
    private Set<NoteTagMap> tags = new LinkedHashSet<>();

    @Column(nullable = false)
    private Instant contentUpdatedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    void initContentUpdatedAt() {
        if (contentUpdatedAt == null) contentUpdatedAt = Instant.now();
    }

    public void renameTitle(String newTitle) {
        if (newTitle == null || newTitle.isBlank()) return;
        if (!Objects.equals(this.title, newTitle)) {
            this.title = newTitle;
            touchContentClock();
        }
    }

    public void rewriteContent(String newContent) {
        if (newContent == null || newContent.isBlank()) return;
        if (!Objects.equals(this.content, newContent)) {
            this.content = newContent;
            touchContentClock();
        }
    }

    public void changeVisibility(Visibility visibility) {
        if (visibility == null) return;
        this.visibility = visibility;
    }

    public void moveCategory(Category category) {
        if (category == null) return;
        this.category = category;
    }

    public void replaceTags(Set<Tag> newTags) {
        if (newTags == null) newTags = Set.of();

        Set<Long> cur = this.tags.stream()
                .map(ntm -> ntm.getTag().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> incoming = newTags.stream()
                .map(Tag::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (Objects.equals(cur, incoming)) return;

        this.tags.removeIf(ntm -> ntm.getTag() != null && !incoming.contains(ntm.getTag().getId()));

        Set<Long> existing = this.tags.stream()
                .map(ntm -> ntm.getTag().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Tag t : newTags) {
            if (t.getId() == null || !existing.contains(t.getId())) {
                this.tags.add(NoteTagMap.builder().note(this).tag(t).build());
            }
        }
    }

    private void touchContentClock() {
        this.contentUpdatedAt = Instant.now();
    }
}
