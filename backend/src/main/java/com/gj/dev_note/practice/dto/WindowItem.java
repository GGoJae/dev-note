package com.gj.dev_note.practice.dto;

import java.util.Set;

public record WindowItem(
        Long sessionItemId,
        Long quizId,
        int orderIndex,
        Set<Long> mySelectedChoiceIds
) {
}
