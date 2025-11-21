package com.gj.dev_note.quiz.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gj.dev_note.common.enums.EnumDto;
import com.gj.dev_note.quiz.domain.AnswerPolicy;

public enum AnswerPolicyDto implements EnumDto<AnswerPolicy, AnswerPolicyDto> {
    EXACTLY_ONE("single", "정답 1개", AnswerPolicy.EXACTLY_ONE),
    EXACTLY_TWO("two", "정답 2개", AnswerPolicy.EXACTLY_TWO),
    MULTIPLE("multiple", "정답 여러 개", AnswerPolicy.MULTIPLE),
    SECRET("secret", "정답 개수 비공개", AnswerPolicy.SECRET);

    private final String code;
    private final String label;
    private final AnswerPolicy domain;

    AnswerPolicyDto(String code, String label, AnswerPolicy domain) {
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
    public AnswerPolicy toType() {
        return domain;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AnswerPolicyDto from(String code) {
        return EnumDto.fromCode(code, AnswerPolicyDto.class);
    }

    public static AnswerPolicyDto fromType(AnswerPolicy type) {
        return EnumDto.fromType(type, AnswerPolicyDto.class);
    }

}
