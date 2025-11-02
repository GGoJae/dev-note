package com.gj.dev_note.note.service;

import com.gj.dev_note.note.trend.TrendKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NoteTrendWriteService {

    private final StringRedisTemplate redis;

    private static final Duration HOUR_ZSET_TTL = Duration.ofHours(36);
    private static final Duration DAY_ZSET_TTL = Duration.ofDays(14);

    public void recordView(long noteId, Instant now) {
        String member = String.valueOf(noteId);
        String hKey = TrendKeys.hourKey(now);
        String dKey = TrendKeys.dayKey(now);

        redis.opsForZSet().incrementScore(hKey, member, 1.0);
        redis.opsForZSet().incrementScore(dKey, member, 1.0);

        redis.opsForSet().add(TrendKeys.RANK_H_INDEX, hKey);
        redis.opsForSet().add(TrendKeys.RANK_D_INDEX, dKey);

        redis.expire(hKey, HOUR_ZSET_TTL);
        redis.expire(dKey, DAY_ZSET_TTL);
    }
}
