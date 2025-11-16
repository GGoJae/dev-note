package com.gj.dev_note.practice.policy;

import com.gj.dev_note.practice.domain.PracticeSessionItem;
import com.gj.dev_note.practice.domain.QuizAttempt;
import com.gj.dev_note.practice.response.AnswerResultResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FeedbackPolicy {

    AnswerResultResponse onSubmit(Set<Long> correctChoiceIds, Set<Long> selectedChoiceIds);

    boolean canPass();

    boolean canBacktrackOrEdit();

    Progress computeProgress(List<PracticeSessionItem> items,
                             Map<Long, QuizAttempt> latestAttemptByItemId);

    boolean allowFinalize(List<PracticeSessionItem> items,
                          Map<Long, QuizAttempt> latestAttemptByItemId);

    record Progress(
            int attempted,
            int correct
    ) {

    }
}
