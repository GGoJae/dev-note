package com.gj.dev_note.category.dto;

import com.gj.dev_note.member.dto.MemberSummary;

import java.time.Instant;

public record CategoryDto(
        Long id,
        MemberSummary owner,
        CategoryScopeDto categoryScope,
        Long parent_id,
        String name,
        String slug,
        Integer depth,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="owner_id")
//    private Member owner;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable=false, length=20)
//    private CategoryScope scope;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "parent_id")
//    private com.gj.dev_note.category.domain.Category parent;
//
//    @Column(nullable = false, length = 120)
//    private String name;
//
//    @Column(nullable = false, length = 140)
//    private String slug;            // URL, PATH 용
//
//    @Column(nullable = false)
//    private Integer depth;          // 루트 = 0
//
//    @Column(length = 500)
//    private String description;
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private Instant createdAt;
//
//    @UpdateTimestamp
//    @Column(nullable = false)
//    private Instant updatedAt;
//
//    @PrePersist @PreUpdate
//    void normalize() {
//        if (slug != null) slug = slug.trim().toLowerCase();
//        if (name != null) name = name.trim();
//        if (depth == null) depth = (parent == null ? 0 : parent.depth + 1);
//    }
//}
