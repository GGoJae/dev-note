package com.gj.dev_note.practice.repository;

import com.gj.dev_note.practice.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    boolean existsBySessionIdAndSessionItemIdAndOwnerId(Long sessionId, Long sessionItemId, Long ownerId);
}
