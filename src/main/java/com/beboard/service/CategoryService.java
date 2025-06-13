package com.beboard.service;

import com.beboard.dto.CategoryDto;
import com.beboard.entity.Category;
import com.beboard.exception.AlreadyExistsException;
import com.beboard.exception.CategoryHasPostsException;
import com.beboard.repository.CategoryRepository;
import com.beboard.util.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 모든 활성 카테고리 목록 조회
     * Redis 캐싱을 적용하여 성능을 최적화
     * 카테고리는 자주 변경되지 않는 정보이므로, 캐싱으로 데이터베이스 부하를 줄일 수 있음
     * @return 활성 카테고리 목록 (표시 순서 기준 정렬)
     */
    @Cacheable(value = "categories", key = "'active-categories'")
    public List<CategoryDto.Response> getAllCategories() {
        List<Category> categories = categoryRepository.findByActiveOrderByDisplayOrderAsc(true);

        // 카테고리별 게시글 수도 함께 조회
        Map<Long, Long> postCountMap = getPostCountMap();

        return categories.stream()
                .map(category -> {
                    Long postCount = postCountMap.getOrDefault(category.getId(), 0L);
                    return CategoryDto.Response.from(category, postCount);
                })
                .collect(Collectors.toList());
    }

    @Cacheable(value = "category", key = "#id")
    public CategoryDto.Response getCategoryById(Long id) {
        log.debug("카테고리 상세 조회 시작: ID = {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다. ID: " + id));
        long postCount = categoryRepository.countPostsByCategoryId(id);
        log.debug("카테고리 상세 조회 완료: name = {}, postCount = {}", category.getName(), postCount);

        return CategoryDto.Response.from(category, postCount);
    }

    /**
     * 새 카테고리 생성
     * 비즈니스 규칙:
     * 1. 카테고리 이름은 중복될 수 없음
     * 2. 표시 순서가 없으면 자동으로 마지막 순서로 설정
     * 3. 생성 후 캐시 무효화
     *
     * @param request 카테고리 생성 요청 정보
     * @return 생성된 카테고리 정보
     * @throws AlreadyExistsException 카테고리 이름이 중복된 경우
     */
    @Transactional
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public CategoryDto.Response createCategory(CategoryDto.CreateRequest request) {
        log.info("카테고리 생성 시작: name = {}", request.getName());

        // 중복 검사
        validateDuplicateName(request.getName(), null);

        // 표시 순서 자동 설정
        if (request.getDisplayOrder() == null) {
            int nextOrder = getNextDisplayOrder();
            request.setDisplayOrder(nextOrder);
            log.debug("표시 순서 자동 설정: {}", nextOrder);
        }
        Category savedCategory = categoryRepository.save(request.toEntity());
        log.info("카테고리 생성 완료: ID = {}, name = {}", savedCategory.getId(), savedCategory.getName());

        return CategoryDto.Response.from(savedCategory, 0L);

    }

    /**
     * 카테고리 정보 수정
     * 비즈니스 규칙:
     * 1. 다른 카테고리와 이름이 중복될 수 없음
     * 2. 필요한 필드만 업데이트
     * 3. 수정 후 캐시 무효화
     *
     * @param id 수정할 카테고리 ID
     * @param request 카테고리 수정 요청 정보
     * @return 수정된 카테고리 정보
     * @throws NoSuchElementException 카테고리를 찾을 수 없는 경우
     * @throws AlreadyExistsException 카테고리 이름이 중복된 경우
     */
    @Transactional
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public CategoryDto.Response updateCategory(Long id, CategoryDto.UpdateRequest request) {
        log.info("카테고리 수정 시작: ID = {}, name = {}", id, request.getName());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다. ID: " + id));

        // 이름 변경 시 중복 검사
        // 변경 전과 이름 동일 시 검증 로직 통과
        if (!category.getName().equals(request.getName())) {
            validateDuplicateName(request.getName(), id);
        }

        // 변경된 필드만 엔티티 업데이트
        category.update(request.getName(), request.getDescription(),
                request.getDisplayOrder() != null ? request.getDisplayOrder() : category.getDisplayOrder());

        // 활성 상태 변경
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        Category updatedCategory = categoryRepository.save(category);
        long postCount = categoryRepository.countPostsByCategoryId(id);
        log.info("카테고리 수정 완료: ID = {}, name = {}", updatedCategory.getId(), updatedCategory.getName());

        return CategoryDto.Response.from(updatedCategory, postCount);
    }

    /**
     * 카테고리 삭제
     *
     * 비즈니스 규칙:
     * 1. 해당 카테고리에 게시글이 있으면 삭제 불가
     * 2. 물리적 삭제 (완전 제거)
     * 3. 삭제 후 캐시 무효화
     *
     * @param id 삭제할 카테고리 ID
     * @throws NoSuchElementException 카테고리를 찾을 수 없는 경우
     * @throws CategoryHasPostsException 카테고리에 게시글이 존재하는 경우
     */
    @Transactional
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public void deleteCategory(Long id) {
        log.info("카테고리 삭제 시작: ID = {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다. ID: " + id));
        long postCount = categoryRepository.countPostsByCategoryId(id);
        if (postCount > 0) {
            throw new CategoryHasPostsException(
                    String.format("카테고리에 %d개의 게시글이 존재하여 삭제할 수 없습니다. 카테고리: %s",
                            postCount, category.getName()));
        }
        categoryRepository.delete(category);
        log.info("카테고리 삭제 완료: ID = {}, name = {}", id, category.getName());
    }

    /**
     * 특정 카테고리의 게시글 수 조회
     *
     * @param id 카테고리 ID
     * @return 게시글 수
     */
    @Cacheable(value = "categoryPostCount", key = "#id")
    public long getPostCountByCategory(Long id) {
        log.debug("카테고리 게시글 수 조회: categoryId = {}", id);

        // 카테고리 존재여부 확인
        if (!categoryRepository.existsById(id)) {
            throw new NoSuchElementException("카테고리를 찾을 수 없습니다. ID: " + id);
        }
        long postCount = categoryRepository.countPostsByCategoryId(id);
        log.debug("카테고리 게시글 수: categoryId = {}, count = {}", id, postCount);

        return postCount;
    }

    public Map<String, Object> getCategoryStatistics() {
        return null;
    }

    /**
     * 모든 카테고리의 게시글 수를 Map 으로 반환
     *
     * @return 카테고리 ID를 키로 하는 게시글 수 맵
     */
    private Map<Long, Long> getPostCountMap() {
        List<Long[]> postCounts = categoryRepository.countPostsByCategories();

        return postCounts.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0], // categoryId
                        arr -> arr[1] // postCount
                ));
    }

    /**
     * 카테고리 이름 중복 여부 검증
     *
     * @param name      검증할 카테고리 이름
     * @param excludeId 검증에서 제외할 카테고리 ID (수정 시 사용)
     * @throws AlreadyExistsException 이름이 중복된 경우
     */
    private void validateDuplicateName(String name, Long excludeId) {
        boolean isDuplicate = excludeId != null
                ? categoryRepository.existsByNameAndIdNot(name, excludeId)
                : categoryRepository.existsByName(name);
        if (isDuplicate) {
            throw new AlreadyExistsException(ErrorCode.CATEGORY_ALREADY_EXIST);
        }
    }

    /**
     * 다음 표시 순서 값 계산
     *
     * @return 다음 표시 순서 값
     */
    private int getNextDisplayOrder() {
        return categoryRepository.findAll().stream()
                .mapToInt(Category::getDisplayOrder)
                .max()
                .orElse(0) + 1;
    }
}
