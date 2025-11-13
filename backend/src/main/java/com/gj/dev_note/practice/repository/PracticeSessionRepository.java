package com.gj.dev_note.practice.repository;

import com.gj.dev_note.practice.domain.PracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {
}
