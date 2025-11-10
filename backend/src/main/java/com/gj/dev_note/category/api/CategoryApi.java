package com.gj.dev_note.category.api;

import com.gj.dev_note.category.request.CategoryCreateRequest;
import com.gj.dev_note.category.request.CategoryPatchRequest;
import com.gj.dev_note.category.response.CategoryDetail;
import com.gj.dev_note.category.response.CategorySummary;
import com.gj.dev_note.category.service.CategoryService;
import com.gj.dev_note.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryApi {

    private final CategoryService service;

    @GetMapping("/me/tree")
    public List<CategorySummary> myTree() {
        return service.myTree(CurrentUser.id());
    }

    @GetMapping("/global/tree")
    public List<CategorySummary> globalTree() {
        return service.globalTree();
    }

    @GetMapping("/{id}/subtree")
    public List<CategorySummary> subtree(@PathVariable Long id) {
        return service.subtree(CurrentUser.idOpt().orElse(null), id);
    }

    /* ===== 생성/수정/삭제 ===== */

    @PostMapping
    public ResponseEntity<CategoryDetail> create(@Valid @RequestBody CategoryCreateRequest req) {
        var out = service.create(CurrentUser.id(), req);
        return ResponseEntity.ok(out);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryDetail> patch(@PathVariable Long id,
                                                @RequestBody CategoryPatchRequest req) {
        var out = service.patch(CurrentUser.id(), id, req);
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(CurrentUser.id(), id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/ping")
    public String adminPing() {
        return "ok";
    }
}
