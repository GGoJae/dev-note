package com.gj.dev_note.category.service;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryTreeService {

    private final CategoryRepository categoryRepo;

    // 간단 버전. 최적화 필요
    public Set<Long> resolveSubtreeIds(Long categoryId) {
        if (categoryId == null) return Set.of();
        Set<Long> out = new LinkedHashSet<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(categoryId);
        while (!stack.isEmpty()) {
            Long cur = stack.pop();
            if (!out.add(cur)) continue;
            List<Category> children = categoryRepo.findByParentId(cur);
            for (Category child : children) {
                stack.push(child.getId());
            }
        }
        return out;
    }
}
