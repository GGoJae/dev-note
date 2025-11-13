package com.gj.dev_note.practice.response;

import java.util.List;
import java.util.Set;

public record FinalizeResponse(
        int attemptCount,
        int correctCount,
        List<FinalizedItem> items
) {
    public record FinalizedItem(
            Long sessionId,
            Long quizId,
            boolean correct,
            Set<Long> selectedChoiceIds,
            Set<Long> correctChoiceIds
    ) {

    }
}
