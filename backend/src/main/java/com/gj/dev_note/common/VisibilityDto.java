package com.gj.dev_note.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gj.dev_note.common.enums.CodeLabel;
import com.gj.dev_note.common.enums.EnumUtils;

public enum VisibilityDto implements CodeLabel<Visibility> {
    PRIVATE("private", "비공개", Visibility.PRIVATE),
    UNLISTED("unlisted", "일부공개", Visibility.UNLISTED),
    PUBLIC("public", "전체공개", Visibility.PUBLIC);

    private final String code;
    private final String label;
    private final Visibility domain;
    private static final Class<VisibilityDto> THIS_CLASS = VisibilityDto.class;

    VisibilityDto(String code, String label, Visibility domain) {
        this.code = code;
        this.label = label;
        this.domain = domain;
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
    public Visibility toType() {
        return domain;
    }

    @JsonValue
    public String json() {
        return code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VisibilityDto from(String code) {
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

    public static VisibilityDto fromType(Visibility type) {
        for (var v : values()) if (v.domain == type) return v;
        throw new IllegalArgumentException("지원하지 않는 도메인 타입 : " + type);
    }



}
