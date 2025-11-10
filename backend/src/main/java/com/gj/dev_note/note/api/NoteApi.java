package com.gj.dev_note.note.api;

import com.gj.dev_note.note.request.NoteCreateRequest;
import com.gj.dev_note.note.request.NotePatchRequest;
import com.gj.dev_note.note.response.NoteDetail;
import com.gj.dev_note.note.response.NoteSummary;
import com.gj.dev_note.note.service.NoteService;
import com.gj.dev_note.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/notes")
@RestController
@RequiredArgsConstructor
public class NoteApi {
    private final NoteService service;

    @GetMapping
    public Page<NoteSummary> noteList(Pageable pageable) {
        return service.getList(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDetail> getNote(@PathVariable Long id) {
        var note = service.getNote(id);

        return ResponseEntity.ok()
                .eTag(service.etagOf(note.id()))
                .body(note);
    }

    @PostMapping
    public ResponseEntity<NoteDetail> createNote(@Valid @RequestBody NoteCreateRequest req) {
        var saved = service.createNote(CurrentUser.id(), req);
        return ResponseEntity.ok(saved);
    }


    @PreAuthorize("@noteAuthz.isOwner(#id")
    @PatchMapping(value = "/{id}", consumes = {"application/merge-patch+json", "application/json"})
    public ResponseEntity<NoteDetail> patchNote(
            @PathVariable Long id,
            @RequestBody NotePatchRequest patch,
            @RequestHeader(value = "If-Match", required = false) String ifMatch
    ) {
        var result = service.patchNote(CurrentUser.id(), id, patch, ifMatch);

        return ResponseEntity.ok()
                .eTag(service.etagOf(result.id()))
                .body(result);
    }

    @PreAuthorize("@noteAuthz.isOwner(#id)")
    @PutMapping("/{id}/tags")
    public ResponseEntity<NoteDetail> replaceTag(
            @PathVariable Long id,
            @RequestBody List<String> slugs,
            @RequestHeader(value = "If-Match", required = false) String ifMatch
    ) {
        var result = service.replaceTags(CurrentUser.id(), id, slugs, ifMatch);

        return ResponseEntity.ok()
                .eTag(service.etagOf(result.id()))
                .body(result);
    }

    @PreAuthorize("@noteAuthz.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        service.deleteNote(id);
        return ResponseEntity.noContent().build();
    }


}
