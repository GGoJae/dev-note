package com.gj.dev_note.category.request;

public record CategoryPatchRequest(
        String name,
        Long parentId
) {
}
