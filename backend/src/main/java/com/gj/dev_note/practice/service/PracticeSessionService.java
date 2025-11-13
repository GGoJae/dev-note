package com.gj.dev_note.practice.service;

import com.gj.dev_note.common.exception.exceptions.DumbDeveloperException;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.practice.domain.*;
import com.gj.dev_note.practice.dto.SessionItemSummary;
import com.gj.dev_note.practice.dto.SessionItemsPage;
import com.gj.dev_note.practice.mapper.PracticeMapper;
import com.gj.dev_note.practice.repository.*;
import com.gj.dev_note.practice.request.AnswerSubmitRequest;
import com.gj.dev_note.practice.request.SessionCreateRequest;
import com.gj.dev_note.practice.response.AnswerResultResponse;
import com.gj.dev_note.practice.response.FinalizeResponse;
import com.gj.dev_note.practice.response.SessionCreatedResponse;
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
                .stream().limit(20).toList();

        List<SessionItemSummary> firstPage = PracticeMapper.toSummaries(items);

        // TODO nextCursor 더 정교하게 만들기
        String nextCursor = (saved.getTotalCount() > 20) ? "offset:20" : null;

        return new SessionCreatedResponse(saved.getId(), saved.getTotalCount(), saved.getSeed(), firstPage, nextCursor);
    }

    @Transactional(readOnly = true)
    public SessionItemsPage page(Long sessionId, String cursor) {
        CurrentUser.id(); // 인증 보장 (owner 체크가 필요하다면 세션의 owner와 비교)
        int offset = parseOffset(cursor);
        List<PracticeSessionItem> all = itemRepo.findAllBySessionIdOrderByOrderIndexAscIdAsc(sessionId);
        int end = Math.min(offset + 20, all.size());
        List<SessionItemSummary> page = PracticeMapper.toSummaries(all.subList(offset, end));
        String next = (end < all.size()) ? ("offset:" + end) : null;
        return new SessionItemsPage(page, next);
    }

    private int parseOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) return 0;
        if (cursor.startsWith("offset:")) {
            try {
                return Integer.parseInt(cursor.substring("offset:".length()));
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    // TODO 전략패턴으로 각 전략별로 응답해주는 클래스로 나누기
    @Transactional
    public AnswerResultResponse submit(Long sessionId, AnswerSubmitRequest req) {
        Long me = CurrentUser.id();

        PracticeSessionItem item = itemRepo.findByIdAndSessionId(req.sessionItemId(), sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 아이템을 찾을 수 없습니다."));

        PracticeSession session = item.getSession();
        if (!Objects.equals(session.getOwner().getId(), me))
            throw new IllegalArgumentException("세션 소유자가 아닙니다.");

        boolean exists = attemptRepo.existsBySessionIdAndSessionItemIdAndOwnerId(sessionId, item.getId(), me);
        if (exists && session.getFeedbackMode() != FeedbackMode.UNTIL_CORRECT) {
        }

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
                .owner(ownerRef).correct(correct)
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

        // 피드백 정책별 응답
        return switch (session.getFeedbackMode()) {
            case IMMEDIATE -> new AnswerResultResponse(
                    correct,
                    null,
                    correctSet
            );
            case SECTION_END -> new AnswerResultResponse(
                    correct,
                    null,
                    null
            );
            case UNTIL_CORRECT -> {
                int remaining = (int) correctSet.stream().filter(id -> !sel.contains(id)).count();
                yield new AnswerResultResponse(correct, remaining, null);
            }
        };
    }

    @Transactional
    public FinalizeResponse finalize(Long sessionId) {
        Long me = CurrentUser.id();

        PracticeSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(session.getOwner().getId(), me))
            throw new IllegalArgumentException("세션 소유자가 아닙니다.");

        if (!session.isFinalized()) session.setFinalizedAt(Instant.now());

        List<PracticeSessionItem> items = itemRepo.findAllBySessionIdOrderByOrderIndexAscIdAsc(sessionId);

        int attempted = 0;
        int correctCnt = 0;
        List<FinalizeResponse.FinalizedItem> out = new ArrayList<>();

            /*
            TODO
            for (PracticeSessionItem it : items) {
            최근 시도로 대체하려면 별도 쿼리 필요. 여기선 exists 여부만 가정/축약.
            실제 구현: attemptRepo.findTopBySessionItemIdOrderByCreatedAtDesc(...)
            여기선 생략하고 리뷰 제외(미시도) 처리.

            리뷰를 위해선 attempt join이 필요 -> 실전 구현시 보강!
        }
             */
        return new FinalizeResponse(attempted, correctCnt, out);
    }
}



