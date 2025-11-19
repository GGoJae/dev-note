package com.gj.dev_note.quiz.api;

import com.gj.dev_note.quiz.dto.QuizLite;
import com.gj.dev_note.quiz.request.QuizLiteBatchRequest;
import com.gj.dev_note.quiz.service.QuizQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizQueryApi {

    private final QuizQueryService service;

    @PostMapping("/lite")
    public List<QuizLite> liteBatch(@Valid @RequestBody QuizLiteBatchRequest req) {
        return service.getLiteBatch(req);
    }

    @GetMapping("/{quizId}/lite")
    public QuizLite liteOne(@PathVariable Long quizId) {
        return service.getLite(quizId);
    }
}
