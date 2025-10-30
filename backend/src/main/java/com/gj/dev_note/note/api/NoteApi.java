package com.gj.dev_note.note.api;

import com.gj.dev_note.note.request.CreateNote;
import com.gj.dev_note.note.request.UpdateNote;
import com.gj.dev_note.note.response.NoteResponse;
import com.gj.dev_note.note.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public NoteResponse createNote(CreateNote createNote) {
        return service.createNote(createNote);
    }

    @PutMapping("/{id}")
    public NoteResponse updateNote(@PathVariable Long id, UpdateNote updateNote) {
        return service.updateNote(id, updateNote);
    }

    @DeleteMapping("/{id}")
    public boolean deleteNote(@PathVariable Long id) {
        service.deleteNote(id);
        return !service.existsById(id);
    }
}
