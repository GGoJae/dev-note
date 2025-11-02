package com.gj.dev_note.note.schedule;

import com.gj.dev_note.note.trend.TrendKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteTrendAggregatorSchedule {

    private final StringRedisTemplate redis;

    private static final Duration TMP_TTL = Duration.ofMinutes(2);

    @Scheduled(fixedDelay = 60_000L, initialDelay = 5_000)
    public void refreshALl() {
        Instant now = Instant.now();
        refresh24h(now);
        refresh7d(now);
    }

    void refresh24h(Instant now) {
        List<String> hourKeys = TrendKeys.lastNHourKeys(now, 24);
        String tmp = "rank:tmp:24h:" + now.toEpochMilli();

        unionInto(tmp, hourKeys);
        redis.expire(tmp, TMP_TTL);

        Long card = redis.opsForZSet().zCard(tmp);
        if (card != null && card > 0) {
            atomicSwap(tmp, TrendKeys.TREND_24H);
        } else {
            // 비어있으면 기존 TREND_24H 그대로 유지
        }
    }

    void refresh7d(Instant now) {
        List<String> daysKeys = TrendKeys.lastNDaysKeys(now, 7);
        String tmp = "rank:tmp:7d:" + now.toEpochMilli();

        unionInto(tmp, daysKeys);
        redis.expire(tmp, TMP_TTL);

        Long card = redis.opsForZSet().zCard(tmp);
        if (card != null && card > 0) {
            atomicSwap(tmp, TrendKeys.TREND_7D);
        } else {
            // 비어있으면 기존 TREND_24H 그대로 유지
        }
    }

    private void unionInto(String dest, List<String> keys) {
        if (keys.isEmpty()) return;
        var z = redis.opsForZSet();
        if (keys.size() == 1) {
            z.unionAndStore(keys.get(0), Collections.emptyList(), dest);
        } else {
            z.unionAndStore(keys.get(0), keys.subList(1, keys.size()), dest);
        }
    }

    private void atomicSwap(String src, String dest) {
        if (Boolean.TRUE.equals(redis.hasKey(src))) {
            redis.rename(src, dest);
        }
    }

}
