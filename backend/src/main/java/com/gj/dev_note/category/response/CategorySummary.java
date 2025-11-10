package com.gj.dev_note.category.response;

import com.gj.dev_note.category.dto.CategoryScopeDto;

import java.util.ArrayList;
import java.util.List;

public record CategorySummary(
        Long id,
        String name,
        String slug,
        Long parentId,
        CategoryScopeDto scope,
        Long ownerId,
        List<CategorySummary> children
) {
    public static CategorySummary leaf(Long id, String name, String slug, Long parentId,
                                       CategoryScopeDto scope, Long ownerId) {
        return new CategorySummary(id, name, slug, parentId, scope, ownerId, new ArrayList<>());
    }
}
