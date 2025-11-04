package com.gj.dev_note.note.api;

import com.gj.dev_note.note.common.PageEnvelope;
import com.gj.dev_note.note.query.NoteQuery;
import com.gj.dev_note.note.request.NoteSearchRequest;
import com.gj.dev_note.note.response.NoteResponse;
import com.gj.dev_note.note.service.NoteReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteSearchApi {

    private final NoteReadService service;

    public PageEnvelope<NoteResponse> search(
            @ModelAttribute NoteSearchRequest req,
            Pageable pageable,
            @AuthenticationPrincipal(expression="id") Long viewerId
    ) {
        return service.search(req.toQuery(viewerId), pageable);
    }



}
