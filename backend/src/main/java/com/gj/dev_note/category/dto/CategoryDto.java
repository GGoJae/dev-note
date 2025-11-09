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