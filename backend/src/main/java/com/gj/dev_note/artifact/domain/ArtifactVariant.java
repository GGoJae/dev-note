package com.gj.dev_note.artifact.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="artifact_variant",
        indexes = @Index(name="idx_art_variant_group", columnList = "group_id")
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ArtifactVariant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="group_id", nullable=false)
    private ArtifactGroup group;

    @Column(nullable=false, length=80)
    private String variantKey;

    @Column(length=200)
    private String title;

    @Column(nullable=false)
    @Builder.Default
    private int displayOrder = 0;
}
