package com.beboard.service;

import com.beboard.dto.CategoryDto;
import com.beboard.entity.Category;
import com.beboard.exception.AlreadyExistsException;
import com.beboard.exception.CategoryHasPostsException;
import com.beboard.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * 각 메서드의 정상 동작과 예외 상황 모두 테스트
 * Mockito 사용하여 의존성 격리 -> 비즈니스 로직에만 집중
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService 테스트")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryDto.CreateRequest createRequest;
    private CategoryDto.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 카테고리 엔티티
        testCategory = Category.builder()
                .name("자유게시판")
                .description("자유롭게 소통하는 공간")
                .displayOrder(1)
                .build();

        // 리플렉션으로 id 설정
        ReflectionTestUtils.setField(testCategory, "id", 1L);

        // 테스트용 생성 요청
        createRequest = new CategoryDto.CreateRequest();
        createRequest.setName("질문게시판");
        createRequest.setDescription("궁금한 것들을 질문하는 공간");
        createRequest.setDisplayOrder(2);

        // 테스트용 수정 요청
        updateRequest = new CategoryDto.UpdateRequest();
        updateRequest.setName("수정된게시판");
        updateRequest.setDescription("수정된 설명");
        updateRequest.setDisplayOrder(3);
        updateRequest.setActive(true);
    }

    @Test
    @DisplayName("모든 활성 카테고리 조회 성공")
    void getAllCategories_Success() {

        // given
        List<Category> categories = Arrays.asList(testCategory);
        given(categoryRepository.findByActiveOrderByDisplayOrderAsc(true))
                .willReturn(categories);

        // when
        List<CategoryDto.Response> result = categoryService.getAllCategories();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("자유게시판");

        verify(categoryRepository).findByActiveOrderByDisplayOrderAsc(true);
        verify(categoryRepository).countPostsByCategories();
    }

    @Test
    @DisplayName("카테고리 상세 조회 성공")
    void getCategoryById_Success() {

        // given
        Long categoryId = 1L;
        given(categoryRepository.findById(categoryId))
                .willReturn(Optional.of(testCategory));
        given(categoryRepository.countPostsByCategoryId(categoryId))
                .willReturn(5L);

        // when
        CategoryDto.Response result = categoryService.getCategoryById(categoryId);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("자유게시판");
        assertThat(result.getPostCount()).isEqualTo(5L);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).countPostsByCategoryId(categoryId);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 조회 시 예외 발생")
    void getCategoryById_NotFound() {

        // given
        Long categoryId = 999L;
        given(categoryRepository.findById(categoryId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("카테고리를 찾을 수 없습니다. ID: " + categoryId);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).countPostsByCategoryId(categoryId);
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_Success() {

        // given
        given(categoryRepository.existsByName(createRequest.getName()))
                .willReturn(false);
        given(categoryRepository.save(any(Category.class)))
                .willReturn(testCategory);

        // when
        CategoryDto.Response result = categoryService.createCategory(createRequest);

        // then
        assertThat(result.getName()).isEqualTo("자유게시판");
        assertThat(result.getPostCount()).isEqualTo(0L);

        verify(categoryRepository).existsByName(createRequest.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("중복 이름으로 카테고리 생성 시 예외 발생")
    void createCategory_DuplicateName() {

        // given
        given(categoryRepository.existsByName(createRequest.getName()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(createRequest))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("이미 존재하는 카테고리입니다.");

        verify(categoryRepository).existsByName(createRequest.getName());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() {

        // given
        Long categoryId = 1L;
        given(categoryRepository.findById(categoryId))
                .willReturn(Optional.of(testCategory));
        given(categoryRepository.existsByNameAndIdNot(updateRequest.getName(), categoryId))
                .willReturn(false);
        given(categoryRepository.save(testCategory))
                .willReturn(testCategory);
        given(categoryRepository.countPostsByCategoryId(categoryId))
                .willReturn(3L);

        // when
        CategoryDto.Response result = categoryService.updateCategory(categoryId, updateRequest);

        // then
        assertThat(result.getName()).isEqualTo(updateRequest.getName());
        assertThat(result.getPostCount()).isEqualTo(3L);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).existsByNameAndIdNot(updateRequest.getName(), categoryId);
        verify(categoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() {
        // given
        Long categoryId = 1L;
        given(categoryRepository.findById(categoryId))
                .willReturn(Optional.of(testCategory));
        given(categoryRepository.countPostsByCategoryId(categoryId))
                .willReturn(0L);

        // when
        assertThatCode(() -> categoryService.deleteCategory(categoryId))
                .doesNotThrowAnyException();

        // then
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).countPostsByCategoryId(categoryId);
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    @DisplayName("게시글이 있는 카테고리 삭제 시 예외 발생")
    void deleteCategory_HasPosts() {
        // given
        Long categoryId = 1L;
        given(categoryRepository.findById(categoryId))
                .willReturn(Optional.of(testCategory));
        given(categoryRepository.countPostsByCategoryId(categoryId))
                .willReturn(5L);

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(CategoryHasPostsException.class)
                .hasMessageContaining("게시글이 존재하여 삭제할 수 없습니다");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).countPostsByCategoryId(categoryId);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("카테고리별 게시글 수 조회 성공")
    void getPostCountByCategory_Success() {
        // given
        Long categoryId = 1L;
        given(categoryRepository.existsById(categoryId))
                .willReturn(true);
        given(categoryRepository.countPostsByCategoryId(categoryId))
                .willReturn(15L);

        // when
        long result = categoryService.getPostCountByCategory(categoryId);

        // then
        assertThat(result).isEqualTo(15L);

        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository).countPostsByCategoryId(categoryId);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리의 게시글 수 조회 시 예외 발생")
    void getPostCountByCategory_CategoryNotFound() {
        // given
        Long categoryId = 999L;
        given(categoryRepository.existsById(categoryId))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> categoryService.getPostCountByCategory(categoryId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("카테고리를 찾을 수 없습니다");

        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository, never()).countPostsByCategoryId(any());
    }
}