package com.gj.dev_note.note.service;

import com.gj.dev_note.note.common.PageEnvelope;
import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.note.mapper.NoteMapper;
import com.gj.dev_note.note.query.NoteQuery;
import com.gj.dev_note.note.query.NoteSorts;
import com.gj.dev_note.note.query.NoteSpecs;
import com.gj.dev_note.note.repository.NoteRepository;
import com.gj.dev_note.note.response.NoteResponse;
import com.gj.dev_note.category.service.CategoryTreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
public class NoteReadService {

    private final NoteRepository noteRepo;
    private final CategoryTreeService categoryTreeService;

    public PageEnvelope<NoteResponse> search(NoteQuery q, Pageable pageable) {

        // 카테고리 범위 결정
        Specification<Note> spec = where(null);

        if (q.hasText()) {
            spec = spec.and(NoteSpecs.textLike(q.q()));
        }

        if (q.categoryId() != null) {
            if (q.includeChildren()) {
                Set<Long> ids = categoryTreeService.resolveSubtreeIds(q.categoryId());
                spec = spec.and(NoteSpecs.inCategoryIds(ids.isEmpty() ? Set.of(-1L) : ids));
            } else {
                spec = spec.and(NoteSpecs.inCategoryId(q.categoryId()));
            }
        }

        if (q.hasTags()) {
            spec = spec.and(
                    q.tagMode() == NoteQuery.TagMode.ALL
                            ? NoteSpecs.hasAllTags(q.tagSlugs())
                            : NoteSpecs.hasAnyTag(q.tagSlugs())
            );
        }

        // 가시성
        spec = switch (q.scope()) {
            case MINE_ONLY     -> spec.and(NoteSpecs.visibilityMineOnly(q.viewerId()));
            case MINE_OR_PUBLIC -> spec.and(NoteSpecs.visibilityMineOrPublic(q.viewerId()));
            default -> spec.and(NoteSpecs.visibilityPublicOnly());
        };

        // 뷰/기간
        spec = spec.and(NoteSpecs.viewsGte(q.minViews()))
                .and(NoteSpecs.viewsLte(q.maxViews()))
                .and(NoteSpecs.createdBetween(q.createdFrom(), q.createdTo()))
                .and(NoteSpecs.updatedBetween(q.updatedFrom(), q.updatedTo()));

        // 정렬 합성
        Sort s = NoteSorts.toSort(q.sortKey(), q.sortDir());
        Pageable pageReq = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), s);

        Page<Note> page = noteRepo.findAll(spec, pageReq);

        return new PageEnvelope<>(
                page.map(NoteMapper::repoToResponse).getContent(),
                page.getPageable(),
                page.getTotalElements()
        );
    }
}
