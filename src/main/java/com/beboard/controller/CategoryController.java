package com.beboard.controller;

import com.beboard.dto.CategoryDto;
import com.beboard.entity.Category;
import com.beboard.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto.Response>> getCategories() {
        log.info("카테고리 목록 조회 요청");
        List<CategoryDto.Response> categories = categoryService.getAllCategories();
        log.info("카테고리 목록 조회 완료: {} 개", categories.size());

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto.Response> getCategory(@PathVariable Long id) {
        log.info("카테고리 상세 조회 요청: ID = {}", id);
        CategoryDto.Response category = categoryService.getCategoryById(id);
        log.info("카테고리 상세 조회 완료: {}", category.getName());

        return ResponseEntity.ok(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.Response> createCategory(
        @Valid @RequestBody CategoryDto.CreateRequest request
    ) {
        log.info("카테고리 생성 요청: name = {}, description = {}", request.getName(), request.getDescription());
        CategoryDto.Response createdCategory = categoryService.createCategory(request);
        log.info("카테고리 생성 완료: ID = {}, name = {}", createdCategory.getId(), createdCategory.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.Response> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto.UpdateRequest request) {
        log.info("카테고리 수정 요청: ID = {}, name = {}, description = {}", id, request.getName(), request.getDescription());
        CategoryDto.Response updatedCategory = categoryService.updateCategory(id, request);
        log.info("카테고리 수정 완료: ID = {}, name = {}", updatedCategory.getId(), updatedCategory.getName());

        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("카테고리 삭제 요청: ID = {}", id);
        categoryService.deleteCategory(id);
        log.info("카테고리 삭제 완료: ID = {}", id);

        return ResponseEntity.noContent().build();
    }

    // 특정 카테고리의 게시글 수 조회
    @GetMapping("/{id}/post-count")
    public ResponseEntity<Map<String, Long>> getCategoryPostCount(@PathVariable Long id) {
        log.info("카테고리 게시글 수 조회 요청: ID = {}", id);
        long postCount = categoryService.getPostCountByCategory(id);
        log.info("카테고리 게시글 수 조회 완료: ID = {}, count = {}", id, postCount);

        return ResponseEntity.ok(Map.of("count", postCount));
    }

    /**
     * 모든 카테고리의 게시글 수 조회 (관리자 전용)
     * 관리자 대시보드에서 카테고리별 통계를 보여주기 위한 엔드포인트
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCategoryStatistics() {
        log.info("카테고리 통계 조회 요청");
        Map<String, Object> statistics = categoryService.getCategoryStatistics();
        log.info("카테고리 통계 조회 완료: 총 {} 개 카테고리", statistics.get("totalCategories"));

        return ResponseEntity.ok(statistics);
    }
}
