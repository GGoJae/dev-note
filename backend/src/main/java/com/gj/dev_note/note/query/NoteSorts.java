package com.gj.dev_note.note.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoteSorts {

    public static Sort toSort(String key, String dir) {
        String k = (key == null || key.isBlank()) ? "createdAt" : key;
        Sort.Direction d = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (k) {
            case "updatedAt" -> Sort.by(d, "updatedAt", "id");
            case "viewCount" -> Sort.by(d, "viewCount", "id");
            case "title"     -> Sort.by(d, "title", "id");
            default          -> Sort.by(d, "createdAt", "id");
        };
    }
}
