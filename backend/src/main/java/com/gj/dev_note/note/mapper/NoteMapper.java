package com.gj.dev_note.note.mapper;

import com.gj.dev_note.common.VisibilityDto;
import com.gj.dev_note.member.dto.MemberSummary;
import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.note.response.NoteResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoteMapper {

    public static NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                MemberSummary.fromDomain(note.getOwner()),
                VisibilityDto.fromType(note.getVisibility()),
                note.getTitle(),
                note.getContent(),
                note.getViewCount(),
                List.of(),  // TODO Tags 제대로 넣기
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.getContentUpdatedAt(),
                note.getContentUpdatedAt().equals(note.getCreatedAt())
        );
    }

}
