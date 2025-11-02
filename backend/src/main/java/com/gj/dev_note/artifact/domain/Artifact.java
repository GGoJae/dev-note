package com.gj.dev_note.artifact.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "artifact",
        uniqueConstraints = @UniqueConstraint(name = "uk_artifact_variant_logical", columnNames = {"variant_id","logicalKey"}),
        indexes = {
                @Index(name = "idx_artifact_variant", columnList = "variant_id"),
                @Index(name = "idx_artifact_kind", columnList = "kind"),
                @Index(name = "idx_artifact_role", columnList = "role"),
                @Index(name = "idx_artifact_order", columnList = "displayOrder")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Artifact {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ArtifactVariant variant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ArtifactKind kind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ArtifactRole role;

    @Column(length = 60)
    private String language;

    @Column(nullable = false, length = 160)
    private String logicalKey;

    @Column(length = 200)
    private String filename;

    @Column(length = 300)
    private String path;

    @Lob
    @Column(columnDefinition = "text")
    private String contentText;     // 컨텐트 소용량

    @Column(length = 500)
    private String contentUrl;      // 대용량  나중에 s3 고려

    private Long size;
    @Column(length = 80)
    private String sha256;

    @Column(nullable = false)
    private int displayOrder;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
