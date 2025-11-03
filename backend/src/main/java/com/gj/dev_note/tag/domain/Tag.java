package com.gj.dev_note.tag.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name="tag",
        indexes = {
                @Index(name="idx_tag_slug", columnList = "slug", unique = true)
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 140, unique = true)
    private String slug;

    @Column(nullable = false)
    @Builder.Default
    private Long usageCount = 0L;

    @PrePersist @PreUpdate
    void normalize() {
        if (slug != null) slug = slug.trim().toLowerCase();
        if (name != null) name = name.trim();
    }
}
