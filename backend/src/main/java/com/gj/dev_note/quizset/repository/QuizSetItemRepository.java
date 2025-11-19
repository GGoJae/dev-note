package com.gj.dev_note.quizset.repository;

import com.gj.dev_note.quizset.domain.QuizSetItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizSetItemRepository extends JpaRepository<QuizSetItem, Long> {

    boolean existsBySetIdAndQuizId(Long setId, Long quizId);

    Page<QuizSetItem> findBySetId(Long setId, Pageable pageable);

    List<QuizSetItem> findAllBySetIdOrderByOrderIndexAscIdAsc(Long setId);

    List<QuizSetItem> findAllBySetIdAndIdIn(Long setId, List<Long> itemIds);

    Optional<QuizSetItem> findByIdAndSetId(Long id, Long setId);

    long countBySetId(Long setId);
}
