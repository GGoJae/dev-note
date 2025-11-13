package com.gj.dev_note.practice.request;

import com.gj.dev_note.practice.dto.FeedbackModeDto;
import com.gj.dev_note.practice.dto.OrderingDto;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record SessionCreateRequest(
        @NotEmpty
        Set<Long> quizIds,
        OrderingDto ordering,
        FeedbackModeDto feedbackMode,
        Integer limit
) {

    private static final int DEFAULT_QUIZ_COUNT = 20;
    private static final int MIN_QUIZ_COUNT = 5;
    private static final int MAX_QUIZ_COUNT = 60;

    public SessionCreateRequest {
        ordering = (ordering == null) ? OrderingDto.SEQUENTIAL : ordering;
        feedbackMode = (feedbackMode == null) ? FeedbackModeDto.SECTION_END : feedbackMode;
        limit = (limit == null)
                ? DEFAULT_QUIZ_COUNT :
                (limit > MAX_QUIZ_COUNT)
                        ? MAX_QUIZ_COUNT :
                        (limit < MIN_QUIZ_COUNT)
                                ? MIN_QUIZ_COUNT : limit;
    }

}
