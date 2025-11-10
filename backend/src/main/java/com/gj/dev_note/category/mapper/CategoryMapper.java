package com.gj.dev_note.category.mapper;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.category.dto.CategoryScopeDto;
import com.gj.dev_note.category.response.CategoryDetail;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryMapper {

    public static CategoryDetail toDetail(Category c) {
        return new CategoryDetail(
                c.getId(),
                CategoryScopeDto.fromType(c.getScope()),
                c.getOwner() == null ? null : c.getOwner().getId(),
                c.getName(),
                c.getSlug(),
                c.getParent() == null ? null : c.getParent().getId(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

}
