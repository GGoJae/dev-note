package com.gj.dev_note.artifact.request;

import com.gj.dev_note.artifact.dto.ArtifactGroupTypeDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ArtifactGroupCreateRequest(
    @NotNull
    ArtifactGroupTypeDto groupType,

    @NotBlank
    String logicalKey,

    @Min(0)
    int displayOrder,

    @NotNull
    List<@NotNull ArtifactVariantCreateRequest> variants
) {}
