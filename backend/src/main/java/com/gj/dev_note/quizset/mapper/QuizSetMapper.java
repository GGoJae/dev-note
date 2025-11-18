package com.gj.dev_note.quizset.mapper;

import com.gj.dev_note.common.VisibilityDto;
import com.gj.dev_note.quizset.domain.QuizSet;
import com.gj.dev_note.quizset.domain.QuizSetItem;
import com.gj.dev_note.quizset.response.QuizSetOverview;
import com.gj.dev_note.quizset.response.QuizSetItemPreview;
import com.gj.dev_note.quizset.response.QuizSetPreview;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuizSetMapper {

    public static QuizSetPreview toSummary(QuizSet s) {
        return new QuizSetPreview(
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

    public static QuizSetOverview toDetail(QuizSet s, List<QuizSetItemPreview> first, Integer nextOffset) {
        return new QuizSetOverview(
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

    public static QuizSetItemPreview toItemSummary(QuizSetItem it) {
        return new QuizSetItemPreview(
                it.getId(),
                it.getQuiz().getId(),
                it.getOrderIndex(),
                it.getMemo(),
                it.isPinned(),
                it.getWeight()
        );
    }

    public static List<QuizSetItemPreview> toItemSummaries(List<QuizSetItem> rows) {
        return rows.stream().map(QuizSetMapper::toItemSummary).toList();
    }
}
