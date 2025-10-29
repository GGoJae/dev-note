package com.gj.dev_note.note.mapper;

import com.gj.dev_note.note.entity.Note;
import com.gj.dev_note.note.response.NoteResponse;

public abstract class NoteMapper {

    public static NoteResponse repoToResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                false
        );
    }

    public static NoteResponse cacheToResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                true
        );
    }
}
