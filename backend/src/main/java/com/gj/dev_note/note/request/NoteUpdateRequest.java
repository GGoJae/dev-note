package com.gj.dev_note.note.request;

import com.gj.dev_note.note.request.common.VisibilityDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

// 전체 갈아끼우기 PUT 용
public record NoteUpdateRequest(
        @NotBlank @Size(max = 200)
        String title,

        @NotBlank
        String content,

        VisibilityDto visibility,

        Long categoryId,

        Set<@Size(min=1, max=120) String> tagSlugs
) {
    public NoteUpdateRequest {
        tagSlugs = (tagSlugs == null) ? Set.of() : Set.copyOf(tagSlugs);
    }
}
