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

    public static QuizLite toLite(Quiz quiz, List<QuizChoiceLite> choiceLites) {
        return new QuizLite(
                quiz.getId(),
                quiz.getOwner().getId(),
                quiz.getQuestion(),
                quiz.getDifficulty(),
                choiceLites,
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }

    public static QuizPlayView toPlayView(Quiz quiz, List<QuizChoiceLite> choiceLites) {
        return new QuizPlayView(
                quiz.getId(),
                quiz.getOwner().getId(),
                quiz.getQuestion(),
                quiz.getDifficulty(),
                AnswerPolicyDto.fromType(quiz.getAnswerPolicy()),
                choiceLites,
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }
}
