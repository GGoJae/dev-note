package com.gj.dev_note.category.service;

import com.gj.dev_note.category.domain.Category;
import com.gj.dev_note.category.domain.CategoryScope;
import com.gj.dev_note.category.dto.CategoryScopeDto;
import com.gj.dev_note.category.mapper.CategoryMapper;
import com.gj.dev_note.category.repository.CategoryRepository;
import com.gj.dev_note.category.request.CategoryCreateRequest;
import com.gj.dev_note.category.request.CategoryPatchRequest;
import com.gj.dev_note.category.response.CategoryDetail;
import com.gj.dev_note.category.response.CategorySummary;
import com.gj.dev_note.common.error.Errors;
import com.gj.dev_note.member.domain.Member;
import com.gj.dev_note.member.domain.Role;
import com.gj.dev_note.member.repository.MemberRepository;
import com.gj.dev_note.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepo;
    private final MemberRepository memberRepo;
    private final NoteRepository noteRepo;
    private final CategoryTreeService treeService;

    /* ================== 생성 ================== */

    @Transactional
    public CategoryDetail create(Long actorId, CategoryCreateRequest req) {
        Objects.requireNonNull(actorId);

        if (req.scope() == null) throw Errors.badRequest("scope는 필수입니다.");
        var scope = req.scope().toType();

        Member owner = null;
        if (scope == CategoryScope.PERSONAL) {
            owner = memberRepo.getReferenceById(actorId);
        } else { // GLOBAL
            ensureAdmin(actorId);
        }

        Category parent = null;
        if (req.parentId() != null) {
            parent = loadAccessible(actorId, req.parentId());
            if (scope == CategoryScope.PERSONAL) {
                if (parent.getScope() != CategoryScope.PERSONAL) {
                    throw Errors.badRequest("개인 카테고리는 개인 카테고리 하위에만 생성할 수 있습니다.");
                }
                if (!Objects.equals(parent.getOwner().getId(), actorId)) {
                    throw Errors.forbidden("해당 카테고리의 하위에 생성할 권한이 없습니다.");
                }
            } else { // GLOBAL
                if (parent.getScope() != CategoryScope.GLOBAL) {
                    throw Errors.badRequest("글로벌 카테고리는 글로벌 카테고리 하위에만 생성할 수 있습니다.");
                }
            }
        }

        String name = req.name().trim();
        String slug = slugify(name);

        if (scope == CategoryScope.PERSONAL) {
            if (categoryRepo.existsByOwnerIdAndSlug(actorId, slug)) {
                slug = dedupPersonalSlug(actorId, slug);
            }
        } else {
            if (categoryRepo.existsByOwnerIsNullAndSlug(slug)) {
                slug = dedupGlobalSlug(slug);
            }
        }

        Category c = Category.builder()
                .scope(scope)
                .owner(owner)
                .parent(parent)
                .name(name)
                .slug(slug)
                .build();

        return CategoryMapper.toDetail(categoryRepo.save(c));
    }

    /* ================== 트리 조회 ================== */

    @Transactional(readOnly = true)
    public List<CategorySummary> myTree(Long actorId) {
        Objects.requireNonNull(actorId);
        var rows = categoryRepo.findAllByOwnerIdOrderByNameAsc(actorId);
        return toTree(rows);
    }

    @Transactional(readOnly = true)
    public List<CategorySummary> globalTree() {
        var rows = categoryRepo.findAllByScopeOrderByNameAsc(CategoryScope.GLOBAL);
        return toTree(rows);
    }

    @Transactional(readOnly = true)
    public List<CategorySummary> subtree(Long actorId, Long rootId) {
        // 접근 가능한 루트인지 검증
        var root = loadAccessible(actorId, rootId);
        // 트리 서비스가 있다면 그걸로 하위 id 뽑아도 되고,
        // 여기선 간단히 전체 조회 후 필터/빌드
        List<Category> rows = (root.getScope() == CategoryScope.GLOBAL)
                ? categoryRepo.findAllByScopeOrderByNameAsc(CategoryScope.GLOBAL)
                : categoryRepo.findAllByOwnerIdOrderByNameAsc(root.getOwner().getId());
        // root 기준으로 잘라서 빌드
        Set<Long> subtreeIds = treeService.resolveSubtreeIds(rootId); // 기존에 있던 기능 활용
        var filtered = rows.stream().filter(c -> subtreeIds.contains(c.getId())).toList();
        return toTree(filtered);
    }

    private List<CategorySummary> toTree(List<Category> rows) {
        Map<Long, CategorySummary> map = new LinkedHashMap<>();
        List<CategorySummary> roots = new ArrayList<>();

        // 먼저 모든 노드 생성
        for (Category c : rows) {
            var node = CategorySummary.leaf(
                    c.getId(),
                    c.getName(),
                    c.getSlug(),
                    c.getParent() == null ? null : c.getParent().getId(),
                    CategoryScopeDto.fromType(c.getScope()),
                    c.getOwner() == null ? null : c.getOwner().getId()
            );
            map.put(c.getId(), node);
        }

        // 부모-자식 연결
        for (Category c : rows) {
            var node = map.get(c.getId());
            Long pid = c.getParent() == null ? null : c.getParent().getId();
            if (pid == null || !map.containsKey(pid)) {
                roots.add(node);
            } else {
                map.get(pid).children().add(node);
            }
        }
        return roots;
    }

    /* ================== 수정/삭제 (관리자 체크 포함) ================== */

    @Transactional
    public CategoryDetail patch(Long actorId, Long categoryId, CategoryPatchRequest p) {
        Category c = categoryRepo.findById(categoryId).orElseThrow(() -> Errors.notFound("category-id", categoryId));
        ensureCanEdit(actorId, c);

        if (p.name() != null) {
            String newName = p.name().trim();
            if (!Objects.equals(newName, c.getName())) {
                String newSlug = slugify(newName);
                if (c.getScope() == CategoryScope.PERSONAL) {
                    Long ownerId = c.getOwner().getId();
                    if (!Objects.equals(c.getSlug(), newSlug)
                            && categoryRepo.existsByOwnerIdAndSlug(ownerId, newSlug)) {
                        newSlug = dedupPersonalSlug(ownerId, newSlug);
                    }
                } else {
                    if (!Objects.equals(c.getSlug(), newSlug)
                            && categoryRepo.existsByOwnerIsNullAndSlug(newSlug)) {
                        newSlug = dedupGlobalSlug(newSlug);
                    }
                }
                c.setName(newName);
                c.setSlug(newSlug);
            }
        }

        if (p.parentId() != null) {
            Category newParent = loadAccessible(actorId, p.parentId());

            Set<Long> subtree = treeService.resolveSubtreeIds(categoryId);
            if (subtree.contains(p.parentId())) {
                throw Errors.badRequest("자기 자신 또는 하위 노드로 이동할 수 없습니다.");
            }
            if (c.getScope() != newParent.getScope()) {
                throw Errors.badRequest("동일 스코프 간 이동만 가능합니다.");
            }
            if (c.getScope() == CategoryScope.PERSONAL &&
                    !Objects.equals(c.getOwner().getId(), newParent.getOwner().getId())) {
                throw Errors.forbidden("해당 카테고리 하위에 생성할 권한이 없습니다.");
            }
            c.setParent(newParent);
        }

        return CategoryMapper.toDetail(c);
    }

    @Transactional
    public void delete(Long actorId, Long categoryId) {
        Category c = categoryRepo.findById(categoryId).orElseThrow(() -> Errors.notFound("category-id",categoryId));
        ensureCanEdit(actorId, c);

        if (categoryRepo.existsByParentId(categoryId)) {
            throw Errors.badRequest("하위 카테고리가 있어 삭제할 수 없습니다.");
        }
        if (noteRepo.existsByCategoryId(categoryId)) {
            throw Errors.badRequest("노트에서 사용 중인 카테고리입니다.");
        }
        // TODO: quizRepo.existsByCategoryId(categoryId)

        categoryRepo.delete(c);
    }

    /* ================== 공용 검증 ================== */

    @Transactional(readOnly = true)
    public Category loadAccessible(Long actorId, Long categoryId) {
        Category c = categoryRepo.findById(categoryId).orElseThrow(() -> Errors.notFound("category-id",categoryId));
        if (!canAccess(actorId, c)) throw Errors.forbidden("해당 카테고리에 엑세스할 권한이 없습니다.");
        return c;
    }

    @Transactional(readOnly = true)
    public Category resolveForAssign(Long actorId, Long categoryId) {
        if (categoryId == null) return null;

        return loadAccessible(actorId, categoryId);
    }

    private boolean canAccess(Long actorId, Category c) {
        if (c.getScope() == CategoryScope.GLOBAL) return true;
        return actorId != null && c.getOwner() != null && Objects.equals(c.getOwner().getId(), actorId);
    }

    private void ensureCanEdit(Long actorId, Category c) {
        if (c.getScope() == CategoryScope.GLOBAL) {
            ensureAdmin(actorId);
            return;
        }
        if (actorId == null || c.getOwner() == null || !Objects.equals(c.getOwner().getId(), actorId)) {
            throw Errors.forbidden("해당 카테고리에 엑세스할 권한이 없습니다.");
        }
    }

    private void ensureAdmin(Long actorId) {
        Objects.requireNonNull(actorId);
        var isAdmin = memberRepo.findById(actorId)
                .map(m -> m.getRoles() != null && m.getRoles().contains(Role.ADMIN))
                .orElse(false);
        if (!isAdmin) throw Errors.forbidden("관리자만 허용됩니다.");
    }

    private String slugify(String s) {
        String t = s.trim();
        t = Normalizer.normalize(t, Normalizer.Form.NFKC);
        t = t.replaceAll("\\s+", "-").replace('_','-')
                .toLowerCase(Locale.ROOT);
        return t;
    }
    private String dedupPersonalSlug(Long ownerId, String base) {
        String s = base;
        for (int i = 2; i < 1000; i++) {
            if (!categoryRepo.existsByOwnerIdAndSlug(ownerId, s)) return s;
            s = base + "-" + i;
        }
        throw Errors.badRequest("슬러그 충돌이 과도합니다.");
    }
    private String dedupGlobalSlug(String base) {
        String s = base;
        for (int i = 2; i < 1000; i++) {
            if (!categoryRepo.existsByOwnerIsNullAndSlug(s)) return s;
            s = base + "-" + i;
        }
        throw Errors.badRequest("슬러그 충돌이 과도합니다.");
    }
}
