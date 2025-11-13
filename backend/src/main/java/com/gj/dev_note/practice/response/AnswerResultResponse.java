package com.gj.dev_note.practice.response;

import java.util.Set;

public record AnswerResultResponse(
        boolean correct,
        Integer remainingCorrect,
        Set<Long> correctChoiceIds
) {
}
