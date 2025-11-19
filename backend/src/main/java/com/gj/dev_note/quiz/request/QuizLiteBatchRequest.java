package com.gj.dev_note.quiz.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuizLiteBatchRequest(
        @NotEmpty
        List<Long> ids
) {
}
