package com.gj.dev_note.quizset.response;

import java.util.List;

public record QuizSetItemsPage(
        List<QuizSetItemSummary> items,
        Integer nextOffset
) {
}
