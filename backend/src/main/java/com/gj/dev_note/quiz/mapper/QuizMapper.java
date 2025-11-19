package com.gj.dev_note.quiz.mapper;

import com.gj.dev_note.quiz.domain.Quiz;
import com.gj.dev_note.quiz.domain.QuizChoice;
import com.gj.dev_note.quiz.dto.QuizChoiceLite;
import com.gj.dev_note.quiz.dto.QuizLite;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuizMapper {


    public static QuizLite toLite(Quiz quiz) {
        return new QuizLite(
                quiz.getId(),
                quiz.getOwner().getId(),
                quiz.getQuestion(),
                quiz.getExplanation(),
                quiz.getDifficulty(),
                toLiteList(quiz.getChoices()),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }

    private static List<QuizChoiceLite> toLiteList(List<QuizChoice> quizChoices) {
        List<QuizChoiceLite> out = quizChoices.stream()
                .map(QuizMapper::toLite)
                .toList();
        return List.copyOf(out);
    }

    private static QuizChoiceLite toLite(QuizChoice quizChoice) {
        return new QuizChoiceLite(
                quizChoice.getId(),
                quizChoice.getText()
        );
    }
}
