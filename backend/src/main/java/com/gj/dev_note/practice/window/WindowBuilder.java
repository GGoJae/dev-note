package com.gj.dev_note.practice.window;

import com.gj.dev_note.practice.domain.PracticeSession;
import com.gj.dev_note.practice.domain.PracticeSessionItem;
import com.gj.dev_note.practice.domain.QuizAttempt;
import com.gj.dev_note.practice.dto.NextHint;
import com.gj.dev_note.practice.dto.ProgressSummary;
import com.gj.dev_note.practice.dto.WindowItem;
import com.gj.dev_note.practice.finalize.FinalizeGate;
import com.gj.dev_note.practice.policy.FeedbackPolicy;
import com.gj.dev_note.practice.repository.PracticeSessionItemRepository;
import com.gj.dev_note.practice.repository.QuizAttemptRepository;
import com.gj.dev_note.practice.response.WindowPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WindowBuilder {

    private final PracticeSessionItemRepository itemRepo;
    private final QuizAttemptRepository attemptRepo;
    private final FinalizeGate finalizeGate;

    public WindowPageResponse build(
            PracticeSession session, Long ownerId, int windowSize,
            int offset, FeedbackPolicy policy) {

        List<PracticeSessionItem> all = itemRepo.findAllBySessionIdOrderByOrderIndexAscIdAsc(session.getId());
        int size = all.size();
        int safeOffset = Math.max(0, Math.min(offset, size));
        int end = Math.min(safeOffset + windowSize, size);
        List<PracticeSessionItem> slice = all.subList(safeOffset, end);
        List<Long> itemIds = slice.stream().map(PracticeSessionItem::getId).toList();

        List<QuizAttempt> latestAttempts = attemptRepo.findLatestBySessionOwnerAndItems(session.getId(), ownerId, itemIds);

        Map<Long, QuizAttempt> latestByItemId = latestAttempts.stream().collect(
                Collectors.toMap(a -> a.getSessionItem().getId(), a -> a));

        List<WindowItem> items = new ArrayList<>(slice.size());
        for (var it : slice) {
            var a = latestByItemId.get(it.getId());
            Set<Long> mySelected = (a == null) ?
                    Set.of() :
                    a.getSelected().stream()
                            .map(ac -> ac.getChoice().getId())
                            .collect(Collectors.toCollection(LinkedHashSet::new));
            items.add(new WindowItem(it.getId(), it.getQuiz().getId(), it.getOrderIndex(), mySelected));
        }

        List<Long> ids = all.stream().map(PracticeSessionItem::getId).toList();
        Map<Long, QuizAttempt> latestAll = attemptRepo.findLatestBySessionOwnerAndItems(session.getId(), ownerId, ids)
                        .stream().collect(Collectors.toMap(a -> a.getSessionItem().getId(), a -> a)
        );

        var prog = policy.computeProgress(all, latestAll);
        boolean canBacktrack = policy.canBacktrackOrEdit();
        boolean canEdit = policy.canBacktrackOrEdit();
        boolean canPass = policy.canPass();

        NextHint next;
        if (end < all.size()) {
            next = NextHint.page(end);
        } else {
            boolean allowed = policy.allowFinalize(all, latestAll);
            String token = finalizeGate.ensureAndIssue(session, allowed, Instant.now());
            next = (token != null) ? NextHint.finalize(token) : NextHint.none();
        }

        ProgressSummary progress = new ProgressSummary(prog.attempted(), prog.correct(), session.getTotalCount());

        return new WindowPageResponse(session.getId(), windowSize, safeOffset, items,
                progress, canBacktrack, canEdit, canPass, next);
    }
}
