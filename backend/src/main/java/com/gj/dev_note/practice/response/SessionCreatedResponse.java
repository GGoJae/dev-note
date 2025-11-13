package com.gj.dev_note.practice.response;

import com.gj.dev_note.practice.dto.SessionItemSummary;

import java.util.List;

public record SessionCreatedResponse(
        Long sessionId,
        int total,
        long seed,
        List<SessionItemSummary> firstPage,
        String nextCursor
        ) {

}
