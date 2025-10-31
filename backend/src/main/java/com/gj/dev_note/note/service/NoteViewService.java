package com.gj.dev_note.note.service;

import com.gj.dev_note.note.repository.NoteRepo;
import com.gj.dev_note.note.trend.TrendKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteViewService {

    private final StringRedisTemplate redis;
    private final NoteRepo repo;

    private static final String VIEW_KEY_PREFIX = "note:view:";
    private static final int HOURS_BUCKET_TTL_HOURS = 26;
    private static final int DAYS_BUCKET_TTL_DAYS = 8;

    public void increaseView(Long noteId) {
        var viewKey = TrendKeys.viewKey(noteId);
        redis.opsForValue().increment(viewKey);

        var now = Instant.now();
        var member = Long.toString(noteId);
        var hourKey = TrendKeys.hourKey(now);
        var dayKey = TrendKeys.dayKey(now);

        ZSetOperations<String, String> z = redis.opsForZSet();
        z.incrementScore(hourKey, member, 1.0);
        z.incrementScore(dayKey, member, 1.0);

        Long hTtl = redis.getExpire(hourKey);
        if (hTtl == -1) {
            redis.expire(hourKey, Duration.ofHours(HOURS_BUCKET_TTL_HOURS));
        }
        Long dTtl = redis.getExpire(dayKey);
        if (dTtl == -1) {
            redis.expire(dayKey, Duration.ofDays(DAYS_BUCKET_TTL_DAYS));
        }
    }

    public long currentTotalViews(Long noteId) {
        long base = repo.findById(noteId).orElseThrow().getViewCount();
        String delStr = redis.opsForValue().get(TrendKeys.viewKey(noteId));
        long delta = delStr != null ? Long.parseLong(delStr) : 0;
        return base + delta;
    }
}
