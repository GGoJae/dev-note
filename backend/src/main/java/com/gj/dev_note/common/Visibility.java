package com.gj.dev_note.common;

import lombok.Getter;

@Getter
public enum Visibility {
    PRIVATE("private", "나만보기"),
    UNLISTED("unlisted", "일부공개"),
    PUBLIC("public", "전체공개");

    private final String value;
    private final String label;

    Visibility(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
