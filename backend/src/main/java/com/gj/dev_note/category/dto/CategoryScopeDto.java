package com.gj.dev_note.category.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gj.dev_note.category.domain.CategoryScope;
import com.gj.dev_note.common.enums.EnumDto;

public enum CategoryScopeDto implements EnumDto<CategoryScope, CategoryScopeDto> {
    GLOBAL("global", "글로벌", CategoryScope.GLOBAL),
    PERSONAL("personal", "개인용", CategoryScope.PERSONAL);

    private final String code;
    private final String label;
    private final CategoryScope domain;

    CategoryScopeDto(String code, String label, CategoryScope domain) {
        this.code = code;
        this.label = label;
        this.domain = domain;
    }

    @JsonValue
    public String json() {
        return code;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public CategoryScope toType() {
        return domain;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CategoryScopeDto from(String code) {
        return EnumDto.fromCode(code, CategoryScopeDto.class);
    }

    public static CategoryScopeDto fromType(CategoryScope type) {
        for (var v : values()) if (v.domain == type) return v;
        throw new IllegalArgumentException("지원하지 않는 도메인 타입 : " + type);
    }
}
