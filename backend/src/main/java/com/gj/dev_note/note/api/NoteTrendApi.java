package com.gj.dev_note.note.api;

import com.gj.dev_note.note.response.TrendingNoteResponse;
import com.gj.dev_note.note.service.NoteTrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notes/trending")
@RequiredArgsConstructor
public class NoteTrendApi {

    private final NoteTrendService trendService;

    @GetMapping("/24h")
    public List<TrendingNoteResponse> top24h(@RequestParam(defaultValue = "10") int limit) {
        return trendService.top24hWithNotes(limit);
    }

    @GetMapping("/7d")
    public List<TrendingNoteResponse> top7d(@RequestParam(defaultValue = "3") int limit) {
        return trendService.top7dWithNotes(limit);
    }
}
