package com.gj.dev_note.note.api;

import com.gj.dev_note.note.service.NoteViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteViewApi {

    private final NoteViewService viewService;

    @PostMapping("/{id}/view")
    public ViewResponse view(@PathVariable Long id) {
        viewService.increaseView(id);
        long totalViews = viewService.currentTotalViews(id);
        return new ViewResponse(id, totalViews);
    }

    @GetMapping("/{id}/view")
    public ViewResponse getTotalViews(@PathVariable Long id) {
        long totalViews = viewService.currentTotalViews(id);
        return new ViewResponse(id, totalViews);
    }

    public record ViewResponse(
            Long id, Long totalViews
    ) {

    }
}

