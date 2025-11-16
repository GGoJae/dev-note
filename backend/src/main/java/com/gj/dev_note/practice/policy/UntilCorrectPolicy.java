package com.gj.dev_note.practice.policy;

import com.gj.dev_note.practice.domain.PracticeSessionItem;
import com.gj.dev_note.practice.domain.QuizAttempt;
import com.gj.dev_note.practice.response.AnswerResultResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UntilCorrectPolicy implements FeedbackPolicy{

    @Override
    public AnswerResultResponse onSubmit(Set<Long> correctChoiceIds, Set<Long> selectedChoiceIds) {
        boolean hasWrong = selectedChoiceIds.stream().anyMatch(id -> !correctChoiceIds.contains(id));
        boolean missing = correctChoiceIds.stream().anyMatch(id -> !selectedChoiceIds.contains(id));
        boolean correct = !hasWrong && !missing;
        int remaining = (int) correctChoiceIds.stream().filter(id -> !selectedChoiceIds.contains(id)).count();
        return new AnswerResultResponse(correct, remaining, null);
    }

    @Override
    public boolean canPass() {
        return false;
    }

    @Override
    public boolean canBacktrackOrEdit() {
        return false;
    }

    @Override
    public Progress computeProgress(List<PracticeSessionItem> items, Map<Long, QuizAttempt> latestAttemptByItemId) {
        int correct = 0;
        for (var it : items) {
            var a = latestAttemptByItemId.get(it.getId());
            if (a != null && a.isCorrect()) correct++;
        }
        return new Progress(correct, correct);
    }

    @Override
    public boolean allowFinalize(List<PracticeSessionItem> items, Map<Long, QuizAttempt> latestAttemptByItemId) {

        for (var it : items) {
            var a = latestAttemptByItemId.get(it.getId());
            if (a == null || !a.isCorrect()) return false;
        }
        return true;
    }
}
