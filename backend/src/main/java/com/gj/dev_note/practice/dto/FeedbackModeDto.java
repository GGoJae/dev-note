package com.gj.dev_note.practice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gj.dev_note.common.enums.EnumDto;
import com.gj.dev_note.practice.domain.FeedbackMode;

public enum FeedbackModeDto implements EnumDto<FeedbackMode, FeedbackModeDto> {
//    IMMEDIATE("immediate", "즉시", FeedbackMode.IMMEDIATE),
    SECTION_END("section-end", "섹션 종료 후", FeedbackMode.SECTION_END),
    UNTIL_CORRECT("until-correct", "맞출때까지", FeedbackMode.UNTIL_CORRECT);

    private final String code;
    private final String label;
    private final FeedbackMode domain;

    FeedbackModeDto(String code, String label, FeedbackMode domain) {
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
    public FeedbackMode toType() {
        return domain;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FeedbackModeDto from(String code) {
        return EnumDto.fromCode(code, FeedbackModeDto.class);
    }

    public static FeedbackModeDto fromType(FeedbackMode type) {
        return EnumDto.fromType(type, FeedbackModeDto.class);
    }
}
