package com.gj.dev_note.note.service;

import com.gj.dev_note.note.response.TrendingNoteResponse;
import com.gj.dev_note.note.trend.TrendKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteTrendService {

    private final StringRedisTemplate redis;
    private final NoteService noteService;

    public record TrendingItem(Long id, double score) {}

    public List<TrendingNoteResponse> top24hWithNotes(int limit) {
        return attachNotes(readTop(TrendKeys.TREND_24H, limit));
    }

    public List<TrendingNoteResponse> top7dWithNotes(int limit) {
        return attachNotes(readTop(TrendKeys.TREND_7D, limit));
    }


    private List<TrendingItem> readTop(String key, int limit) {
        var tuples = redis.opsForZSet().reverseRangeWithScores(key, 0, Math.max(0, limit - 1));
        if (tuples == null || tuples.isEmpty()) return List.of();

        var out = new ArrayList<TrendingItem>(tuples.size());
        for (var t : tuples) {
            if (t.getValue() == null || t.getScore() == null) continue;
            out.add(new TrendingItem(Long.valueOf(t.getValue()), t.getScore()));
        }
        return out;
    }

    private List<TrendingNoteResponse> attachNotes(List<TrendingItem> items) {
        var out = new ArrayList<TrendingNoteResponse>(items.size());
        for (var it : items) {
            var note = noteService.getOne(it.id());
            out.add(new TrendingNoteResponse(note, it.score));
        }
        return out;
    }
}
