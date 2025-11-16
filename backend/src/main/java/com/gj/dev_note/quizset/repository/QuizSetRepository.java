package com.gj.dev_note.quizset.repository;

import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.quizset.domain.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizSetRepository extends JpaRepository<QuizSet, Long> {

    List<QuizSet> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<QuizSet> findByIdAndVisibility(Long id, Visibility visibility);
}
