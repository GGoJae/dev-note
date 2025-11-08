package com.gj.dev_note.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@JsonIgnoreProperties(
        ignoreUnknown = true,
        value = {"pageable", "sort", "last", "first", "empty", "totalPages", "numberOfElements"})
public class PageEnvelope<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PageEnvelope(
            @JsonProperty("content") List<T> content,
            @JsonProperty("page") int page,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements
    ) {
        super(content, PageRequest.of(page, size), totalElements);
    }
    public PageEnvelope(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public PageEnvelope(Page<T> page) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    public static <T> PageEnvelope<T> of(Page<T> page) {
        return new PageEnvelope<>(page);
    }
}

