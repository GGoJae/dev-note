package com.gj.dev_note.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gj.dev_note.common.enums.CodeLabel;
import com.gj.dev_note.common.enums.EnumUtils;

import java.util.Arrays;

public enum VisibilityDto implements CodeLabel<Visibility> {
    PRIVATE("private", "비공개", Visibility.PRIVATE),
    UNLISTED("unlisted", "일부공개", Visibility.UNLISTED),
    PUBLIC("public", "전체공개", Visibility.PUBLIC);

    private final String code;
    private final String label;
    private final Visibility domain;
    private static final Class<VisibilityDto> thisClass = VisibilityDto.class;

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
        var v = EnumUtils.byCodeIndex(thisClass).get(code);
        if (v == null) {
            throw new IllegalArgumentException("알 수 없는 code: " + code +
                    " (허용 code: " + EnumUtils.allowedCodes(thisClass) + ")");
        }
        return v;
    }

    public static VisibilityDto fromType(Visibility domain) {
        return Arrays.stream(VisibilityDto.values()).filter(v -> v.domain == domain)
                .findFirst().orElseThrow(() -> new IllegalArgumentException());
    }



}
