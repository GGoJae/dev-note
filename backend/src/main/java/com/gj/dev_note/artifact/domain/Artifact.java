package com.gj.dev_note.artifact.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name="artifact",
        indexes = {
                @Index(name="idx_art_variant", columnList = "variant_id"),
                @Index(name="idx_art_kind", columnList = "kind"),
                @Index(name="idx_art_role", columnList = "role")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Artifact {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ArtifactVariant variant;

    @Column(nullable=false, length=40)
    private String kind;

    @Column(length=40)
    private String role;

    @Column(columnDefinition = "text")
    private String metaJson;

    @Lob @Column(columnDefinition = "text")
    private String content;

    @Column(length=300)
    private String contentUrl;

    @Column(length=120)
    private String filename;

    @Column(length=40)
    private String language;

    @Column(length=100)
    private String mimeType;

    private Long sizeBytes;

    @Column(length=64)
    private String sha256;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant createdAt;
}
