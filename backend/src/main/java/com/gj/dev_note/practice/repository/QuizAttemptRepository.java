package com.gj.dev_note.practice.repository;

import com.gj.dev_note.practice.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    boolean existsBySessionIdAndSessionItemIdAndOwnerId(Long sessionId, Long sessionItemId, Long ownerId);

    Optional<QuizAttempt> findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(Long sessionItemId, Long ownerId);

    long countBySessionIdAndOwnerId(Long sessionId, Long ownerId);

    long countBySessionIdAndOwnerIdAndCorrectTrue(Long sessionId, Long ownerId);

    @Query(value = """
        SELECT DISTINCT ON (qa.session_item_id) qa.*
        FROM quiz_attempt qa
        WHERE qa.session_id = :sessionId
          AND qa.owner_id = :ownerId
          AND qa.session_item_id = ANY (:itemIds)
        ORDER BY qa.session_item_id, qa.created_at DESC
        """, nativeQuery = true)
    List<QuizAttempt> findLatestBySessionOwnerAndItems(@Param("sessionId") Long sessionId,
                                                       @Param("ownerId") Long ownerId,
                                                       @Param("itemIds") Long[] itemIds);

    default List<QuizAttempt> findLatestBySessionOwnerAndItems(Long sessionId, Long ownerId, Collection<Long> itemIds) {
        return findLatestBySessionOwnerAndItems(sessionId, ownerId, itemIds.toArray(Long[]::new));
    }
}
