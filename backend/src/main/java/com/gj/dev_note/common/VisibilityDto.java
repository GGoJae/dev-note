package com.gj.dev_note.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gj.dev_note.common.enums.EnumDto;

public enum VisibilityDto implements EnumDto<Visibility, VisibilityDto> {
    PRIVATE("private", "비공개", Visibility.PRIVATE),
    UNLISTED("unlisted", "일부공개", Visibility.UNLISTED),
    PUBLIC("public", "전체공개", Visibility.PUBLIC);

    private final String code;
    private final String label;
    private final Visibility domain;

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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VisibilityDto from(String code) {
        return EnumDto.fromCode(code, VisibilityDto.class);
    }

    public static VisibilityDto fromType(Visibility type) {
        return EnumDto.fromType(type, VisibilityDto.class);
    }

}
