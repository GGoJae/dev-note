package com.gj.dev_note.category.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gj.dev_note.category.domain.CategoryScope;
import com.gj.dev_note.common.enums.CodeLabel;
import com.gj.dev_note.common.enums.EnumUtils;

public enum CategoryScopeDto implements CodeLabel<CategoryScope> {
    GLOBAL("global", "글로벌", CategoryScope.GLOBAL),
    PERSONAL("personal", "개인용", CategoryScope.PERSONAL);

    private final String code;
    private final String label;
    private final CategoryScope domain;

    private final static Class<CategoryScopeDto> THIS_CLASS = CategoryScopeDto.class;

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
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "CategoryScope code는 필수입니다. 허용 코드: " + EnumUtils.allowedCodes(THIS_CLASS)
            );
        }
        var v = EnumUtils.byCodeIndex(THIS_CLASS).get(code);
        if (v == null) {
            throw new IllegalArgumentException("알 수 없는 code: " + code +
                    " (허용 code: " + EnumUtils.allowedCodes(THIS_CLASS) + ")");
        }
        return v;
    }

    public static CategoryScopeDto fromType(CategoryScope type) {
        for (var v : values()) if (v.domain == type) return v;
        throw new IllegalArgumentException("지원하지 않는 도메인 타입 : " + type);
    }
}
