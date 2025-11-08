package com.gj.dev_note.note.service;

import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.common.PageEnvelope;
import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.note.mapper.NoteMapper;
import com.gj.dev_note.note.repository.NoteRepository;
import com.gj.dev_note.note.request.NoteCreateRequest;
import com.gj.dev_note.note.request.NoteUpdateRequest;
import com.gj.dev_note.note.response.NoteDetail;
import com.gj.dev_note.note.response.NoteSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository repo;
    private final CacheManager cacheManager;
    private final MemberRepository memberRepo;

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    @Cacheable(
            cacheNames = "allNote",
            key = "'p'+#pageable.pageNumber+':s'+#pageable.pageSize+':'+#pageable.sort.toString()",
            sync = true
    )
    public PageEnvelope<NoteSummary> getList(Pageable pageable) {
        var page = repo.findAll(pageable)
                .map(NoteMapper::toSummary);

        return PageEnvelope.of(page);
    }

    @Cacheable(
            cacheNames = "noteById",
            key = "'note:'+#id"
    )
    public NoteDetail getNote(Long id) {
        // 캐시 매니저를 사용할 때 수동 버전
//        Cache cache = cacheManager.getCache("noteById");
//        if (cache != null) {
//            NoteResponse cacheHit = cache.get(id, NoteResponse.class);
//            if (cacheHit != null) {
//                return cacheHit;
//            }
//        }

        Note note = repo.findById(id).orElseThrow();

//        if (cache != null) {
//            NoteResponse noteCache = NoteMapper.toResponse(note);
//            log.debug("캐시에 노트 저장, caching 필드를 true 인 상태로 저장 , {}", noteCache);
//            cache.put(id, noteCache);
//        }

        return NoteMapper.toDetail(note);
    }

    @CacheEvict(cacheNames = {"allNote"}, allEntries = true)
    @Transactional
    public NoteDetail createNote(Long ownerId, NoteCreateRequest createNote) {
        var ownerRef = memberRepo.getReferenceById(ownerId);
        Note newNote = Note.builder()
                .title(createNote.title())
                .content(createNote.content())
                .owner(ownerRef)
                .build();
        Note saved = repo.save(newNote);
        log.debug("저장된 노트 : {} ", saved);
//        Cache cache = cacheManager.getCache("noteById");
//        if (cache != null) {
//            log.debug("캐시에 저장");
//            cache.put(saved.getId(), NoteMapper.toResponse(saved));
//        }

        return NoteMapper.toDetail(saved);
    }

    @CacheEvict(cacheNames = {"allNote"}, allEntries = true)
    @Transactional
    public NoteDetail updateNote(Long id, NoteUpdateRequest noteUpdateRequest) {
        Note note = repo.findById(id).orElseThrow();
        log.debug("update 전 note: {}",note);
        note.setTitle(noteUpdateRequest.title());
        note.setContent(noteUpdateRequest.content());
        log.debug("update 전 note: {}",note);

        Cache cache = cacheManager.getCache("noteById");
        if (cache != null) {
            log.debug("캐시에 저장");
            cache.put(id, NoteMapper.toDetail(note));
        }
        return NoteMapper.toDetail(note);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = {"allNote"}, allEntries = true),
            @CacheEvict(cacheNames = {"noteById"}, key = "#id")
    })
    @Transactional
    public void deleteNote(Long id) {
        log.debug("delete Note 진입");
        repo.deleteById(id);

        log.debug("delete Note 완료");
    }
}
