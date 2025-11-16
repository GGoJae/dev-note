package com.gj.dev_note.quizset.request;

import com.gj.dev_note.common.VisibilityDto;
import jakarta.validation.constraints.NotBlank;

public record QuizSetCreateRequest(
        @NotBlank
        String name,
        String description,
        VisibilityDto visibility,
        Long categoryId
) {
}
