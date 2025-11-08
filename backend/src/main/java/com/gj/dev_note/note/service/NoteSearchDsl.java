package com.gj.dev_note.note.service;

import com.gj.dev_note.category.service.CategoryTreeService;
import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.note.domain.QNote;
import com.gj.dev_note.note.query.NoteQuery;
import com.gj.dev_note.note.query.NoteSorts;
import com.gj.dev_note.tag.domain.QNoteTagMap;
import com.gj.dev_note.tag.domain.QTag;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
class NoteSearchDsl {

    private final CategoryTreeService categoryTreeService;

    // Q-타입(메서드형 Path를 쓰는 네 규칙에 맞춤)
    final QNote note = QNote.note;
    final QNoteTagMap ntm = QNoteTagMap.noteTagMap;
    final QTag tag = QTag.tag;
    final QNoteTagMap ntm2 = new QNoteTagMap("ntm2"); // 서브쿼리 alias
    final QTag tag2 = new QTag("tag2");

    /** 메인 where 조립 (텍스트/카테고리/스코프/범위 + 태그 ALL) */
    BooleanBuilder buildWhere(NoteQuery q) {
        var where = new BooleanBuilder();

        // 텍스트
        if (q.hasText()) {
            String pat = "%" + q.getQ().toLowerCase() + "%";
            where.and(note.title.lower().like(pat).or(note.content.lower().like(pat)));
        }

        // 카테고리(하위 포함)
        if (q.getCategoryId() != null) {
            if (q.isIncludeChildren()) {
                Set<Long> ids = categoryTreeService.resolveSubtreeIds(q.getCategoryId());
                if (ids == null || ids.isEmpty()) {
                    // 존재하지 않는 트리면 무의미한 결과
                    where.and(note.id.eq(-1L));
                } else {
                    where.and(note.category().id.in(ids));
                }
            } else {
                where.and(note.category().id.eq(q.getCategoryId()));
            }
        }

        // 스코프
        var scope = (q.getScope() == null) ? NoteQuery.VisibilityScope.PUBLIC_ONLY : q.getScope();
        switch (scope) {
            case PUBLIC_ONLY -> where.and(note.visibility.eq(Visibility.PUBLIC));
            case MINE_ONLY -> {
                if (q.getViewerId() == null) {
                    where.and(note.id.eq(-1L));
                } else {
                    where.and(note.owner().id.eq(q.getViewerId()));
                }
            }
            case MINE_OR_PUBLIC -> {
                if (q.getViewerId() == null) {
                    where.and(note.visibility.eq(Visibility.PUBLIC));
                } else {
                    where.and(note.owner().id.eq(q.getViewerId())
                            .or(note.visibility.eq(Visibility.PUBLIC)));
                }
            }
            case OWNER_PUBLIC -> {
                if (q.getOwnerId() == null) where.and(note.id.eq(-1L));
                else where.and(note.owner().id.eq(q.getOwnerId()))
                        .and(note.visibility.eq(Visibility.PUBLIC));
            }
            case OWNER_ACCESSIBLE -> {
                if (q.getOwnerId() == null) {
                    where.and(note.id.eq(-1L));
                } else if (q.getViewerId()!=null && q.getViewerId().equals(q.getOwnerId())) {
                    where.and(note.owner().id.eq(q.getOwnerId()));
                } else {
                    where.and(note.owner().id.eq(q.getOwnerId()))
                            .and(note.visibility.eq(Visibility.PUBLIC));
                }
            }
        }

        // 조회수/날짜 범위
        if (q.getMinViews() != null) where.and(note.viewCount.goe(q.getMinViews()));
        if (q.getMaxViews() != null) where.and(note.viewCount.loe(q.getMaxViews()));
        if (q.getCreatedFrom() != null) where.and(note.createdAt.goe(q.getCreatedFrom()));
        if (q.getCreatedTo() != null)   where.and(note.createdAt.loe(q.getCreatedTo()));
        if (q.getUpdatedFrom() != null) where.and(note.updatedAt.goe(q.getUpdatedFrom()));
        if (q.getUpdatedTo() != null)   where.and(note.updatedAt.loe(q.getUpdatedTo()));

        // 태그 ALL (exists 연쇄)
        if (q.hasTags() && q.getTagMode() == NoteQuery.TagMode.ALL) {
            var all = new BooleanBuilder();
            for (String slugVal : q.getTag()) {
                all.and(JPAExpressions.selectOne()
                        .from(ntm2)
                        .join(ntm2.tag(), tag2)
                        .where(ntm2.note().id.eq(note.id).and(tag2.slug.eq(slugVal)))
                        .exists());
            }
            where.and(all);
        }

        return where;
    }

    /** 태그 ANY가 필요한가? */
    boolean needsTagAny(NoteQuery q) {
        return q.hasTags() && q.getTagMode() == NoteQuery.TagMode.ANY;
    }

    /** 태그 ANY 조인을 쿼리에 적용 (count/data 동일하게 호출) */
    void applyTagAnyJoin(JPAQuery<?> query, NoteQuery q) {
        query.join(note.tags, ntm)
                .join(ntm.tag(), tag)
                .where(tag.slug.in(q.getTag()));
    }

    /** 정렬 빌드 (마지막에 id 타이브레이커 한 번만) */
    OrderSpecifier<?>[] buildOrders(NoteQuery q) {
        Sort s = NoteSorts.toSort(q.getSortKey(), q.getSortDir());
        List<OrderSpecifier<?>> out = new ArrayList<>();
        for (Sort.Order o : s) {
            Order ord = o.isAscending() ? Order.ASC : Order.DESC;
            switch (o.getProperty()) {
                case "updatedAt"        -> out.add(new OrderSpecifier<>(ord, note.updatedAt));
                case "viewCount"        -> out.add(new OrderSpecifier<>(ord, note.viewCount));
                case "title"            -> out.add(new OrderSpecifier<>(ord, note.title));
                case "contentUpdatedAt" -> out.add(new OrderSpecifier<>(ord, note.contentUpdatedAt));
                default                 -> out.add(new OrderSpecifier<>(ord, note.createdAt));
            }
        }
        // tie-break once
        out.add(new OrderSpecifier<>(Order.DESC, note.id));
        return out.toArray(OrderSpecifier[]::new);
    }
}
