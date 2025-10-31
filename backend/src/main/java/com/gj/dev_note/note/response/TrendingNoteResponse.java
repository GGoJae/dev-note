package com.gj.dev_note.note.response;

public record TrendingNoteResponse(
        NoteResponse note,
        double score
) {
}
