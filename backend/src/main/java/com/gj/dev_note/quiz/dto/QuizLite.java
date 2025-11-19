package com.gj.dev_note.quiz.dto;

import java.time.Instant;
import java.util.List;

public record QuizLite(
        Long id,
        Long ownerId,
        String question,
        int difficulty,
        List<QuizChoiceLite> quizChoice,
        Instant createdAt,
        Instant updatedAt
) {
}
