package com.gj.dev_note.quizset.service;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.category.service.CategoryService;
import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.common.error.Errors;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.quiz.domain.Quiz;
import com.gj.dev_note.quiz.repository.QuizRepository;
import com.gj.dev_note.quizset.domain.QuizSet;
import com.gj.dev_note.quizset.domain.QuizSetItem;
import com.gj.dev_note.quizset.mapper.QuizSetMapper;
import com.gj.dev_note.quizset.repository.QuizSetItemRepository;
import com.gj.dev_note.quizset.repository.QuizSetRepository;
import com.gj.dev_note.quizset.request.*;
import com.gj.dev_note.quizset.response.QuizSetDetail;
import com.gj.dev_note.quizset.response.QuizSetItemsPage;
import com.gj.dev_note.quizset.response.QuizSetSummary;
import com.gj.dev_note.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizSetService {

    private static final int PAGE_SIZE = 20;

    private final QuizSetRepository setRepo;
    private final QuizSetItemRepository itemRepo;
    private final QuizRepository quizRepo;
    private final MemberRepository memberRepo;

    @Transactional
    public QuizSetDetail create(QuizSetCreateRequest req) {
        Long me = CurrentUser.id();
        Member owner = memberRepo.findById(me).orElseThrow(Errors::internal);

        QuizSet qs = QuizSet.builder()
                .owner(owner)
                .name(req.name().trim())
                .description(req.description())
                .visibility(req.visibility() == null ? Visibility.PRIVATE : req.visibility().toType())
                .itemCount(0)
                .build();

        QuizSet saved = setRepo.save(qs);

        return QuizSetMapper.toDetail(saved, List.of(), null);
    }

    @Transactional(readOnly = true)
    public List<QuizSetSummary> mySets() {
        Long me = CurrentUser.id();
        return setRepo.findAllByOwnerIdOrderByCreatedAtDesc(me).stream()
                .map(QuizSetMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizSetDetail get(Long setId) {
        Long me = CurrentUser.idOrNull();
        QuizSet qs = setRepo.findById(setId).orElseThrow(() -> Errors.notFound("quiz-set", setId));

        if (!canRead(me, qs)) throw Errors.forbidden("세트 열람 권한이 없습니다.");

        var first = firstPageItems(setId);
        Integer next = (qs.getItemCount() > first.size()) ? first.size() : null;

        return QuizSetMapper.toDetail(qs, QuizSetMapper.toItemSummaries(first), next);
    }

    @Transactional
    public QuizSetDetail patch(Long setId, QuizSetPatchRequest req) {
        Long me = CurrentUser.id();
        QuizSet qs = setRepo.findById(setId).orElseThrow(() -> Errors.notFound("quiz-set", setId));
        ensureOwner(me, qs);

        if (req.name() != null) qs.setName(req.name().trim());
        if (req.description() != null) qs.setDescription(req.description());
        if (req.visibility() != null) qs.setVisibility(req.visibility().toType());

        var first = firstPageItems(setId);
        Integer next = (qs.getItemCount() > first.size()) ? first.size() : null;

        return QuizSetMapper.toDetail(qs, QuizSetMapper.toItemSummaries(first), next);
    }

    @Transactional
    public void delete(Long setId) {
        Long me = CurrentUser.id();
        QuizSet qs = setRepo.findById(setId).orElseThrow(() -> Errors.notFound("quiz-set", setId));
        ensureOwner(me, qs);

        setRepo.delete(qs);
    }

    @Transactional
    public QuizSetDetail addQuizzes(Long setId, AddQuizzesRequest req) {
        Long me = CurrentUser.id();
        QuizSet qs = setRepo.findById(setId).orElseThrow(() -> Errors.notFound("quiz-set", setId));
        ensureOwner(me, qs);

        List<Quiz> quizzes = req.quizIds().isEmpty() ? List.of() : quizRepo.findAllById(req.quizIds());
        if (quizzes.isEmpty()) return get(setId);

        int startIdx = (int) itemRepo.countBySetId(setId);
        int idx = startIdx;

        Member meRef = memberRepo.getReferenceById(me);
        int added = 0;

        for (var q : quizzes) {
            if (itemRepo.existsBySetIdAndQuizId(setId, q.getId())) continue;
            QuizSetItem it = QuizSetItem.builder()
                    .set(qs)
                    .quiz(q)
                    .orderIndex(idx++)
                    .addedBy(meRef)
                    .build();
            itemRepo.save(it);
            added++;
        }
        if (added > 0) qs.incCount(added);

        var first = firstPageItems(setId);
        Integer next = (qs.getItemCount() > first.size()) ? first.size() : null;

        return QuizSetMapper.toDetail(qs, QuizSetMapper.toItemSummaries(first), next);
    }

    @Transactional
    public QuizSetItemsPage pageItems(Long setId, Integer offset) {
        Long me = CurrentUser.idOrNull();
        QuizSet qs = setRepo.findById(setId).orElseThrow(() -> Errors.notFound("quiz-set", setId));
        if (!canRead(me, qs)) throw Errors.forbidden("세트 열람 권한이 없습니다.");

        int off = (offset == null || offset < 0) ? 0 : offset;
        var rows = itemRepo.findAllBySetIdOrderByOrderIndexAscIdAsc(setId);
        int end = Math.min(off + PAGE_SIZE, rows.size());
        var slice = rows.subList(off, end);

        Integer next = (end < rows.size()) ? end : null;

        return new QuizSetItemsPage(QuizSetMapper.toItemSummaries(slice), next);
    }

    @Transactional
    public QuizSetDetail reorder(Long setId, ReorderRequest req) {
        Long me = CurrentUser.id();
        QuizSet qs = setRepo.findById(setId).orElseThrow(() -> Errors.notFound("quiz-set", setId));
        ensureOwner(me, qs);

        var all = itemRepo.findAllBySetIdOrderByOrderIndexAscIdAsc(setId);
        if (all.isEmpty()) return get(setId);

        Set<Long> currentIds = all.stream().map(QuizSetItem::getId).collect(Collectors.toSet());
        Set<Long> incoming = new LinkedHashSet<>(req.itemIdsInOrder());

        if (!currentIds.equals(incoming)) {
            throw Errors.badRequest("세트 아이템 전체와 동일한 집합이어야 재정렬할 수 있습니다.");
        }

        int idx = 0;
        for (Long id : incoming) {
            var it = all.stream().filter(x -> Objects.equals(x.getId(), id)).findFirst()
                    .orElseThrow();
            it.setOrderIndex(idx++);
        }

        var first = firstPageItems(setId);
        Integer next = (qs.getItemCount() > first.size()) ? first.size() : null;

        return QuizSetMapper.toDetail(qs, QuizSetMapper.toItemSummaries(first), next);
    }

    @Transactional
    public QuizSetDetail removeItems(Long setId, RemoveItemsRequest req) {
        Long me = CurrentUser.id();
        QuizSet qs = setRepo.findById(setId).orElseThrow(() -> Errors.notFound("quiz-set", setId));
        ensureOwner(me, qs);

        var victims = itemRepo.findAllBySetIdAndIdIn(setId, req.itemIds());
        if (victims.isEmpty()) return get(setId);

        itemRepo.deleteAll(victims);
        qs.incCount(-victims.size());

        var rest = itemRepo.findAllBySetIdOrderByOrderIndexAscIdAsc(setId);
        int i = 0;
        for (var it : rest) {
            it.setOrderIndex(i++);
        }

        var first = firstPageItems(setId);
        Integer next = (qs.getItemCount() > first.size()) ? first.size() : null;

        return QuizSetMapper.toDetail(qs, QuizSetMapper.toItemSummaries(first), next);

    }


    private void ensureOwner(Long me, QuizSet s) {
        if (me == null || !Objects.equals(s.getOwner().getId(), me)) {
            throw Errors.forbidden("세트 소유자가 아닙니다.");
        }
    }

    private boolean canRead(Long me, QuizSet s) {
        if (s.getVisibility() == Visibility.PUBLIC) return true;
        return me != null && Objects.equals(s.getOwner().getId(), me);
    }

    private List<QuizSetItem> firstPageItems(Long setId) {
        var rows = itemRepo.findAllBySetIdOrderByOrderIndexAscIdAsc(setId);
        int end = Math.min(PAGE_SIZE, rows.size());
        return rows.subList(0, end);
    }
}
