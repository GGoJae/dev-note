package com.gj.dev_note.category.response;

import com.gj.dev_note.category.dto.CategoryScopeDto;

import java.time.Instant;

public record CategoryDetail(
        Long id,
        CategoryScopeDto scope,
        Long ownerId,
        String name,
        String slug,
        Long parentId,
        Instant createdAt,
        Instant updatedAt
) {
}
