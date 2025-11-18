package com.gj.dev_note.quizset.mapper;

import com.gj.dev_note.common.VisibilityDto;
import com.gj.dev_note.quizset.domain.QuizSet;
import com.gj.dev_note.quizset.domain.QuizSetItem;
import com.gj.dev_note.quizset.response.QuizSetDetail;
import com.gj.dev_note.quizset.response.QuizSetItemSummary;
import com.gj.dev_note.quizset.response.QuizSetSummary;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuizSetMapper {

    public static QuizSetSummary toSummary(QuizSet s) {
        return new QuizSetSummary(
                s.getId(),
                s.getOwner().getId(),
                s.getName(),
                s.getDescription(),
                VisibilityDto.fromType(s.getVisibility()),
                s.getItemCount(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    public static QuizSetDetail toDetail(QuizSet s, List<QuizSetItemSummary> first, Integer nextOffset) {
        return new QuizSetDetail(
                s.getId(),
                s.getOwner().getId(),
                s.getName(),
                s.getDescription(),
                VisibilityDto.fromType(s.getVisibility()),
                s.getItemCount(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                first,
                nextOffset
        );
    }

    public static QuizSetItemSummary toItemSummary(QuizSetItem it) {
        return new QuizSetItemSummary(
                it.getId(),
                it.getQuiz().getId(),
                it.getOrderIndex(),
                it.getMemo(),
                it.isPinned(),
                it.getWeight()
        );
    }

    public static List<QuizSetItemSummary> toItemSummaries(List<QuizSetItem> rows) {
        return rows.stream().map(QuizSetMapper::toItemSummary).toList();
    }
}
