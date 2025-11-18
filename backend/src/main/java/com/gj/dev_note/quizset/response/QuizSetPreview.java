package com.gj.dev_note.quizset.response;

import com.gj.dev_note.common.VisibilityDto;

import java.time.Instant;

public record QuizSetPreview(
        Long id,
        Long ownerId,
        String name,
        String description,
        VisibilityDto visibility,
        int itemCount,
        Instant createdAt,
        Instant updatedAt
) {
}
