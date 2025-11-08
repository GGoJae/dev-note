package com.gj.dev_note.note.api;

import com.gj.dev_note.note.query.NoteQuery;
import com.gj.dev_note.note.request.NoteSearchRequest;
import com.gj.dev_note.note.response.NoteResponse;
import com.gj.dev_note.note.service.NoteReadServiceQdsl;
import com.gj.dev_note.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NoteSearchApi {

    private final NoteReadServiceQdsl service;

    @GetMapping("/notes/public")
    public Page<NoteResponse> searchPublic(@Valid NoteSearchRequest req, Pageable pageable) {
        return service.search(NoteQuery.publicOnly(req), pageable);
    }

    @GetMapping("/me/notes")
    public Page<NoteResponse> searchMine(@Valid NoteSearchRequest req, Pageable pageable) {
        return service.search(NoteQuery.mineOnly(req, CurrentUser.id()), pageable);
    }

    @GetMapping("/notes")
    public Page<NoteResponse> searchMixed(@Valid NoteSearchRequest req, Pageable pageable) {
        Long viewerId = CurrentUser.idOpt().orElse(null);
        return service.search(NoteQuery.mineOrPublic(req, viewerId), pageable);
    }

    @GetMapping("/users/{ownerId}/notes")
    public Page<NoteResponse> searchOwnerPublic(@PathVariable long ownerId,
                                               @Valid NoteSearchRequest req,
                                                Pageable pageable) {
        return service.search(NoteQuery.ownerPublic(req, ownerId), pageable);
    }

    @GetMapping("/users/{ownerId}/notes/access")
    public Page<NoteResponse> searchOwnerAccessible(@PathVariable long ownerId,
                                                   @Valid NoteSearchRequest req,
                                                    Pageable pageable) {
        Long viewerId = CurrentUser.idOpt().orElse(null);
        return service.search(NoteQuery.ownerAccessible(req, ownerId, viewerId), pageable);
    }



}
