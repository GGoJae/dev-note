package com.gj.dev_note.note.request;

import com.gj.dev_note.note.query.NoteQuery;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class NoteSearchRequest {
    private String q;
    private Long categoryId;
    private boolean includeChildren;
    private Set<String> tag; // ?tag=a&tag=b
    private NoteQuery.TagMode tagMode = NoteQuery.TagMode.ANY;
    private NoteQuery.VisibilityScope scope = NoteQuery.VisibilityScope.MINE_OR_PUBLIC;

    private Long minViews;
    private Long maxViews;
    private Instant createdFrom;
    private Instant createdTo;
    private Instant updatedFrom;
    private Instant updatedTo;

    private String sortKey;
    private String sortDir;

    public NoteQuery toQuery(Long viewerId) {
        return new NoteQuery(
                q, categoryId, includeChildren, tag, tagMode, scope, viewerId,
                minViews, maxViews, createdFrom, createdTo, updatedFrom, updatedTo,
                sortKey, sortDir
        );
    }
}