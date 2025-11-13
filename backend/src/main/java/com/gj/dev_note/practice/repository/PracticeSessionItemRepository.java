package com.gj.dev_note.practice.repository;

import com.gj.dev_note.practice.domain.PracticeSessionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PracticeSessionItemRepository extends JpaRepository<PracticeSessionItem, Long> {
    List<PracticeSessionItem> findAllBySessionIdOrderByOrderIndexAscIdAsc(Long sessionId);
    Optional<PracticeSessionItem> findByIdAndSessionId(Long id, Long sessionId);
}
