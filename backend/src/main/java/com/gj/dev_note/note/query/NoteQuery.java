package com.gj.dev_note.note.query;

import java.time.Instant;
import java.util.Set;

public record NoteQuery(
        String q,                   // 텍스트 검색 (제목/내용)
        Long categoryId,            // 기준 카테고리
        boolean includeChildren,    // 하위 카테고리 포함 여부
        Set<String> tagSlugs,       // 태그 슬러그들
        TagMode tagMode,            // ANY / ALL
        VisibilityScope scope,      // PUBLIC_ONLY / MINE_OR_PUBLIC / MINE_ONLY
        Long viewerId,              // 현재 사용자 id (scope 해석용)
        Long minViews,              // 조회수 최소
        Long maxViews,              // 조회수 최대
        Instant createdFrom,        // 생성일 시작
        Instant createdTo,          // 생성일 끝(이하)
        Instant updatedFrom,        // 수정일 시작
        Instant updatedTo,          // 수정일 끝(이하)
        String sortKey,             // createdAt, updatedAt, viewCount, title
        String sortDir              // asc / desc
) {
    public enum TagMode { ANY, ALL }
    public enum VisibilityScope { PUBLIC_ONLY, MINE_OR_PUBLIC, MINE_ONLY }

    public boolean hasText() { return q != null && !q.isBlank(); }
    public boolean hasTags() { return tagSlugs != null && !tagSlugs.isEmpty(); }
}
