package com.gj.dev_note.note.response;

import com.gj.dev_note.member.dto.MemberSummary;
import com.gj.dev_note.common.VisibilityDto;

import java.time.Instant;
import java.util.List;

public record NoteResponse(
        Long id,
        MemberSummary owner,
//        CategorySummary category,
        VisibilityDto visibility,
        String title,
        String content,
        long viewCount,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt,
        Instant contentUpdatedAt,
        boolean edited
) {}
