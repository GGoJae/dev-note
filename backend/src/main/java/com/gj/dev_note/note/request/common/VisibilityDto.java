package com.gj.dev_note.note.request.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum VisibilityDto {
    PRIVATE("private"),
    UNLISTED("unlisted"),
    PUBLIC("public");

    private final String scope;

    VisibilityDto(String scope) {
        this.scope = scope;
    }

    @JsonValue
    public String getScope() {
        return scope;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VisibilityDto from(String rawValue) {
        return Arrays.stream(values())
                .filter(v -> v.scope.equalsIgnoreCase(rawValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 Visibility 값입니다."));
    }
}
