package com.gj.dev_note.note.repository;

import com.gj.dev_note.note.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Note n set n.viewCount = n.viewCount + :delta where n.id = :id")
    int increaseViewCount(@Param("id") Long id, @Param("delta") long delta);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    @Query("select n.version from Note n where n.id = :id")
    Optional<Long> findVersionById(@Param("id") Long id);

    boolean existsByCategoryId(Long categoryId);
}
