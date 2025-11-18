package com.gj.dev_note.quizset.request;

import com.gj.dev_note.common.VisibilityDto;

public record QuizSetPatchRequest(
        String name,
        String description,
        VisibilityDto visibility
) {
}
