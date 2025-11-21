package com.gj.dev_note.quiz.dto;

import java.time.Instant;
import java.util.List;

public record QuizPlayView(
        Long id,
        Long ownerId,
        String question,
        int difficulty,
        AnswerPolicyDto answerPolicy,
        List<QuizChoiceLite> choices,
        Instant createdAt,
        Instant updatedAt
) {
}
