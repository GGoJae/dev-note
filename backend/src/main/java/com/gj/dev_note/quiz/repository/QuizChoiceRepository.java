package com.gj.dev_note.quiz.repository;

import com.gj.dev_note.quiz.domain.QuizChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuizChoiceRepository extends JpaRepository<QuizChoice, Long> {

    List<QuizChoice> findAllByQuizIdInOrderByQuizIdAscDisplayOrderAscIdAsc(Collection<Long> quizIds);

    List<QuizChoice> findAllByQuizIdOrderByDisplayOrderAscIdAsc(Long quizId);
}
