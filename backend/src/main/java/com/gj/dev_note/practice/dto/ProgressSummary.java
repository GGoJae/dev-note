package com.gj.dev_note.practice.dto;

public record ProgressSummary(
        int attempted,
        int correct,
        int total
) {
}
