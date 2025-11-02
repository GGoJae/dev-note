package com.gj.dev_note.category.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "category",
        indexes = {
                @Index(name = "idx_category_parent", columnList = "parent_id"),
                @Index(name = "idx_category_slug", columnList = "slug")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_parent_slug", columnNames = {"parent_id", "slug"})
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Category {

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "parent_id")
        private Category parent;

        @Column(nullable = false, length = 100)
        private String name;

        @Column(nullable = false, length = 120)
        private String slug;            // URL, PATH 용

        @Column(nullable = false)
        private Integer depth;          // 루트 = 0

        @Column(length = 500)
        private String description;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private Instant createdAt;

        @UpdateTimestamp
        @Column(nullable = false)
        private Instant updatedAt;
}
