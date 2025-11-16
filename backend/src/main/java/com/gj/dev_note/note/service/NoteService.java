package com.gj.dev_note.note.service;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.category.service.CategoryService;
import com.gj.dev_note.common.PageEnvelope;
import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.common.error.Errors;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.note.mapper.NoteMapper;
import com.gj.dev_note.note.repository.NoteRepository;
import com.gj.dev_note.note.request.NoteCreateRequest;
import com.gj.dev_note.note.request.NotePatchRequest;
import com.gj.dev_note.note.response.NoteDetail;
import com.gj.dev_note.note.response.NoteSummary;
import com.gj.dev_note.security.CurrentUser;
import com.gj.dev_note.tag.domain.Tag;
import com.gj.dev_note.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepo;
    private final MemberRepository memberRepo;
    private final CategoryService categoryService;
    private final TagService tagService;

    public boolean existsById(Long id) {
        return noteRepo.existsById(id);
    }

    @Cacheable(
            cacheNames = "allNote",
            key = "'p'+#pageable.pageNumber+':s'+#pageable.pageSize+':'+#pageable.sort.toString()",
            sync = true
    )
    public PageEnvelope<NoteSummary> getList(Pageable pageable) {
        var page = noteRepo.findAll(pageable)
                .map(NoteMapper::toSummary);

        return PageEnvelope.of(page);
    }

    @Cacheable(
            cacheNames = "noteById",
            key = "'note:'+#id"
    )
    public NoteDetail getNote(Long id) {
        Note note = noteRepo.findById(id).orElseThrow(() -> Errors.notFound("존재하지 않는 note ", id));
        if (cannotView(CurrentUser.idOpt().orElse(null), note)) {
            throw Errors.forbidden("해당 노트를 읽을 권한이 없습니다.");
        }

        return NoteMapper.toDetail(note);
    }

    public NoteSummary getNoteSummary(Long id) {
        Note note = noteRepo.findById(id).orElseThrow(() -> Errors.notFound("note", id));
        if (cannotView(CurrentUser.idOpt().orElse(null), note)) {
            throw Errors.forbidden("해당 노트를 읽을 권한이 없습니다.");
        }

        return NoteMapper.toSummary(note);
    }

    @CacheEvict(cacheNames = {"allNote"}, allEntries = true)
    @Transactional
    public NoteDetail createNote(Long ownerId, NoteCreateRequest req) {
        var owner = memberRepo.findById(ownerId)
                .orElseThrow(() -> Errors.notFound("존재하지 않는 사용자 ", ownerId));

        var category = categoryService.resolveForAssign(ownerId, req.categoryId());

        var tags = tagService.resolveBySlugs(req.tagSlugs());

        Note newNote = Note.builder()
                .owner(owner)
                .title(req.title())
                .content(req.content())
                .visibility(req.visibility().toType())
                .build();

        newNote.moveCategory(category);
        newNote.replaceTags(tags);

        Note saved = noteRepo.save(newNote);

        log.debug("생성된 note : id={}, title={}", saved.getId(), saved.getTitle());

        return NoteMapper.toDetail(saved);
    }

    @Transactional
    public NoteDetail patchNote(Long actorId, Long noteId, NotePatchRequest patch, String ifMatch) {
        Note note = noteRepo.findById(noteId).orElseThrow(() -> Errors.notFound("존재하지 않는 노트 ", noteId));
        ensureCanEdit(actorId, note);
        ensureVersion(ifMatch, note.getVersion());

        if (patch.hasTitle()) note.renameTitle(patch.title());
        if (patch.hasContent()) note.rewriteContent(patch.content());
        if (patch.hasVisibility()) note.changeVisibility(patch.visibility().toType());
        if (patch.hasCategoryId()) {
            Category c = categoryService.resolveForAssign(actorId, patch.categoryId());
            note.moveCategory(c);
        }

        return NoteMapper.toDetail(note);
    }

    @Transactional
    public NoteDetail replaceTags(Long actorId, Long noteId, List<String> slugList, String ifMatch) {
        Note note = noteRepo.findById(noteId).orElseThrow(() -> Errors.notFound("존재하지 않는 노트 ",noteId));
        ensureCanEdit(actorId, note);
        ensureVersion(ifMatch, note.getVersion());

        Set<String> normalized = normalizeSlugs(slugList);
        Set<Tag> tags = tagService.resolveBySlugs(normalized);
        note.replaceTags(tags);

        return NoteMapper.toDetail(note);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = {"allNote"}, allEntries = true),
            @CacheEvict(cacheNames = {"noteById"}, key = "#id")
    })
    @Transactional
    public void deleteNote(Long id) {
        Note note = noteRepo.findById(id).orElseThrow(() -> Errors.notFound("존재하지 않는 노트 ", id));
        ensureCanEdit(CurrentUser.id(), note);
        noteRepo.delete(note);
    }


    private boolean canView(Long viewerId, Note n) {
        if (n.getVisibility() == Visibility.PUBLIC) return true;
        return viewerId != null && Objects.equals(n.getOwner().getId(), viewerId);
    }

    private boolean cannotView(Long viewerId, Note n) {
        return !canView(viewerId, n);
    }

    private void ensureCanEdit(Long actorId, Note n) {
        if (actorId == null || !Objects.equals(n.getOwner().getId(), actorId)) {
            throw Errors.forbidden("해당 노트를 수정할 권한이 없습니다.");
        }
    }

    private void ensureVersion(String ifMatch, Long currentVersion) {
        if (currentVersion == null) return; // 버전 미사용이면 패스
        if (ifMatch == null || ifMatch.isBlank()) return; // 선택: 없으면 허용/거부 정책 선택
        long expected = parseEtag(ifMatch);
        if (!Objects.equals(expected, currentVersion)) {
            throw new ConcurrencyFailureException("version conflict");
        }
    }

    public String etagOf(Long noteId) {
        Long v = noteRepo.findVersionById(noteId).orElse(null);
        return (v == null) ? null : "\"v%d\"".formatted(v);
    }

    private long parseEtag(String ifMatch) {
        String s = ifMatch.replace("W/", "").replace("\"", "");
        if (!s.startsWith("v")) throw Errors.badRequest("bad ETag");
        return Long.parseLong(s.substring(1));
    }

    private Set<String> normalizeSlugs(List<String> in) {
        if (in == null) return Set.of();
        return in.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
