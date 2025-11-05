package com.gj.dev_note.artifact.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ArtifactVariantCreateRequest(
        @NotBlank
        String name,        //  ex) (refactoring 에서) before, after (best vs bad 에서) best, bad

        @Min(0)
        int displayOrder,

        @NotNull
        List<@NotNull ArtifactCreateRequest> artifacts
) {
}
