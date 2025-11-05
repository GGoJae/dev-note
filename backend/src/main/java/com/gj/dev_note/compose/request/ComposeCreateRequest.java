package com.gj.dev_note.compose.request;

import com.gj.dev_note.artifact.request.ArtifactGroupCreateRequest;
import com.gj.dev_note.note.request.NoteCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ComposeCreateRequest(
        @NotNull @Valid
        NoteCreateRequest note,

        List<@Valid ArtifactGroupCreateRequest> artifactGroups

        // TODO Quiz, Tag 등 한번에 만들 요청 넣기
) {
}
