package com.gj.dev_note.artifact.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ArtifactCreateRequest(
        @NotBlank
        String kind,            // 코드, 정리, 설명 등

        String content,

        String contentUrl,

        String mimeType,

        @Min(0)
        int displayOrder
) {
    public ArtifactCreateRequest{
        if (content.isBlank() && contentUrl.isBlank()) {
            throw new IllegalArgumentException("content 혹은 contentUrl 작성은 필수입니다.");
        }
    }
}
