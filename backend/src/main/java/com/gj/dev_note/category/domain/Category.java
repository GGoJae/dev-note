package com.gj.dev_note.category.domain;

import com.gj.dev_note.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name="category",
        indexes = {
                @Index(name="idx_category_parent", columnList = "parent_id"),
                @Index(name="idx_category_scope", columnList = "scope"),
                @Index(name="idx_category_owner", columnList = "owner_id"),
                @Index(name="idx_category_slug", columnList = "slug")
        },
        uniqueConstraints = {
                // GLOBAL: parent+slug+scope unique
                @UniqueConstraint(name="uk_cat_global", columnNames = {"parent_id","slug","scope"}),
                // PERSONAL: owner+parent+slug+scope unique
                @UniqueConstraint(name="uk_cat_personal", columnNames = {"owner_id","parent_id","slug","scope"})
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Category {

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // PERSONAL인 경우에만 세팅 (GLOBAL일 땐 null 허용)
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name="owner_id")
        private Member owner;

        @Enumerated(EnumType.STRING)
        @Column(nullable=false, length=20)
        private CategoryScope scope;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "parent_id")
        private Category parent;

        @Column(nullable = false, length = 120)
        private String name;

        @Column(nullable = false, length = 140)
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

        @PrePersist @PreUpdate
        void normalize() {
                if (slug != null) slug = slug.trim().toLowerCase();
                if (name != null) name = name.trim();
                if (depth == null) depth = (parent == null ? 0 : parent.depth + 1);
        }
}
