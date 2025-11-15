package com.gj.dev_note.practice.response;

public record ResumeResponse(
        Long sessionId,
        int windowSize,
        int pivotIndex,
        WindowPageResponse window
) {
}
