package com.gj.dev_note.category.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import com.gj.dev_note.category.domain.CategoryScope;

public enum CategoryScopeDto {
    GLOBAL("global", "글로벌"),
    PERSONAL("personal", "개인용");

    private final String code;
    private final String label;

    CategoryScopeDto(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static CategoryScopeDto conversionToDto(CategoryScope scope) {
        return switch (scope) {
            case GLOBAL -> CategoryScopeDto.GLOBAL;
            case PERSONAL -> CategoryScopeDto.PERSONAL;
        };
    }


}
