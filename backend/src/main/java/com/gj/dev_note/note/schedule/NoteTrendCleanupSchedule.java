package com.gj.dev_note.note.schedule;

import com.gj.dev_note.note.trend.TrendKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteTrendCleanupSchedule {

    private final StringRedisTemplate redis;

    // 매 시간 05 분 마다: 시간 ZSET 들 중 48시간 이전 키 정리
    @Scheduled(cron = "0 5 * * * *")
    public void cleanupHourlyZSets() {
        Instant cutoff = Instant.now().minus(48, ChronoUnit.HOURS);
        cleanIndex(TrendKeys.RANK_H_INDEX, cutoff, TIME_UNIT.RANK_HOURS);
    }

    @Scheduled(cron = "0 30 2 * * *")
    public void cleanupDailyZSets() {
        Instant cutoff = Instant.now().minus(14, ChronoUnit.DAYS);
        cleanIndex(TrendKeys.RANK_D_INDEX, cutoff, TIME_UNIT.RANK_DAYS);
    }

    private void cleanIndex(String indexKey, Instant cutoff, TIME_UNIT timeUnit) {
        Set<String> members = redis.opsForSet().members(indexKey);
        if (members == null || members.isEmpty()) return;

        for (String key : members) {
            try {
                Instant when = switch (timeUnit) {
                    case RANK_HOURS -> TrendKeys.parseHourKey(key);
                    case RANK_DAYS -> TrendKeys.parseDayKey(key);
                };

                if (when.isBefore(cutoff)) {
                    Boolean deleted = redis.delete(key);
                    redis.opsForSet().remove(indexKey, key);
                    log.debug("레디스 정리 {} -> deleted : {}, cutoff: {}", key, deleted, cutoff);
                }
            } catch (Exception e) {
                log.warn("레디스 정리 skip (인덱스 키 : {})", key);
            }
        }

    }

    private enum TIME_UNIT {
        RANK_HOURS, RANK_DAYS
    }
}
