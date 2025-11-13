package com.gj.dev_note.artifact.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gj.dev_note.artifact.domain.ArtifactGroupType;
import com.gj.dev_note.common.enums.EnumDto;

public enum ArtifactGroupTypeDto implements EnumDto<ArtifactGroupType, ArtifactGroupTypeDto> {
    REFACTORING("refactoring", "리팩토링", ArtifactGroupType.REFACTORING),
    EXAMPLE_SET("example-set", "예시", ArtifactGroupType.EXAMPLE_SET),
    BEST_VS_BAD("best-vs-bad", "좋은예-나쁜예", ArtifactGroupType.BEST_VS_BAD),
    ETC("etc", "기타", ArtifactGroupType.ETC);

    private final String code;
    private final String label;
    private final ArtifactGroupType domain;

    ArtifactGroupTypeDto(String code, String label, ArtifactGroupType domain) {
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
    public ArtifactGroupType toType() {
        return domain;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ArtifactGroupTypeDto from(String code) {
        return EnumDto.fromCode(code, ArtifactGroupTypeDto.class);
    }

    public static ArtifactGroupTypeDto fromType(ArtifactGroupType type) {
        return EnumDto.fromType(type, ArtifactGroupTypeDto.class);
    }

}
