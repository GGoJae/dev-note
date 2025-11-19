package com.gj.dev_note.quiz.service;

import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.quiz.domain.Quiz;
import com.gj.dev_note.quiz.domain.QuizChoice;
import com.gj.dev_note.quiz.dto.QuizChoiceLite;
import com.gj.dev_note.quiz.dto.QuizLite;
import com.gj.dev_note.quiz.repository.QuizChoiceRepository;
import com.gj.dev_note.quiz.repository.QuizRepository;
import com.gj.dev_note.quiz.request.QuizLiteBatchRequest;
import com.gj.dev_note.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizQueryService {

    private final QuizRepository quizRepo;
    private final QuizChoiceRepository choiceRepo;

    @Transactional(readOnly = true)
    public List<QuizLite> getLiteBatch(QuizLiteBatchRequest req) {
        List<Long> distinctIds = req.ids().stream().distinct().toList();
        if (distinctIds.isEmpty()) return List.of();

        Long me = CurrentUser.idOrNull();

        List<Quiz> quizzes = quizRepo.findAllById(distinctIds);
        Map<Long, Quiz> quizById = quizzes.stream()
                .collect(Collectors.toMap(Quiz::getId, Function.identity(), (a,b)->a, LinkedHashMap::new));

        Set<Long> readableIds = quizById.values().stream()
                .filter(q -> canRead(me, q))
                .map(Quiz::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (readableIds.isEmpty()) return List.of();

        List<QuizChoice> allChoices = choiceRepo.findAllByQuizIdIn(readableIds);
        Map<Long, List<QuizChoice>> choicesByQuizId = allChoices.stream()
                .collect(Collectors.groupingBy(ch -> ch.getQuiz().getId(),
                        LinkedHashMap::new, Collectors.toList()));

        List<QuizLite> out = new ArrayList<>(readableIds.size());
        for (Long id : distinctIds) {
            Quiz q = quizById.get(id);
            if (q == null) continue;                 // 존재하지 않거나 삭제됨
            if (!readableIds.contains(id)) continue;

            List<QuizChoice> chs = choicesByQuizId.getOrDefault(id, List.of());
            List<QuizChoiceLite> liteChoices = chs.stream()
                    .map(c -> new QuizChoiceLite(c.getId(), c.getText()))
                    .toList();

            out.add(new QuizLite(
                    q.getId(),
                    q.getOwner().getId(),
                    q.getQuestion(),
                    q.getDifficulty(),
                    liteChoices,
                    q.getCreatedAt(),
                    q.getUpdatedAt()
            ));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public QuizLite getLite(Long quizId) {
        Long me = CurrentUser.idOrNull();
        Quiz q = quizRepo.findById(quizId).orElse(null);
        if (q == null || !canRead(me, q)) return null;

        List<QuizChoice> chs = choiceRepo.findAllByQuizIdIn(List.of(quizId));
        List<QuizChoiceLite> liteChoices = chs.stream()
                .map(c -> new QuizChoiceLite(c.getId(), c.getText()))
                .toList();

        return new QuizLite(
                q.getId(),
                q.getOwner().getId(),
                q.getQuestion(),
                q.getDifficulty(),
                liteChoices,
                q.getCreatedAt(),
                q.getUpdatedAt()
        );
    }

    private boolean canRead(Long me, Quiz q) {
        if (q.getVisibility() == Visibility.PUBLIC) return true;
        return me != null && Objects.equals(q.getOwner().getId(), me);
    }
}
