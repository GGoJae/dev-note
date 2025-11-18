package com.gj.dev_note.quizset.response;

import com.gj.dev_note.common.VisibilityDto;

import java.time.Instant;
import java.util.List;

public record QuizSetOverview(
        Long id,
        Long ownerId,
        String name,
        String description,
        VisibilityDto visibility,
        Integer itemCount,
        Instant createdAt,
        Instant updatedAt,
        List<QuizSetItemPreview> firstItems,
        Integer nextOffset
) {
}
