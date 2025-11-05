package com.gj.dev_note.artifact.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gj.dev_note.artifact.domain.ArtifactGroupType;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum ArtifactGroupTypeDto {
    REFACTORING("refactoring", "리팩토링"),
    EXAMPLE_SET("example-set", "예시"),
    BEST_VS_BAD("best-and-bad", "좋은예-나쁜예"),
    ETC("etc", "기타");

    private final String code;
    private final String label;

    ArtifactGroupTypeDto(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ArtifactGroupTypeDto from(String rawName) {
        String lowerCase = rawName.toLowerCase(Locale.ROOT);
        return switch (lowerCase) {
            case "refactoring", "refactor", "리팩토링", "리팩터" -> REFACTORING;
            case "example-set", "examples", "example", "예시", "예", "예시들" -> EXAMPLE_SET;
            case "best-and-bad", "best-bad", "bestandbad", "bestbad" -> BEST_VS_BAD;
            case "etc", "기타", "등등", "기타등등", "그외" -> ETC;

            default -> throw new IllegalStateException("Unexpected value: " + rawName);
        };
    }

    public ArtifactGroupType toType() {
        return switch (this) {
            case REFACTORING -> ArtifactGroupType.REFACTORING;
            case EXAMPLE_SET -> ArtifactGroupType.EXAMPLE_SET;
            case BEST_VS_BAD -> ArtifactGroupType.BEST_VS_BAD;
            case ETC -> ArtifactGroupType.ETC;
        };
    }

    public static ArtifactGroupTypeDto fromType(ArtifactGroupType type) {
        return switch (type) {
            case REFACTORING -> REFACTORING;
            case EXAMPLE_SET -> EXAMPLE_SET;
            case BEST_VS_BAD -> BEST_VS_BAD;
            case ETC -> ETC;
        };
    }

    public static List<ArtifactGroupValues> showValues() {
        return Arrays.stream(ArtifactGroupType.values())
                .map(ArtifactGroupTypeDto::fromType)
                .map(ArtifactGroupValues::getValue)
                .toList();
    }

    public record ArtifactGroupValues(
            String name,
            String label
    ) {
        private static ArtifactGroupValues getValue(ArtifactGroupTypeDto dto) {
            return new ArtifactGroupValues(dto.code, dto.label);
        }
    }

}
