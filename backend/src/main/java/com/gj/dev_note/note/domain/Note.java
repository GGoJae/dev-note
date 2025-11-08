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
import java.util.Set;

@Entity
@Table(name="note",
        indexes = {
                @Index(name="idx_note_owner", columnList = "owner_id"),
                @Index(name="idx_note_category", columnList = "category_id"),
                @Index(name="idx_note_visibility", columnList = "visibility"),
                @Index(name="idx_note_created_at", columnList="createdAt")
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

    @OneToMany(mappedBy="note", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default
    private Set<NoteTagMap> tags = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Instant contentUpdatedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void initContentUpdatedAt() {
        if (contentUpdatedAt == null) contentUpdatedAt = Instant.now();
    }

    public void editContent(String title, String content, Set<Tag> tags) {
        this.title =title;
        this.content = content;
        // TODO tag 교체 로직 따로 작성 후에 추가
        this.contentUpdatedAt = Instant.now();
    }
}
