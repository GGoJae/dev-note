package com.gj.dev_note.tag.service;

import com.gj.dev_note.tag.domain.Tag;
import com.gj.dev_note.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepo;

    @Transactional
    public Set<Tag> resolveBySlugs(Set<String> rawSlugs) {
        if (rawSlugs == null || rawSlugs.isEmpty()) return Set.of();

        Set<String> slugs = rawSlugs.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Tag> existing = tagRepo.findAllBySlugIn(slugs);
        Map<String, Tag> bySlug = new HashMap<>();
        for (Tag t : existing) bySlug.put(t.getSlug(), t);

        for (var slug : slugs) {
            if (!bySlug.containsKey(slug)) {
                Tag t = Tag.builder()
                        .slug(slug)
                        .name(slug)
                        .build();
                try {
                    t = tagRepo.save(t);
                } catch (DataIntegrityViolationException e) {
                    t = tagRepo.findBySlug(slug).orElseThrow();
                }
                bySlug.put(slug, t);
            }
        }
        return new LinkedHashSet<>(bySlug.values());

    }
}
