package com.gj.dev_note.practice.request;

import jakarta.validation.constraints.NotBlank;

public record FinalizeRequest(
        @NotBlank
        String token
) {
}
