package com.gj.dev_note.category.repository;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.category.domain.CategoryScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByParentId(Long parentId);
    List<Category> findByParentId(Long parentId);

    Optional<Category> findByIdAndScope(Long id, CategoryScope scope);

    Optional<Category> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByOwnerIdAndSlug(Long ownerId, String slug);

    boolean existsByOwnerIsNullAndSlug(String slug);

    List<Category> findAllByOwnerIdOrderByNameAsc(Long ownerId);

    List<Category> findAllByScopeOrderByNameAsc(CategoryScope scope);
}
