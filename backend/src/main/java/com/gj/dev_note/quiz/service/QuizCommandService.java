package com.gj.dev_note.quiz.service;

import com.gj.dev_note.common.error.Errors;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.quiz.domain.Quiz;
import com.gj.dev_note.quiz.domain.QuizChoice;
import com.gj.dev_note.quiz.dto.AnswerPolicyDto;
import com.gj.dev_note.quiz.dto.QuizChoiceLite;
import com.gj.dev_note.quiz.dto.QuizPlayView;
import com.gj.dev_note.quiz.mapper.QuizChoiceMapper;
import com.gj.dev_note.quiz.mapper.QuizMapper;
import com.gj.dev_note.quiz.repository.QuizRepository;
import com.gj.dev_note.quiz.request.QuizCreateRequest;
import com.gj.dev_note.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizCommandService {

    private final QuizRepository quizRepo;
    private final MemberRepository memberRepo;

    @Transactional
    public QuizPlayView create(@Valid QuizCreateRequest createReq) {
        Long me = CurrentUser.id();

        Member meRef = memberRepo.findById(me).orElseThrow(Errors::internal);

        validateAnswerPolicy(createReq.answerPolicy(), createReq.choices());


        Quiz build = Quiz.builder()
                .owner(meRef)
                .visibility(createReq.visibility().toType())
                .question(createReq.question())
                .explanation(createReq.explanation())
                .answerPolicy(createReq.answerPolicy().toType())
                .difficulty(createReq.difficulty())
                .build();

        int order = 0;
        var choices = createReq.choices();
        for (var c : choices) {
            QuizChoice choiceBuild = QuizChoice.builder()
                    .text(c.text())
                    .correct(c.correct())
                    .displayOrder(order++)
                    .build();

            build.addChoice(choiceBuild);
        }

        // TODO tag 맵핑하는 로직 작성

        Quiz saved = quizRepo.save(build);
        List<QuizChoiceLite> choiceLites = saved.getChoices().stream()
                .sorted(Comparator.comparing(QuizChoice::getDisplayOrder).thenComparing(QuizChoice::getId))
                .map(QuizChoiceMapper::toLite).toList();

        return QuizMapper.toPlayView(saved, choiceLites);
    }

    private void validateAnswerPolicy(AnswerPolicyDto policy, List<QuizCreateRequest.QuizChoiceCreateRequest> choices) {
        if (policy == null || choices == null) {
            throw Errors.badRequest("정답 정책 또는 보기 목록이 유효하지 않습니다.");
        }

        long correctCount = choices.stream().filter(QuizCreateRequest.QuizChoiceCreateRequest::correct).count();
        boolean ok = switch (policy) {
            case EXACTLY_ONE -> correctCount == 1;
            case EXACTLY_TWO -> correctCount == 2;
            case MULTIPLE    -> correctCount >= 2;
            case SECRET      -> correctCount >= 1;
        };
        if (!ok) throw Errors.badRequest("정답 정책과 정답 개수가 일치하지 않습니다.");
    }
}
