package com.gj.dev_note.note.query;

import com.gj.dev_note.note.request.NoteSearchRequest;
import lombok.*;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Getter
@Builder(builderMethodName = "builderInternal", access = AccessLevel.PRIVATE)
public class NoteQuery {
    private final String q;
    private final Long categoryId;
    private final boolean includeChildren;
    private final Set<String> tag;
    private final TagMode tagMode;
    private final Long viewerId;   // 요청자
    private final Long ownerId;    // 타겟 소유자
    private final Long minViews;
    private final Long maxViews;
    private final Instant createdFrom;
    private final Instant createdTo;
    private final Instant updatedFrom;
    private final Instant updatedTo;
    private final String sortKey;
    private final String sortDir;

    private final VisibilityScope scope;

    public enum TagMode { ANY, ALL }
    public enum VisibilityScope { PUBLIC_ONLY, MINE_ONLY, MINE_OR_PUBLIC, OWNER_PUBLIC, OWNER_ACCESSIBLE }

    public boolean hasText() {
        return q != null && !q.isBlank();
    }

    public boolean hasTags() {
        return tag != null && !tag.isEmpty();
    }

    private static NoteQueryBuilder base(NoteSearchRequest req, Long viewerId, Long ownerId) {
        return builderInternal()
                .q(req.getQ())
                .categoryId(req.getCategoryId())
                .includeChildren(req.isIncludeChildren())
                .tag(req.getTag() == null ? null : Set.copyOf(req.getTag()))
                .tagMode(req.getTagMode())
                .viewerId(viewerId)
                .ownerId(ownerId)
                .minViews(req.getMinViews()).maxViews(req.getMaxViews())
                .createdFrom(req.getCreatedFrom()).createdTo(req.getCreatedTo())
                .updatedFrom(req.getUpdatedFrom()).updatedTo(req.getUpdatedTo())
                .sortKey(req.getSortKey()).sortDir(req.getSortDir());
    }
    private static NoteQuery buildChecked(NoteQueryBuilder b) {
        NoteQuery q = b.build();
        Objects.requireNonNull(q.scope, "scope is required");
        return q;
    }

    public static NoteQuery publicOnly(NoteSearchRequest req) {
        return buildChecked(base(req, null, null).scope(VisibilityScope.PUBLIC_ONLY));
    }
    public static NoteQuery mineOnly(NoteSearchRequest req, long viewerId) {
        return buildChecked(base(req, viewerId, viewerId).scope(VisibilityScope.MINE_ONLY));
    }
    public static NoteQuery mineOrPublic(NoteSearchRequest req, Long viewerId) {
        var scope = (viewerId == null) ? VisibilityScope.PUBLIC_ONLY : VisibilityScope.MINE_OR_PUBLIC;
        return buildChecked(base(req, viewerId, null).scope(scope));
    }
    public static NoteQuery ownerPublic(NoteSearchRequest req, long ownerId) {
        return buildChecked(base(req, null, ownerId).scope(VisibilityScope.OWNER_PUBLIC));
    }
    public static NoteQuery ownerAccessible(NoteSearchRequest req, long ownerId, Long viewerId) {
        return buildChecked(base(req, viewerId, ownerId).scope(VisibilityScope.OWNER_ACCESSIBLE));
    }
}
