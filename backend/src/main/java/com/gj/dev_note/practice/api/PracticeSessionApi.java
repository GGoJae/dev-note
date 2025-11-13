package com.gj.dev_note.practice.api;

import com.gj.dev_note.practice.dto.*;
import com.gj.dev_note.practice.request.AnswerSubmitRequest;
import com.gj.dev_note.practice.request.SessionCreateRequest;
import com.gj.dev_note.practice.response.AnswerResultResponse;
import com.gj.dev_note.practice.response.FinalizeResponse;
import com.gj.dev_note.practice.response.SessionCreatedResponse;
import com.gj.dev_note.practice.service.PracticeSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practice-sessions")
@RequiredArgsConstructor
public class PracticeSessionApi {

    private final PracticeSessionService service;

    @PostMapping
    public SessionCreatedResponse create(@Valid @RequestBody SessionCreateRequest req) {
        return service.create(req);
    }

    @GetMapping("/{sessionId}/items")
    public SessionItemsPage page(@PathVariable Long sessionId,
                                 @RequestParam(required = false) String cursor) {
        return service.page(sessionId, cursor);
    }

    @PostMapping("/{sessionId}/answers")
    public AnswerResultResponse submit(@PathVariable Long sessionId,
                                       @Valid @RequestBody AnswerSubmitRequest req) {
        return service.submit(sessionId, req);
    }

    @PostMapping("/{sessionId}/finalize")
    public FinalizeResponse finalize(@PathVariable Long sessionId) {
        return service.finalize(sessionId);
    }
}
