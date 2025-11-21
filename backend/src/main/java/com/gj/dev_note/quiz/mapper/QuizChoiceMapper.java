package com.gj.dev_note.quiz.mapper;

import com.gj.dev_note.quiz.domain.QuizChoice;
import com.gj.dev_note.quiz.dto.QuizChoiceLite;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuizChoiceMapper {

    public static QuizChoiceLite toLite(QuizChoice quizChoice) {
        return new QuizChoiceLite(
                quizChoice.getId(),
                quizChoice.getText()
        );
    }

}
