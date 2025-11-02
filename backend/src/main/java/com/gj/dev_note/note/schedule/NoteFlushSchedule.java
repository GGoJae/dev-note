package com.gj.dev_note.note.schedule;

import com.gj.dev_note.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteFlushSchedule {

    private final StringRedisTemplate redis;
    private final NoteRepository repo;

    private static final String VIEW_KEY_PREFIX = "note:view:";

    @Scheduled(fixedDelay = 30_000L)
    @Transactional
    public void flushToDatabase() {
        log.debug("flashToDB 진입 완료");
        ScanOptions scan = KeyScanOptions.scanOptions(DataType.STRING)
                .match(VIEW_KEY_PREFIX + "*")
                .count(1000)
                .build();

        try (Cursor<byte[]> cur = redis.execute(conn -> conn.scan(scan), true)) {
            if (cur == null) return;
            while (cur.hasNext()) {
                String key = new String(cur.next(), StandardCharsets.UTF_8);
                String idStr = key.substring(VIEW_KEY_PREFIX.length());
                long noteId = Long.parseLong(idStr);
                log.debug("noteId 확인 :{}, {},  {}", key, idStr, noteId);

                String delStr = redis.opsForValue().getAndDelete(key);
                long delta = delStr != null ? Long.parseLong(delStr) : 0;
                if (delta > 0) {
                    int count = repo.increaseViewCount(noteId, delta);
                    log.debug("count 확인!! {}",count);
                }
            }
        } catch (Exception e) {
            log.warn("레디스 커넥션 확인 중, 에러", e);
        }
    }
}
