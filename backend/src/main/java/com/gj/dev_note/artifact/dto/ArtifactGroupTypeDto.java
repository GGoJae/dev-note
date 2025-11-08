package com.gj.dev_note.artifact.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gj.dev_note.artifact.domain.ArtifactGroupType;
import com.gj.dev_note.common.enums.CodeLabel;
import com.gj.dev_note.common.enums.EnumUtils;

public enum ArtifactGroupTypeDto implements CodeLabel<ArtifactGroupType> {
    REFACTORING("refactoring", "리팩토링", ArtifactGroupType.REFACTORING),
    EXAMPLE_SET("example-set", "예시", ArtifactGroupType.EXAMPLE_SET),
    BEST_VS_BAD("best-vs-bad", "좋은예-나쁜예", ArtifactGroupType.BEST_VS_BAD),
    ETC("etc", "기타", ArtifactGroupType.ETC);

    private final String code;
    private final String label;
    private final ArtifactGroupType domain;
    private static final Class<ArtifactGroupTypeDto> thisClass = ArtifactGroupTypeDto.class;

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

    // JSON 직렬화 시 "refactoring" 같은 코드 문자열로 나감
    @JsonValue
    public String json() {
        return code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ArtifactGroupTypeDto from(String code) {
        if (code == null) throw new IllegalArgumentException("ArtifactGroupType 는 필수입니다.");
        ArtifactGroupTypeDto v = EnumUtils.byCodeIndex(thisClass).get(code);
        if (v == null) {
            throw new IllegalArgumentException("알 수 없는 code: " + code +
                    " (허용 code: " + EnumUtils.allowedCodes(thisClass) + ")");
        }
        return v;
    }

    public static ArtifactGroupTypeDto fromType(ArtifactGroupType type) {
        for (var v : values()) if (v.domain == type) return v;
        throw new IllegalArgumentException("Unsupported domain type: " + type);
    }

}
