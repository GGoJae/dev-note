package com.gj.dev_note.note.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gj.dev_note.common.VisibilityDto;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotePatchRequest(
        @Size(max = 200) String title,
        String content,
        Long categoryId,
        VisibilityDto visibility
) {

    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }

    public boolean hasContent() {
        return content != null && !content.isBlank();
    }

    public boolean hasCategoryId() {
        return categoryId != null;
    }

    public boolean hasVisibility() {
        return visibility != null;
    }
}
