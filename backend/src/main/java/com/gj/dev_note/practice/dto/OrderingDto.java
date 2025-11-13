package com.gj.dev_note.practice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gj.dev_note.common.enums.EnumDto;
import com.gj.dev_note.practice.domain.Ordering;

public enum OrderingDto implements EnumDto<Ordering, OrderingDto> {
    RANDOM("random", "무작위", Ordering.RANDOM),
    SEQUENTIAL("sequential", "일반", Ordering.SEQUENTIAL);

    private final String code;
    private final String label;
    private final Ordering domain;

    OrderingDto(String code, String label, Ordering domain) {
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
    public Ordering toType() {
        return domain;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OrderingDto from(String code) {
        return EnumDto.fromCode(code, OrderingDto.class);
    }

    public static OrderingDto fromType(Ordering type) {
        return EnumDto.fromType(type, OrderingDto.class);
    }


}
