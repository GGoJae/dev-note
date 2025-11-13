package com.gj.dev_note.quiz.repository;

import com.gj.dev_note.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
