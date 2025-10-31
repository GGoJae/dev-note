package com.gj.dev_note.note.service;

import com.gj.dev_note.note.response.NoteResponse;
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

    private static final Duration TMP_TTL = Duration.ofSeconds(10);

    public List<TrendingItem> top24hIds(int limit) {
        var now = Instant.now();
        var hoursKeys = TrendKeys.lastNHourKeys(now, 24);

        String dest = "rank:tmp:24h:" + now.toEpochMilli();
        unionInto(dest, hoursKeys);
        redis.expire(dest, TMP_TTL);

        return readTop(dest, limit);
    }

    public List<TrendingItem> top7dIds(int limit) {
        var now = Instant.now();
        var dayKeys = TrendKeys.lastNDaysKeys(now, 7);

        String dest = "rank:tmp:7d:" + now.toEpochMilli();
        unionInto(dest, dayKeys);
        redis.expire(dest, TMP_TTL);

        return readTop(dest, limit);
    }

    public List<TrendingNoteResponse> top24hWithNotes(int limit) {
        var items = top24hIds(limit);
        return attachNotes(items);
    }

    public List<TrendingNoteResponse> top7dWithNotes(int limit) {
        var items = top7dIds(limit);
        return attachNotes(items);
    }



    private void unionInto(String dest, List<String> keys) {
        if (keys.isEmpty()) {
            return;
        }
        var z = redis.opsForZSet();
        if (keys.size() == 1) {
            z.unionAndStore(keys.get(0), Collections.emptyList(), dest);
            return;
        }
        z.unionAndStore(keys.get(0), keys.subList(1, keys.size()), dest);
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
