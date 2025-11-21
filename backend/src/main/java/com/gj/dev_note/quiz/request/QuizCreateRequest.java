package com.gj.dev_note.quiz.request;

import com.gj.dev_note.common.VisibilityDto;
import com.gj.dev_note.quiz.dto.AnswerPolicyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Set;

public record QuizCreateRequest(
        VisibilityDto visibility,

        @NotBlank
        String question,

        @NotEmpty @Size(min = 3, max = 10) @Valid
        List<QuizChoiceCreateRequest> choices,

        @NotBlank
        String explanation,

        @NotNull
        AnswerPolicyDto answerPolicy,

        @Min(1) @Max(5)
        Integer difficulty,
        Set<String> tags
) {

    public QuizCreateRequest{
        visibility = (visibility == null) ? VisibilityDto.PRIVATE : visibility;
        difficulty = (difficulty == null) ? 3 : difficulty;
        tags = (tags == null) ? Set.of() : Set.copyOf(tags);
    }

    @AssertTrue(message = "정답 정책과 정답의 개수가 일치해야 합니다.")
    public boolean isPolicyConsistent() {
        if (answerPolicy == null || choices == null ) return true;

        long correctCount = choices.stream().filter(QuizChoiceCreateRequest::correct).count();

        return switch (answerPolicy) {
            case EXACTLY_ONE -> correctCount == 1;
            case EXACTLY_TWO -> correctCount == 2;
            case MULTIPLE -> correctCount >= 2;
            case SECRET -> correctCount >= 1;
        };
    }

    public record QuizChoiceCreateRequest(
            boolean correct,
            @NotBlank @Size(max = 300)
            String text
    ) {
    }
}
