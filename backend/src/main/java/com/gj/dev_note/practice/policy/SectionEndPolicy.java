package com.gj.dev_note.practice.policy;

import com.gj.dev_note.practice.domain.PracticeSessionItem;
import com.gj.dev_note.practice.domain.QuizAttempt;
import com.gj.dev_note.practice.response.AnswerResultResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SectionEndPolicy implements FeedbackPolicy{

    @Override
    public AnswerResultResponse onSubmit(Set<Long> correctChoiceIds, Set<Long> selectedChoiceIds) {
        boolean hasWrong = selectedChoiceIds.stream().anyMatch(id -> !correctChoiceIds.contains(id));
        boolean missing = correctChoiceIds.stream().anyMatch(id -> !selectedChoiceIds.contains(id));
        boolean correct = !hasWrong && !missing;
        return new AnswerResultResponse(correct, null, null);
    }

    @Override
    public boolean canPass() {
        return true;
    }

    @Override
    public boolean canBacktrackOrEdit() {
        return true;
    }

    @Override
    public Progress computeProgress(List<PracticeSessionItem> items, Map<Long, QuizAttempt> latestAttemptByItemId) {
        int attempted = 0;
        int correct = 0;

        for (var it : items) {
            var a = latestAttemptByItemId.get(it.getId());

            if (a != null) {
                attempted++;
                if (a.isCorrect()) correct++;

            }
        }
        return new Progress(attempted, correct);
    }

    @Override
    public boolean allowFinalize(List<PracticeSessionItem> items, Map<Long, QuizAttempt> latestAttemptByItemId) {

        for (var it : items) {
            if (!latestAttemptByItemId.containsKey(it.getId())) {
                return false;
            }
        }
        return true;
    }
}
