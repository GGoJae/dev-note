package com.gj.dev_note.note.response;

import com.gj.dev_note.common.VisibilityDto;
import com.gj.dev_note.member.dto.MemberSummary;

import java.time.Instant;

public record NoteSummary(
        Long id,
        MemberSummary owner,
        VisibilityDto visibility,
        String title,
        long viewCount,
        Instant createdAt,
        Instant updatedAt,
        Instant contentUpdatedAt,
        boolean edited,
        String snippet
) {
}
