package com.gj.dev_note.artifact.domain;

import com.gj.dev_note.note.domain.GroupType;
import com.gj.dev_note.note.domain.Note;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artifact_group",
        uniqueConstraints = @UniqueConstraint(name = "uk_artifact_group_slug", columnNames = "slug"),
        indexes = {
                @Index(name = "idx_artifact_group_note", columnList = "note_id"),
                @Index(name = "idx_artifact_group_type", columnList = "groupType"),
                @Index(name = "idx_artifact_group_order", columnList = "displayOrder")
        })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class ArtifactGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private GroupType groupType;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 160)
    private String slug;

    @Column(nullable = false)
    private int displayOrder;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ArtifactVariant> variants = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
