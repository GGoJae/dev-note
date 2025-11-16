package com.gj.dev_note.practice.service;

import com.gj.dev_note.common.error.Errors;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.practice.domain.*;
import com.gj.dev_note.practice.finalize.FinalizeGate;
import com.gj.dev_note.practice.mapper.PracticeMapper;
import com.gj.dev_note.practice.policy.FeedbackPolicy;
import com.gj.dev_note.practice.policy.FeedbackPolicyFactory;
import com.gj.dev_note.practice.repository.*;
import com.gj.dev_note.practice.request.AnswerSubmitRequest;
import com.gj.dev_note.practice.request.FinalizeRequest;
import com.gj.dev_note.practice.request.SessionCreateRequest;
import com.gj.dev_note.practice.response.*;
import com.gj.dev_note.practice.window.WindowBuilder;
import com.gj.dev_note.quiz.domain.Quiz;
import com.gj.dev_note.quiz.domain.QuizChoice;
import com.gj.dev_note.quiz.repository.QuizRepository;
import com.gj.dev_note.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PracticeSessionService {

    // TODO 추후 ConfigurationProperties 고려
    private static final int DEFAULT_WINDOW_SIZE = 5;

    private final PracticeSessionRepository sessionRepo;
    private final PracticeSessionItemRepository itemRepo;
    private final QuizAttemptRepository attemptRepo;
    private final AttemptChoiceRepository attemptChoiceRepo;
    private final UserQuizStatRepository statRepo;
    private final MemberRepository memberRepo;
    private final QuizRepository quizRepo;

    private final FeedbackPolicyFactory policyFactory;
    private final WindowBuilder windowBuilder;
    private final FinalizeGate finalizeGate;

    @Transactional
    public SessionCreatedResponse create(SessionCreateRequest req) {
        Long me = CurrentUser.id();
        Ordering ordering = req.ordering().toType();
        FeedbackMode feedbackMode = req.feedbackMode().toType();

        List<Long> requested = req.quizIds().stream().toList();
        List<Quiz> quizzes = quizRepo.findAllById(req.quizIds());
        if (quizzes.isEmpty()) throw Errors.notFound("quiz", requested);

        if (quizzes.size() != requested.size()) {
            var foundIds = quizzes.stream().map(Quiz::getId).collect(Collectors.toSet());
            var missing = requested.stream().filter(id -> !foundIds.contains(id)).toList();
            throw Errors.notFound("quiz", missing);
        }
        // TODO 접근 권한 필터링 하기

        long seed = (ordering == Ordering.RANDOM)
                ? ThreadLocalRandom.current().nextLong()
                : 0L;

        List<Quiz> ordered = new ArrayList<>(quizzes);
        if (ordering == Ordering.RANDOM) {
            Collections.shuffle(ordered, new Random(seed));
        } else {
            ordered.sort(Comparator.comparing(Quiz::getId));
        }

        int limit = Math.min(req.limit(), ordered.size());
        ordered = ordered.subList(0, limit);

        Member owner = memberRepo.findById(me)
                .orElseThrow(Errors::internal);

        PracticeSession session = PracticeSession.builder()
                .owner(owner)
                .feedbackMode(feedbackMode)
                .ordering(ordering)
                .totalCount(limit)
                .seed(seed)
                .build();

        int idx = 0;
        for (var q : ordered) {
            PracticeSessionItem item = PracticeSessionItem.builder()
                    .quiz(q)
                    .orderIndex(idx++)
                    .build();
            session.addItem(item);
        }

        PracticeSession saved = sessionRepo.save(session);

        var firstItems = itemRepo.findAllBySessionIdOrderByOrderIndexAscIdAsc(saved.getId())
                .stream().limit(DEFAULT_WINDOW_SIZE).toList();
        var firstSummaries = PracticeMapper.toSummaries(firstItems);
        String nextCursor = (saved.getTotalCount() > DEFAULT_WINDOW_SIZE) ? String.valueOf(DEFAULT_WINDOW_SIZE) : null;

        return new SessionCreatedResponse(saved.getId(), saved.getTotalCount(), saved.getSeed(), firstSummaries, nextCursor);
    }

    @Transactional(readOnly = true)
    public ResumeResponse resume(Long sessionId) {
        Long me = CurrentUser.id();
        PracticeSession s = requireSessionOwned(sessionId, me);

        int pivot = computePivotIndex(s, me);
        int windowStart = Math.max(0, (pivot / DEFAULT_WINDOW_SIZE) * DEFAULT_WINDOW_SIZE);

        FeedbackPolicy policy = policyFactory.get(s.getFeedbackMode());
        WindowPageResponse window = windowBuilder.build(s, me, DEFAULT_WINDOW_SIZE, windowStart, policy);

        return new ResumeResponse(s.getId(), DEFAULT_WINDOW_SIZE, pivot, window);
    }

    @Transactional(readOnly = true)
    public WindowPageResponse window(Long sessionId, String cursor) {
        Long me = CurrentUser.id();
        PracticeSession s = requireSessionOwned(sessionId, me);
        int offset = parseOffset(cursor);
        FeedbackPolicy policy = policyFactory.get(s.getFeedbackMode());
        return windowBuilder.build(s, me, DEFAULT_WINDOW_SIZE, offset, policy);
    }

    private int computePivotIndex(PracticeSession s, Long me) {
        var items = s.getItems();
        int n = items.size();
        if (n == 0) return 0;

        List<Long> ids = items.stream().map(PracticeSessionItem::getId).toList();
        var latest = attemptRepo.findLatestBySessionOwnerAndItems(s.getId(), me, ids)
                .stream().collect(Collectors.toMap(a -> a.getSessionItem().getId(), a -> a));

        FeedbackMode mode = s.getFeedbackMode();
        for (int i = 0; i < n; i++) {
            var it = items.get(i);
            var a = latest.get(it.getId());
            if (a == null) return i;
            if (mode == FeedbackMode.UNTIL_CORRECT && !a.isCorrect()) return i;
        }
        return n;
    }



    @Transactional
    public WindowPageResponse pass(Long sessionId, Long sessionItemId) {
        Long me = CurrentUser.id();
        PracticeSession s = requireSessionOwned(sessionId, me);
        FeedbackPolicy policy = policyFactory.get(s.getFeedbackMode());
        if (!policy.canPass()) throw Errors.badRequest("해당 모드에서는 pass를 허용하지 않습니다.");

        PracticeSessionItem item = itemRepo.findByIdAndSessionId(sessionItemId, sessionId)
                .orElseThrow(() -> Errors.notFound("session-item", sessionItemId));

        boolean exists = attemptRepo.existsBySessionIdAndSessionItemIdAndOwnerId(sessionId, sessionItemId, me);
        if (!exists) {
            var ownerRef = memberRepo.getReferenceById(me);
            QuizAttempt qa = QuizAttempt.builder()
                    .session(s).sessionItem(item).quiz(item.getQuiz())
                    .owner(ownerRef).correct(false).skipped(true)
                    .build();
            attemptRepo.save(qa);
        }

        int pivot = computePivotIndex(s, me);
        int offset = Math.max(0, (pivot / DEFAULT_WINDOW_SIZE) * DEFAULT_WINDOW_SIZE);

        return windowBuilder.build(s, me, DEFAULT_WINDOW_SIZE, offset, policy);
    }


    @Transactional
    public AnswerResultResponse submit(Long sessionId, AnswerSubmitRequest req) {
        Long me = CurrentUser.id();

        PracticeSessionItem item = itemRepo.findByIdAndSessionId(req.sessionItemId(), sessionId)
                .orElseThrow(() -> Errors.notFound("session-item", req.sessionItemId()));

        PracticeSession session = item.getSession();
        if (!Objects.equals(session.getOwner().getId(), me))
            throw Errors.forbidden("세션 소유자가 아닙니다.");

        var quiz = item.getQuiz();
        Set<Long> correctSet = quiz.getChoices().stream()
                .filter(QuizChoice::isCorrect)
                .map(QuizChoice::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> sel = (req.selectedChoiceIds() == null) ? Set.of() : req.selectedChoiceIds();

        boolean hasWrong = sel.stream().anyMatch(id -> !correctSet.contains(id));
        boolean missing = correctSet.stream().anyMatch(id -> !sel.contains(id));
        boolean correct = !hasWrong && !missing;

        Member ownerRef = memberRepo.getReferenceById(me);
        var attempt = QuizAttempt.builder()
                .session(session).sessionItem(item).quiz(quiz)
                .owner(ownerRef).correct(correct).skipped(false)
                .build();
        var savedAttempt = attemptRepo.save(attempt);

        if (!sel.isEmpty()) {
            List<AttemptChoice> ac = sel.stream()
                    .map(cid -> AttemptChoice.builder()
                            .attempt(savedAttempt)
                            .choice(quiz.getChoices().stream()
                                    .filter(c -> Objects.equals(c.getId(), cid))
                                    .findFirst()
                                    .orElseThrow(() -> Errors.badRequest("choice 불일치")))
                            .build())
                    .toList();
            attemptChoiceRepo.saveAll(ac);
        }

        UserQuizStat stat = statRepo.findByOwnerIdAndQuizId(me, quiz.getId())
                .orElseGet(() -> UserQuizStat.builder()
                        .owner(ownerRef).quiz(quiz)
                        .tries(0).corrects(0).recentWrongCount(0)
                        .build());
        stat.onAttempt(correct, Instant.now());
        statRepo.save(stat);

        FeedbackPolicy policy = policyFactory.get(session.getFeedbackMode());
        return policy.onSubmit(correctSet, sel);
    }

    /*----------------- Finalize (토큰 게이트) -----------------*/

    @Transactional
    public FinalizeResponse finalizeSession(Long sessionId, FinalizeRequest req) {
        Long me = CurrentUser.id();

        PracticeSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> Errors.notFound("session", sessionId));
        if (!Objects.equals(session.getOwner().getId(), me)) {
            throw Errors.forbidden("세션 소유자가 아닙니다.");
        }

        if (!finalizeGate.validate(session, req.token(), Instant.now())) {
            throw Errors.badRequest("finalize 토큰이 유효하지 않습니다.");
        }

        if (!session.isFinalized()) session.setFinalizedAt(Instant.now());

        List<PracticeSessionItem> items = itemRepo.findAllBySessionIdOrderByOrderIndexAscIdAsc(sessionId);
        List<Long> ids = items.stream().map(PracticeSessionItem::getId).toList();
        var latest = attemptRepo.findLatestBySessionOwnerAndItems(sessionId, me, ids)
                .stream().collect(
                        Collectors.toMap(a -> a.getSessionItem().getId(), a -> a)
                );

        int attempted = 0;
        int correctCnt = 0;
        List<FinalizeResponse.FinalizedItem> out = new ArrayList<>();

        for (var it : items) {
            var a = latest.get(it.getId());
            if (a == null) {
                out.add(new FinalizeResponse.FinalizedItem(
                        session.getId(),
                        it.getQuiz().getId(),
                        false,
                        Set.of(),
                        it.getQuiz().getChoices().stream().filter(QuizChoice::isCorrect).map(QuizChoice::getId)
                                .collect(Collectors.toSet())
                ));
                continue;
            }
            attempted++;
            if (a.isCorrect()) correctCnt++;

            Set<Long> selectedIds = a.getSelected().stream().map(ch -> ch.getChoice().getId()).collect(Collectors.toSet());
            Set<Long> correctIds = it.getQuiz().getChoices().stream().filter(QuizChoice::isCorrect).map(QuizChoice::getId).collect(Collectors.toSet());

            out.add(new FinalizeResponse.FinalizedItem(
                    session.getId(),
                    it.getQuiz().getId(),
                    a.isCorrect(),
                    selectedIds,
                    correctIds
            ));
        }

        return new FinalizeResponse(attempted, correctCnt, out);
    }

    /*----------------- 공통 -----------------*/

    private PracticeSession requireSessionOwned(Long sessionId, Long me) {
        PracticeSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> Errors.notFound("session", sessionId));
        if (s.getExpiresAt() != null && s.getExpiresAt().isBefore(Instant.now())){
            throw Errors.badRequest("세션 유효 시간이 만료 되었습니다.");
        }

        if (!Objects.equals(s.getOwner().getId(), me)) {
            throw Errors.forbidden("세션 소유자가 아닙니다.");
        }
        return s;
    }

    private int parseOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) return 0;
        try { return Integer.parseInt(cursor.trim()); } catch (Exception ignore) { return 0; }
    }


}



