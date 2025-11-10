package com.gj.dev_note.category.request;

import com.gj.dev_note.category.dto.CategoryScopeDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        CategoryScopeDto scope,
        Long parentId,
        @NotBlank @Size(max = 100)
        String name
) {
}
