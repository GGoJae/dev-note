package com.gj.dev_note.note.response;

import java.time.Instant;

public record NoteResponse(
        Long id,
        String title,
        String content,
        Instant createdAt,
        boolean caching
) {}
