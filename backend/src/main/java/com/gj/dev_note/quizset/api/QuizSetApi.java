package com.gj.dev_note.quizset.api;

import com.gj.dev_note.quizset.request.*;
import com.gj.dev_note.quizset.response.QuizSetOverview;
import com.gj.dev_note.quizset.response.QuizSetItemsPage;
import com.gj.dev_note.quizset.response.QuizSetPreview;
import com.gj.dev_note.quizset.service.QuizSetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-sets")
@RequiredArgsConstructor
public class QuizSetApi {

    private final QuizSetService service;

    //TODO 리턴 타입 ResponseEntity 로 바꾸기

    @PostMapping
    public QuizSetOverview create(@Valid @RequestBody QuizSetCreateRequest req) {
        return service.create(req);
    }

    @GetMapping
    public List<QuizSetPreview> mySets() {
        return service.mySets();
    }

    @PatchMapping("/{setId}")
    public QuizSetOverview patch(@PathVariable Long setId,
                                 @Valid @RequestBody QuizSetPatchRequest req) {
        return service.patch(setId, req);
    }

    @DeleteMapping("/{setId}")
    public void delete(@PathVariable Long setId) {
        service.delete(setId);
    }

    @PostMapping("/{setId}/items")
    public QuizSetOverview addQuizzes(@PathVariable Long setId,
                                      @Valid @RequestBody AddQuizzesRequest req) {
        return service.addQuizzes(setId, req);
    }

    @GetMapping("/{setId}/items")
    public QuizSetItemsPage pageItems(@PathVariable Long setId,
                                      @RequestParam(required = false) Integer offset) {
        return service.pageItems(setId, offset);
    }

    @PatchMapping("/{setId}/items/order")
    public QuizSetOverview reorder(@PathVariable Long setId,
                                   @Valid @RequestBody ReorderRequest req) {
        return service.reorder(setId, req);
    }

    @DeleteMapping("/{setId}/items")
    public QuizSetOverview remove(@PathVariable Long setId,
                                  @Valid @RequestBody RemoveItemsRequest req) {
        return service.removeItems(setId, req);
    }

}
