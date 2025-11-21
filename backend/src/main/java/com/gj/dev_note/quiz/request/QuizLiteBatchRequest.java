package com.gj.dev_note.quiz.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizLiteBatchRequest(
        @NotEmpty @Size(max = 100)
        List<Long> ids
) {
}
