package com.gj.dev_note.quizset.response;

public record QuizSetItemPreview(
        Long itemId,
        Long quizId,
        int orderIndex,
        String memo,
        boolean pinned,
        int weight
) {
}
