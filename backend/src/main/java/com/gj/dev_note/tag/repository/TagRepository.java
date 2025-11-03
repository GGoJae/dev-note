package com.gj.dev_note.tag.repository;

import com.gj.dev_note.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findBySlugIn(Set<String> slugs);
}
