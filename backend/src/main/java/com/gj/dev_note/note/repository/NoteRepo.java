package com.gj.dev_note.note.repository;

import com.gj.dev_note.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepo extends JpaRepository<Note, Long> {
}
