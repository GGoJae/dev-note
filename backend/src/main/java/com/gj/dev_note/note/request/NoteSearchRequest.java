package com.gj.dev_note.note.request;
import com.gj.dev_note.note.query.NoteQuery;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class NoteSearchRequest {
    private String q;
    private Long categoryId;
    private boolean includeChildren;
    private Set<String> tag;
    private NoteQuery.TagMode tagMode = NoteQuery.TagMode.ANY;

    @Min(0) private Long minViews;
    @Min(0) private Long maxViews;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private Instant createdFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private Instant createdTo;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private Instant updatedFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private Instant updatedTo;

    private String sortKey = "createdAt";
    private String sortDir = "desc";

    public void setTag(Set<String> tag) {
        if (tag == null) { this.tag = null; return; }
        this.tag = tag.stream()
                .flatMap(s -> Arrays.stream(s.split("[,\\s]+")))
                .map(String::trim).filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
