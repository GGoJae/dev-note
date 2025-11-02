package com.gj.dev_note.artifact.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "artifact_variant",
        uniqueConstraints = @UniqueConstraint(name = "uk_variant_group_code", columnNames = {"group_id","variantCode"}),
        indexes = {
                @Index(name = "idx_variant_group", columnList = "group_id"),
                @Index(name = "idx_variant_order", columnList = "displayOrder")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ArtifactVariant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ArtifactGroup group;

    @Column(nullable = false, length = 40)
    private String variantCode;

    @Column(nullable = false, length = 120) //  UI 표기
    private String label;

    @Column(nullable = false)
    private int displayOrder;
}
