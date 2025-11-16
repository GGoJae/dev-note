package com.gj.dev_note.practice.service;

import com.gj.dev_note.common.exception.exceptions.DumbDeveloperException;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.practice.domain.*;
import com.gj.dev_note.practice.dto.*;
import com.gj.dev_note.practice.mapper.PracticeMapper;
import com.gj.dev_note.practice.repository.*;
import com.gj.dev_note.practice.request.AnswerSubmitRequest;
import com.gj.dev_note.practice.request.FinalizeRequest;
import com.gj.dev_note.practice.request.SessionCreateRequest;
import com.gj.dev_note.practice.response.*;
import com.gj.dev_note.quiz.domain.Quiz;
import com.gj.dev_note.quiz.domain.QuizChoice;
import com.gj.dev_note.quiz.repository.QuizRepository;
import com.gj.dev_note.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PracticeSessionService {

    private static final int DEFAULT_WINDOW_SIZE = 5;

    private final PracticeSessionRepository sessionRepo;
    private final PracticeSessionItemRepository itemRepo;
    private final QuizAttemptRepository attemptRepo;
    private final AttemptChoiceRepository attemptChoiceRepo;
    private final UserQuizStatRepository statRepo;
    private final MemberRepository memberRepo;
    private final QuizRepository quizRepo;

    @Transactional
    public SessionCreatedResponse create(SessionCreateRequest req) {
        Long me = CurrentUser.id();
        Ordering ordering = req.ordering().toType();
        FeedbackMode feedbackMode = req.feedbackMode().toType();

        List<Quiz> quizzes = quizRepo.findAllById(req.quizIds());
        if (quizzes.isEmpty()) throw new IllegalArgumentException("퀴즈를 찾을 수 없습니다.");

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
                .orElseThrow(DumbDeveloperException::new);

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

        List<PracticeSessionItem> items = itemRepo.findAllBySessionIdOrderByOrderIndexAscIdAsc(saved.getId())
                .stream().limit(DEFAULT_WINDOW_SIZE).toList();

        var firstSummaries = PracticeMapper.toSummaries(items);
        String nextCursor = (saved.getTotalCount() > DEFAULT_WINDOW_SIZE) ? String.valueOf(DEFAULT_WINDOW_SIZE) : null;

        return new SessionCreatedResponse(saved.getId(), saved.getTotalCount(), saved.getSeed(), firstSummaries, nextCursor);
    }

    @Transactional(readOnly = true)
    public ResumeResponse resume(Long sessionId) {
        Long me = CurrentUser.id();
        PracticeSession s = requireSessionOwned(sessionId, me);

        // pivot 계산: UNTIL_CORRECT → correct인 마지막 다음 인덱스,
        // SECTION_END → 시도/패스 포함 마지막 다음 인덱스
        int pivot = computePivotIndex(s, me);

        int windowStart = Math.max(0, (pivot / DEFAULT_WINDOW_SIZE) * DEFAULT_WINDOW_SIZE);
        WindowPageResponse window = buildWindow(s, me, windowStart);

        return new ResumeResponse(s.getId(), DEFAULT_WINDOW_SIZE, pivot, window);
    }

    private int computePivotIndex(PracticeSession s, Long me) {
        List<PracticeSessionItem> items = s.getItems();
        FeedbackMode mode = s.getFeedbackMode();

        int n = items.size();
        for (int i = 0; i < n; i++) {
            Long itemId = items.get(i).getId();
            var last = attemptRepo.findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(itemId, me);
            if (last.isEmpty()) {
                return i; // 미시도 발견 → 여기서 진행
            }
            if (mode == FeedbackMode.UNTIL_CORRECT && !last.get().isCorrect()) {
                return i; // UNTIL_CORRECT에서 정답 아닐 시 해당 위치
            }
        }
        return n; // 전부 완료
    }

    @Transactional(readOnly = true)
    public WindowPageResponse window(Long sessionId, String cursor) {
        Long me = CurrentUser.id();
        PracticeSession s = requireSessionOwned(sessionId, me);

        int offset = parseOffset(cursor);
        return buildWindow(s, me, offset);
    }

    private WindowPageResponse buildWindow(PracticeSession s, Long me, int offset) {
        List<PracticeSessionItem> all = itemRepo.findAllBySessionIdOrderByOrderIndexAscIdAsc(s.getId());
        int end = Math.min(offset + DEFAULT_WINDOW_SIZE, all.size());
        List<PracticeSessionItem> slice = all.subList(offset, end);

        // 내 최신 선택 복원
        List<WindowItem> items = slice.stream().map(it -> {
            var last = attemptRepo.findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(it.getId(), me);
            Set<Long> mySelected = last.map(a -> a.getSelected().stream()
                            .map(ch -> ch.getChoice().getId())
                            .collect(Collectors.toCollection(LinkedHashSet::new)))
                    .orElseGet(LinkedHashSet::new);
            return new WindowItem(it.getId(), it.getQuiz().getId(), it.getOrderIndex(), mySelected);
        }).toList();

        ProgressSummary prog = computeProgress(s, me);

        // 권한 플래그
        boolean canBacktrack = (s.getFeedbackMode() == FeedbackMode.SECTION_END);
        boolean canEdit      = (s.getFeedbackMode() == FeedbackMode.SECTION_END);
        boolean canPass      = (s.getFeedbackMode() == FeedbackMode.SECTION_END);

        NextHint next;
        if (end < all.size()) {
            next = NextHint.page(end); // 다음 오프셋
        } else {
            // 마지막 윈도우 → finalize 게이트 판단
            String token = issueFinalizeTokenIfAllowed(s, me);
            next = (token != null) ? NextHint.finalize(token) : NextHint.none();
        }

        return new WindowPageResponse(s.getId(), DEFAULT_WINDOW_SIZE, offset, items, prog, canBacktrack, canEdit, canPass, next);
    }

    private ProgressSummary computeProgress(PracticeSession s, Long me) {
        int total = s.getTotalCount();
        int attempted; // 패스도 시도로 잡을지 정책화 가능(여기선 "시도/패스 기록 존재"를 attempted로 간주)
        int correct;

        if (s.getFeedbackMode() == FeedbackMode.UNTIL_CORRECT) {
            // UNTIL_CORRECT: 마지막 시도가 correct인 아이템 수 == attempted == correct
            correct   = (int) s.getItems().stream().filter(it ->
                    attemptRepo.findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(it.getId(), me)
                            .map(QuizAttempt::isCorrect).orElse(false)
            ).count();
            attempted = correct; // 해당 모드에선 사실상 correct == attempted로 해도 UX상 자연스러움
        } else {
            // SECTION_END: 최근 시도(오답/패스 포함) 있으면 attempted로 본다
            attempted = (int) s.getItems().stream().filter(it ->
                    attemptRepo.findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(it.getId(), me)
                            .isPresent()
            ).count();
            correct   = (int) s.getItems().stream().filter(it ->
                    attemptRepo.findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(it.getId(), me)
                            .map(QuizAttempt::isCorrect).orElse(false)
            ).count();
        }
        return new ProgressSummary(attempted, correct, total);
    }

    private String issueFinalizeTokenIfAllowed(PracticeSession s, Long me) {
        // 게이트 조건: 마지막 윈도우 시점 + 모드별 완료 요건 충족
        boolean allowed;
        if (s.getFeedbackMode() == FeedbackMode.UNTIL_CORRECT) {
            // 모두 정답이어야
            allowed = s.getItems().stream().allMatch(it ->
                    attemptRepo.findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(it.getId(), me)
                            .map(QuizAttempt::isCorrect).orElse(false)
            );
        } else {
            // SECTION_END: 모두 "시도 또는 패스" 기록이 있어야(혹은 정책상 일부 미시도도 허용할 수 있음)
            allowed = s.getItems().stream().allMatch(it ->
                    attemptRepo.findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(it.getId(), me)
                            .isPresent()
            );
        }
        if (!allowed) return null;

        // 이미 토큰 발급되었다면 재사용(유효기간 체크)
        Instant now = Instant.now();
        if (s.getFinalizeToken() != null && s.getFinalizeTokenExpiresAt() != null &&
                now.isBefore(s.getFinalizeTokenExpiresAt())) {
            return s.getFinalizeToken();
        }

        String token = RandomStringUtils.randomAlphanumeric(48);
        s.issueFinalizeToken(token, now.plus(Duration.ofMinutes(10)));
        return token;
    }

    private int parseOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) return 0;
        try { return Integer.parseInt(cursor.trim()); } catch (Exception ignore) { return 0; }
    }

    /*----------------- Pass -----------------*/

    @Transactional
    public WindowPageResponse pass(Long sessionId, Long sessionItemId) {
        Long me = CurrentUser.id();
        PracticeSession s = requireSessionOwned(sessionId, me);
        if (s.getFeedbackMode() == FeedbackMode.UNTIL_CORRECT) {
            throw new IllegalArgumentException("해당 모드에서는 pass를 허용하지 않습니다.");
        }

        PracticeSessionItem item = itemRepo.findByIdAndSessionId(sessionItemId, sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 아이템을 찾을 수 없습니다."));

        // 이미 시도있으면 스킵 기록 생략 가능. 여기선 "pass 기록"을 남겨 재개 시 포인터가 전진되도록.
        Member ownerRef = memberRepo.getReferenceById(me);
        boolean exists = attemptRepo.existsBySessionIdAndSessionItemIdAndOwnerId(sessionId, sessionItemId, me);
        if (!exists) {
            QuizAttempt a = QuizAttempt.builder()
                    .session(s).sessionItem(item).quiz(item.getQuiz())
                    .owner(ownerRef).correct(false).skipped(true)
                    .build();
            attemptRepo.save(a);
        }

        // pass 후 현재 윈도우 재계산(클라에서 cursor 유지 중이라면 그 offset으로 갱신)
        // UX상 자연스럽게 하려면 클라가 보고 있던 offset을 같이 넘겨도 됨. 여기선 0으로 단순화하거나 resume 사용.
        return buildWindow(s, me, 0);
    }

    /*----------------- 제출(정책 반영) -----------------*/

    @Transactional
    public AnswerResultResponse submit(Long sessionId, AnswerSubmitRequest req) {
        Long me = CurrentUser.id();

        PracticeSessionItem item = itemRepo.findByIdAndSessionId(req.sessionItemId(), sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 아이템을 찾을 수 없습니다."));

        PracticeSession session = item.getSession();
        if (!Objects.equals(session.getOwner().getId(), me))
            throw new IllegalArgumentException("세션 소유자가 아닙니다.");

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
        QuizAttempt attempt = QuizAttempt.builder()
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
                                    .orElseThrow(() -> new IllegalArgumentException("choice 불일치")))
                            .build())
                    .toList();
            attemptChoiceRepo.saveAll(ac);
        }

        // 통계 반영(제출된 문제만)
        UserQuizStat stat = statRepo.findByOwnerIdAndQuizId(me, quiz.getId())
                .orElseGet(() -> UserQuizStat.builder()
                        .owner(ownerRef).quiz(quiz)
                        .tries(0).corrects(0).recentWrongCount(0)
                        .build());
        stat.onAttempt(correct, Instant.now());
        statRepo.save(stat);

        // 피드백
        return switch (session.getFeedbackMode()) {
            case SECTION_END -> new AnswerResultResponse(correct, null, null);
            case UNTIL_CORRECT -> {
                int remaining = (int) correctSet.stream().filter(id -> !sel.contains(id)).count();
                yield new AnswerResultResponse(correct, remaining, null);
            }
        };
    }

    /*----------------- Finalize (토큰 게이트) -----------------*/

    @Transactional
    public FinalizeResponse finalizeSession(Long sessionId, FinalizeRequest req) {
        Long me = CurrentUser.id();

        PracticeSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(session.getOwner().getId(), me))
            throw new IllegalArgumentException("세션 소유자가 아닙니다.");

        // 토큰 검증
        if (!session.canUseFinalizeToken(req.token(), Instant.now())) {
            throw new IllegalArgumentException("finalize 토큰이 유효하지 않습니다.");
        }

        if (!session.isFinalized()) session.setFinalizedAt(Instant.now());

        List<PracticeSessionItem> items = itemRepo.findAllBySessionIdOrderByOrderIndexAscIdAsc(sessionId);

        int attempted = 0;
        int correctCnt = 0;
        List<FinalizeResponse.FinalizedItem> out = new ArrayList<>();

        for (var it : items) {
            var last = attemptRepo.findTopBySessionItemIdAndOwnerIdOrderByCreatedAtDesc(it.getId(), me);
            if (last.isEmpty()) {
                out.add(new FinalizeResponse.FinalizedItem(
                        session.getId(),
                        it.getQuiz().getId(),
                        false,
                        Set.of(),
                        // SECTION_END에서만 정답 공개(여긴 공개한다고 가정, 필요 시 정책 분리)
                        it.getQuiz().getChoices().stream().filter(QuizChoice::isCorrect).map(QuizChoice::getId).collect(Collectors.toSet())
                ));
                continue;
            }
            var a = last.get();
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
    // TODO 예외 정리하기

    private PracticeSession requireSessionOwned(Long sessionId, Long me) {
        PracticeSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (s.getExpiresAt().isBefore(Instant.now())) throw new IllegalStateException("세션 유효 시간이 만료 되었습니다.");
        if (!Objects.equals(s.getOwner().getId(), me))
            throw new IllegalArgumentException("세션 소유자가 아닙니다.");
        return s;
    }
}



