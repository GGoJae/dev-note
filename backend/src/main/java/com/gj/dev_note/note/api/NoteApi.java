package com.gj.dev_note.note.api;

import com.gj.dev_note.note.request.NoteCreateRequest;
import com.gj.dev_note.note.request.NoteUpdateRequest;
import com.gj.dev_note.note.response.NoteResponse;
import com.gj.dev_note.note.service.NoteService;
import com.gj.dev_note.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/notes")
@RestController
@RequiredArgsConstructor
public class NoteApi {
    private final NoteService service;

    @GetMapping
    public Page<NoteResponse> noteList(Pageable pageable) {
        return service.getList(pageable);
    }

    @GetMapping("/{id}")
    public NoteResponse getNote(@PathVariable Long id) {
        return service.getOne(id);
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteCreateRequest req) {
        var saved = service.createNote(CurrentUser.id(), req);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public NoteResponse updateNote(@PathVariable Long id, NoteUpdateRequest noteUpdateRequest) {
        return service.updateNote(id, noteUpdateRequest);
    }

    @DeleteMapping("/{id}")
    public boolean deleteNote(@PathVariable Long id) {
        service.deleteNote(id);
        return !service.existsById(id);
    }
}
