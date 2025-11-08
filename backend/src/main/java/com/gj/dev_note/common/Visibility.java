package com.gj.dev_note.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Visibility {
    PRIVATE,
    UNLISTED,
    PUBLIC;
}
