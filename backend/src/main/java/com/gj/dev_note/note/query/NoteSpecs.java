package com.gj.dev_note.note.query;

import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.tag.domain.Tag;
import jakarta.persistence.criteria.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoteSpecs {

    public static Specification<Note> textLike(String q) {
        if (q == null || q.isBlank()) return null;
        String pat = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pat),
                cb.like(cb.lower(root.get("content")), pat)
        );
    }

    public static Specification<Note> inCategoryIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return (root, cq, cb) -> root.get("category").get("id").in(ids);
    }

    public static Specification<Note> inCategoryId(Long id) {
        if (id == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("category").get("id"), id);
    }

    // 태그 ANY: join + IN
    public static Specification<Note> hasAnyTag(Set<String> slugs) {
        if (slugs == null || slugs.isEmpty()) return null;
        return (root, cq, cb) -> {
            root.fetch("tags", JoinType.LEFT); // 필요 시 페치 제거
            Join<Note, Tag> tag = root.join("tags", JoinType.LEFT);
            cq.distinct(true);
            CriteriaBuilder.In<String> in = cb.in(tag.get("slug"));
            slugs.forEach(in::value);
            return in;
        };
    }

    // 태그 ALL: 태그별 EXISTS 서브쿼리
    public static Specification<Note> hasAllTags(Set<String> slugs) {
        if (slugs == null || slugs.isEmpty()) return null;
        return (root, cq, cb) -> {
            Predicate[] must = slugs.stream().map(slug -> {
                Subquery<Long> sq = cq.subquery(Long.class);
                Root<Note> n2 = sq.from(Note.class);
                Join<Note, Tag> t2 = n2.join("tags");
                sq.select(n2.get("id"))
                        .where(
                                cb.equal(n2.get("id"), root.get("id")),
                                cb.equal(t2.get("slug"), slug)
                        );
                return cb.exists(sq);
            }).toArray(Predicate[]::new);
            return cb.and(must);
        };
    }

    public static Specification<Note> visibilityPublicOnly() {
        return (root, cq, cb) -> cb.equal(root.get("visibility"), Visibility.PUBLIC);
    }

    public static Specification<Note> visibilityMineOnly(Long viewerId) {
        if (viewerId == null) return null; // viewer 미확정일 땐 null 리턴
        return (root, cq, cb) -> cb.equal(root.get("owner").get("id"), viewerId);
    }

    public static Specification<Note> visibilityMineOrPublic(Long viewerId) {
        if (viewerId == null) return visibilityPublicOnly();
        return (root, cq, cb) -> cb.or(
                cb.equal(root.get("owner").get("id"), viewerId),
                cb.equal(root.get("visibility"), Visibility.PUBLIC)
        );
    }

    public static Specification<Note> viewsGte(Long min) {
        if (min == null) return null;
        return (root, cq, cb) -> cb.ge(root.get("viewCount"), min);
    }

    public static Specification<Note> viewsLte(Long max) {
        if (max == null) return null;
        return (root, cq, cb) -> cb.le(root.get("viewCount"), max);
    }

    public static Specification<Note> createdBetween(java.time.Instant from, java.time.Instant to) {
        if (from == null && to == null) return null;
        return (root, cq, cb) -> {
            Path<java.time.Instant> p = root.get("createdAt");
            if (from != null && to != null) return cb.between(p, from, to);
            if (from != null) return cb.greaterThanOrEqualTo(p, from);
            return cb.lessThanOrEqualTo(p, to);
        };
    }

    public static Specification<Note> updatedBetween(java.time.Instant from, java.time.Instant to) {
        if (from == null && to == null) return null;
        return (root, cq, cb) -> {
            Path<java.time.Instant> p = root.get("updatedAt");
            if (from != null && to != null) return cb.between(p, from, to);
            if (from != null) return cb.greaterThanOrEqualTo(p, from);
            return cb.lessThanOrEqualTo(p, to);
        };
    }
}
