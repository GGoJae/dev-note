package com.gj.dev_note.note.service;

import com.gj.dev_note.category.service.CategoryTreeService;
import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.note.common.PageEnvelope;
import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.note.domain.QNote;
import com.gj.dev_note.note.mapper.NoteMapper;
import com.gj.dev_note.note.query.NoteQuery;
import com.gj.dev_note.note.query.NoteSorts;
import com.gj.dev_note.note.response.NoteResponse;
import com.gj.dev_note.tag.domain.QTag;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NoteReadServiceQdsl {

    private final JPAQueryFactory qf;
    private final CategoryTreeService categoryTreeService;

    public PageEnvelope<NoteResponse> search(NoteQuery q, Pageable pageable) {
        QNote note = QNote.note;
        QTag tag = QTag.tag;

        BooleanBuilder where = new BooleanBuilder();

        // 텍스트
        if (q.hasText()) {
            String pat = "%" + q.q().toLowerCase() + "%";
            where.and(
                    note.title.lower().like(pat)
                            .or(note.content.lower().like(pat))
            );
        }

        // 카테고리
        if (q.categoryId() != null) {
            if (q.includeChildren()) {
                Set<Long> ids = categoryTreeService.resolveSubtreeIds(q.categoryId());
                if (ids.isEmpty()) {
                    // 존재하지 않게 만드는 가드
                    where.and(note.id.eq(-1L));
                } else {
                    where.and(note.category().id.in(ids));
                }
            } else {
                where.and(note.category().id.eq(q.categoryId()));
            }
        }

        // 가시성
        switch (q.scope() == null ? NoteQuery.VisibilityScope.PUBLIC_ONLY : q.scope()) {
            case PUBLIC_ONLY -> where.and(note.visibility.eq(Visibility.PUBLIC));
            case MINE_ONLY -> {
                if (q.viewerId() != null) {
                    where.and(note.owner().id.eq(q.viewerId()));
                } else {
                    // 인증 안 된 상태에서 MINE_ONLY 요청이면 결과 없음
                    where.and(note.id.eq(-1L));
                }
            }
            case MINE_OR_PUBLIC -> {
                if (q.viewerId() != null) {
                    where.and(note.owner().id.eq(q.viewerId())
                            .or(note.visibility.eq(Visibility.PUBLIC)));
                } else {
                    where.and(note.visibility.eq(Visibility.PUBLIC));
                }
            }
        }

        // 조회수 범위
        if (q.minViews() != null) where.and(note.viewCount.goe(q.minViews()));
        if (q.maxViews() != null) where.and(note.viewCount.loe(q.maxViews()));

        // 날짜 범위
        if (q.createdFrom() != null) where.and(note.createdAt.goe(q.createdFrom()));
        if (q.createdTo() != null)   where.and(note.createdAt.loe(q.createdTo()));
        if (q.updatedFrom() != null) where.and(note.updatedAt.goe(q.updatedFrom()));
        if (q.updatedTo() != null)   where.and(note.updatedAt.loe(q.updatedTo()));

        // 정렬
        OrderSpecifier<?>[] orders = toOrders(q.sortKey(), q.sortDir(), note);

        // 태그: ANY/ALL
        // ANY: left join tags + slug in (...)
        // ALL: slug 별 exists 서브쿼리
        var base = qf.selectFrom(note);

        if (q.hasTags()) {
            if (q.tagMode() == NoteQuery.TagMode.ANY) {
                base.leftJoin(note.tags, tag).on(tag.isNotNull());
                where.and(tag.slug.in(q.tagSlugs()));
            } else {
                // ALL
                BooleanBuilder all = new BooleanBuilder();
                for (String slug : q.tagSlugs()) {
                    all.and(
                            qf.selectOne()
                                    .from(note.tags, tag)
                                    .where(tag.slug.eq(slug).and(note.id.eq(QNote.note.id)))
                                    .exists()
                    );
                }
                where.and(all);
            }
        }

        // 카운트
        long total = base.clone()
                .where(where)
                .select(note.count())
                .fetchOne();

        if (total == 0L) {
            return new PageEnvelope<>(List.of(), pageable, 0);
        }

        // 페이지 데이터
        List<Note> rows = base.clone()
                .where(where)
                .orderBy(orders)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        var dtoList = rows.stream().map(NoteMapper::repoToResponse).toList();
        return new PageEnvelope<>(dtoList, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), total);
    }

    private OrderSpecifier<?>[] toOrders(String key, String dir, QNote note) {
        Sort s = NoteSorts.toSort(key, dir);
        List<OrderSpecifier<?>> out = new ArrayList<>();
        for (Sort.Order o : s) {
            Order ord = o.isAscending() ? Order.ASC : Order.DESC;
            String prop = o.getProperty();
            switch (prop) {
                case "updatedAt" -> out.add(new OrderSpecifier<>(ord, note.updatedAt));
                case "viewCount" -> out.add(new OrderSpecifier<>(ord, note.viewCount));
                case "title"     -> out.add(new OrderSpecifier<>(ord, note.title));
                default          -> out.add(new OrderSpecifier<>(ord, note.createdAt));
            }
            // tie-break
            out.add(new OrderSpecifier<>(ord, note.id));
        }
        return out.toArray(OrderSpecifier[]::new);
    }
}
