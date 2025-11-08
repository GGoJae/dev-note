package com.gj.dev_note.note.service;

import com.gj.dev_note.common.PageEnvelope;
import com.gj.dev_note.note.domain.Note;
import com.gj.dev_note.note.mapper.NoteMapper;
import com.gj.dev_note.note.query.NoteQuery;
import com.gj.dev_note.note.response.NoteSummary;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteReadServiceQdsl {

    private final JPAQueryFactory qf;
    private final NoteSearchDsl dsl;

    public PageEnvelope<NoteSummary> search(NoteQuery q, Pageable pageable) {
        var where = dsl.buildWhere(q);
        var orders = dsl.buildOrders(q);

        // count
        var countQuery = qf.select(dsl.note.id.countDistinct())
                .from(dsl.note);
        if (dsl.needsTagAny(q)) dsl.applyTagAnyJoin(countQuery, q);

        Long totalL = countQuery.where(where).fetchOne();
        long total = (totalL == null) ? 0L : totalL;
        if (total == 0L) return new PageEnvelope<>(List.of(), pageable, 0);

        // data
        var dataQuery = qf.selectFrom(dsl.note)
                .join(dsl.note.owner(), dsl.owner)
                .fetchJoin()
                .distinct();
        if (dsl.needsTagAny(q)) dsl.applyTagAnyJoin(dataQuery, q);

        List<Note> rows = dataQuery
                .where(where)
                .orderBy(orders)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        var dtoList = rows.stream().map(NoteMapper::toSummary).toList();
        return new PageEnvelope<>(dtoList, pageable, total);
    }
}
