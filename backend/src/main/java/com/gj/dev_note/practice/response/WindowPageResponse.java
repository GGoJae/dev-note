package com.gj.dev_note.practice.response;

import com.gj.dev_note.practice.dto.NextHint;
import com.gj.dev_note.practice.dto.ProgressSummary;
import com.gj.dev_note.practice.dto.WindowItem;

import java.util.List;

public record WindowPageResponse(
        Long sessionId,
        int windowSize,
        int offset,
        List<WindowItem> items,
        ProgressSummary progress,
        boolean canBacktrack,
        boolean canEdit,
        boolean canPass,
        NextHint next
) {
}
