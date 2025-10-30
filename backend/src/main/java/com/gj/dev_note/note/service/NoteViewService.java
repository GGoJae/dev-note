package com.gj.dev_note.note.service;

import com.gj.dev_note.note.repository.NoteRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteViewService {

    private final StringRedisTemplate redis;
    private final NoteRepo repo;

    private static final String VIEW_KEY_PREFIX = "note:view:";

    private String keyOf(Long id) {
        return VIEW_KEY_PREFIX + id;
    }

    public void increaseView(Long noteId) {
        redis.opsForValue().increment(keyOf(noteId));
    }

    public long currentTotalViews(Long noteId) {
        long base = repo.findById(noteId).orElseThrow().getViewCount();
        String delStr = redis.opsForValue().get(keyOf(noteId));
        long delta = delStr != null ? Long.parseLong(delStr) : 0;
        return base + delta;
    }
}
