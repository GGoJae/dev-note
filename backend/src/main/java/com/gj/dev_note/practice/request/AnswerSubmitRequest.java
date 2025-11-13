package com.gj.dev_note.practice.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AnswerSubmitRequest(
        @NotNull
        Long sessionItemId,
        @NotEmpty
        Set<Long> selectedChoiceIds
) {
}
