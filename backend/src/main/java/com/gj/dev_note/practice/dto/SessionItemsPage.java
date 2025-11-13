package com.gj.dev_note.practice.dto;

import java.util.List;

public record SessionItemsPage(
        List<SessionItemSummary> items,
        String nextCursor
) {
}
