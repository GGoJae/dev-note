package com.gj.dev_note.note.mapper;

import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.note.response.NoteResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoteMapper {

    public static NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt()
        );
    }

}
