package com.gj.dev_note.quiz.mapper;

import com.gj.dev_note.quiz.domain.Quiz;
import com.gj.dev_note.quiz.domain.QuizChoice;
import com.gj.dev_note.quiz.dto.AnswerPolicyDto;
import com.gj.dev_note.quiz.dto.QuizChoiceLite;
import com.gj.dev_note.quiz.dto.QuizLite;
import com.gj.dev_note.quiz.dto.QuizPlayView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuizMapper {

    public static QuizLite toLite(Quiz quiz, List<QuizChoiceLite> choicesLite) {
        return new QuizLite(
                quiz.getId(),
                quiz.getOwner().getId(),
                quiz.getQuestion(),
                quiz.getDifficulty(),
                choicesLite,
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }

    public static QuizChoiceLite toChoiceLite(QuizChoice quizChoice) {
        return new QuizChoiceLite(
                quizChoice.getId(),
                quizChoice.getText()
        );
    }

    public static QuizPlayView toPlayView(Quiz quiz) {
        return new QuizPlayView(
                quiz.getId(),
                quiz.getOwner().getId(),
                quiz.getQuestion(),
                quiz.getDifficulty(),
                AnswerPolicyDto.fromType(quiz.getAnswerPolicy()),
                toChoiceLiteList(quiz.getChoices()),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }
}
