package com.gj.dev_note.note.request;

import com.gj.dev_note.common.VisibilityDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record NoteCreateRequest(
        @NotBlank @Size(max = 200)
        String title,

        @NotBlank
        String content,

        VisibilityDto visibility,

        Long categoryId,

        Set<@Size(min = 1, max = 200) String> tagSlugs
) {
        public NoteCreateRequest {
                tagSlugs = (tagSlugs == null) ? Set.of() : Set.copyOf(tagSlugs);
        }
}
