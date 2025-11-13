package com.gj.dev_note.practice.repository;

import com.gj.dev_note.practice.domain.UserQuizStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserQuizStatRepository extends JpaRepository<UserQuizStat, Long> {
    Optional<UserQuizStat> findByOwnerIdAndQuizId(Long ownerId, Long quizId);
}
