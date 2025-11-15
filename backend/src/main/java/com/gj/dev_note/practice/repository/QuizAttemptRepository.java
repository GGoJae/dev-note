package com.gj.dev_note.practice.repository;

import com.gj.dev_note.practice.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    boolean existsBySessionIdAndSessionItemIdAndOwnerId(Long sessionId, Long sessionItemId, Long ownerId);

    Optional<QuizAttempt> findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(Long sessionItemId, Long ownerId);

    long countBySessionIdAndOwnerId(Long sessionId, Long ownerId);

    long countBySessionIdAndOwnerIdAndCorrectTrue(Long sessionId, Long ownerId);
}
