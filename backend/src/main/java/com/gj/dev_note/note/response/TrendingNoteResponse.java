package com.gj.dev_note.note.response;

public record TrendingNoteResponse(
        NoteSummary note,
        double score
) {
}
