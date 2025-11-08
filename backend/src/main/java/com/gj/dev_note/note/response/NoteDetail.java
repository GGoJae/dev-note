package com.gj.dev_note.note.response;

import com.gj.dev_note.common.VisibilityDto;
import com.gj.dev_note.member.dto.MemberSummary;
import com.gj.dev_note.tag.dto.TagLite;

import java.time.Instant;
import java.util.List;

public record NoteDetail(
        Long id,
        MemberSummary owner,
//        CategorySummary category, // TODO CategorySummary 만들면 주석 해제
        VisibilityDto visibility,
        String title,
        String content,
        long viewCount,
        List<TagLite> tags,
        Instant createdAt,
        Instant updatedAt,
        Instant contentUpdatedAt,
        boolean edited
) {}
