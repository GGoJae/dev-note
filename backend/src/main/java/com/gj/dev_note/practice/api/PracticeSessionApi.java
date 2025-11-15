package com.gj.dev_note.practice.api;

import com.gj.dev_note.practice.request.AnswerSubmitRequest;
import com.gj.dev_note.practice.request.FinalizeRequest;
import com.gj.dev_note.practice.request.SessionCreateRequest;
import com.gj.dev_note.practice.response.*;
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

    @GetMapping("/{sessionId}/resume")
    public ResumeResponse resume(@PathVariable Long sessionId) {
        return service.resume(sessionId);
    }

    @GetMapping("/{sessionId}/window")
    public WindowPageResponse window(@PathVariable Long sessionId,
                                     @RequestParam(required = false) String cursor) {
        return service.window(sessionId, cursor);
    }

    @PostMapping("/{sessionId}/answers")
    public AnswerResultResponse submit(@PathVariable Long sessionId,
                                       @Valid @RequestBody AnswerSubmitRequest req) {
        return service.submit(sessionId, req);
    }

    @PostMapping("/{sessionId}/pass/{sessionItemId}")
    public WindowPageResponse pass(@PathVariable Long sessionId,
                                   @PathVariable Long sessionItemId) {
        return service.pass(sessionId, sessionItemId);
    }

    @PostMapping("/{sessionId}/finalize")
    public FinalizeResponse finalize(@PathVariable Long sessionId,
                                     @Valid @RequestBody FinalizeRequest req) {
        return service.finalizeSession(sessionId, req);
    }
}
