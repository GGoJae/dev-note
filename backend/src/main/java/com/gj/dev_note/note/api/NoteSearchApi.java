package com.gj.dev_note.note.api;

import com.gj.dev_note.note.common.PageEnvelope;
import com.gj.dev_note.note.query.NoteQuery;
import com.gj.dev_note.note.response.NoteResponse;
import com.gj.dev_note.note.service.NoteReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteSearchApi {

    private final NoteReadService service;

    public PageEnvelope<NoteResponse> search(
            @RequestParam NoteQuery query,
            Pageable pageable,
            @RequestHeader(name = "X-Viewer-Id", required = false) Long viewerId
    ) {
        return service.search(query, pageable);
    }



}
