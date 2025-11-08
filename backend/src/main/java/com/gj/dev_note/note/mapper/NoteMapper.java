package com.gj.dev_note.note.mapper;

import com.gj.dev_note.common.VisibilityDto;
import com.gj.dev_note.member.dto.MemberSummary;
import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.note.response.NoteDetail;
import com.gj.dev_note.note.response.NoteSummary;
import com.gj.dev_note.tag.domain.NoteTagMap;
import com.gj.dev_note.tag.dto.TagLite;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoteMapper {

    public static NoteDetail toDetail(Note note) {
        return new NoteDetail(
                note.getId(),
                MemberSummary.fromDomain(note.getOwner()),
                VisibilityDto.fromType(note.getVisibility()),
                note.getTitle(),
                note.getContent(),
                note.getViewCount(),
                toTagLites(note),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                safeContentUpdatedAt(note),
                isEdited(note)
        );
    }

    public static NoteSummary toSummary(Note note) {
        return new NoteSummary(
                note.getId(),
                MemberSummary.fromDomain(note.getOwner()),
                VisibilityDto.fromType(note.getVisibility()),
                note.getTitle(),
                note.getViewCount(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                safeContentUpdatedAt(note),
                isEdited(note),
                makeSnippet(note.getContent(), 180) // 길이는 취향껏
        );
    }

    /* ----------------- helpers ----------------- */

    private static List<TagLite> toTagLites(Note note) {
        if (note.getTags() == null || note.getTags().isEmpty()) return List.of();

        // NoteTagMap -> Tag -> TagLite, slug 기준 중복 제거 + 안정 정렬
        Map<String, TagLite> bySlug = note.getTags().stream()
                .map(NoteTagMap::getTag)
                .filter(Objects::nonNull)
                .map(t -> TagLite.of(t.getSlug(), t.getName()))
                .collect(Collectors.toMap(
                        TagLite::slug,
                        Function.identity(),
                        (a, b) -> a, // 충돌 시 첫 값 유지
                        LinkedHashMap::new
                ));
        return bySlug.values().stream()
                .sorted(Comparator.comparing(TagLite::name, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(TagLite::slug))
                .toList();
    }

    private static boolean isEdited(Note note) {
        Instant created = note.getCreatedAt();
        Instant contentUpd = safeContentUpdatedAt(note);
        if (created == null || contentUpd == null) return false;
        // 생성 시점보다 이후면 '수정됨'
        return contentUpd.isAfter(created);
    }

    private static Instant safeContentUpdatedAt(Note note) {
        // contentUpdatedAt가 null이거나 생성시각보다 살짝 앞설 수 있으니 안전 처리
        Instant cu = note.getContentUpdatedAt();
        if (cu == null) return note.getCreatedAt();
        return cu;
    }

    private static String makeSnippet(String content, int max) {
        if (content == null) return "";
        String c = content.strip();
        if (c.length() <= max) return c;
        return c.substring(0, max).trim() + "…";
    }
}
