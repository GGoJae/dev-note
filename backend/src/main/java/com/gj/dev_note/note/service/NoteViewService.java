package com.gj.dev_note.note.service;

import com.gj.dev_note.note.repository.NoteRepository;
import com.gj.dev_note.note.trend.TrendKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteViewService {

    private final StringRedisTemplate redis;
    private final NoteRepository repo;
    private final NoteTrendWriteService trendWriter;

    private static final int HOURS_BUCKET_TTL_HOURS = 26;
    private static final int DAYS_BUCKET_TTL_DAYS = 8;

    public void increaseView(Long noteId) {
        redis.opsForValue().increment(TrendKeys.viewKey(noteId));
        trendWriter.recordView(noteId, Instant.now());
    }

    public long currentTotalViews(Long noteId) {
        long base = repo.findById(noteId).orElseThrow().getViewCount();
        String delStr = redis.opsForValue().get(TrendKeys.viewKey(noteId));
        long delta = delStr != null ? Long.parseLong(delStr) : 0;
        return base + delta;
    }
}
