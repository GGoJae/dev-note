package com.gj.dev_note.quizset.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddQuizzesRequest(
        @NotEmpty
        List<Long> quizIds
) {
}
