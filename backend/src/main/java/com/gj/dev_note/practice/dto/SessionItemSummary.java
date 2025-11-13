package com.gj.dev_note.practice.dto;

public record SessionItemSummary(
        Long sessionItemId,
        Long quizId,
        int orderIndex
) {}
