package com.gj.dev_note.practice.mapper;

import com.gj.dev_note.practice.domain.PracticeSessionItem;
import com.gj.dev_note.practice.dto.SessionItemSummary;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PracticeMapper {

    public static List<SessionItemSummary> toSummaries(List<PracticeSessionItem> rows) {
        return rows.stream()
                .map(it -> new SessionItemSummary(it.getId(), it.getQuiz().getId(), it.getOrderIndex()))
                .toList();
    }
}
