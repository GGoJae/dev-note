package com.gj.dev_note.quiz.api;

import com.gj.dev_note.quiz.dto.QuizPlayView;
import com.gj.dev_note.quiz.request.QuizCreateRequest;
import com.gj.dev_note.quiz.service.QuizCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizCommandApi {

    private final QuizCommandService service;

    @PostMapping
    public QuizPlayView create(@Valid @RequestBody QuizCreateRequest createReq) {
        return service.create(createReq);
    }
}
