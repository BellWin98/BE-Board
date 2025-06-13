package com.beboard.repository;

import com.beboard.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * CategoryRepository JPA 테스트
 *
 * 실제 데이터베이스 없이 JPA 기능을 테스트합니다.
 * 커스텀 쿼리 메서드들의 정확성을 검증합니다.
 */
@DataJpaTest
@DisplayName("CategoryRepository 테스트")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = Category.builder()
                .name("자유게시판")
                .description("자유롭게 소통하는 공간")
                .displayOrder(1)
                .build();

        category2 = Category.builder()
                .name("질문게시판")
                .description("궁금한 것들을 질문하는 공간")
                .displayOrder(2)
                .build();
        category2.setActive(false); // 비활성 상태로 설정

        entityManager.persist(category1);
        entityManager.persist(category2);
        entityManager.flush();
    }

    @Test
    @DisplayName("이름으로 카테고리 조회")
    void findByName_Success() {
        // when
        Optional<Category> found = categoryRepository.findByName("자유게시판");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("자유게시판");
        assertThat(found.get().getDescription()).isEqualTo("자유롭게 소통하는 공간");
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 조회 시 빈 결과")
    void findByName_NotFound() {
        // when
        Optional<Category> found = categoryRepository.findByName("존재하지않음");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이름 존재 여부 확인")
    void existsByName_True() {
        // when
        boolean exists = categoryRepository.existsByName("자유게시판");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 ID 제외하고 이름 존재 여부 확인")
    void existsByNameAndIdNot_False() {
        // when
        boolean exists = categoryRepository.existsByNameAndIdNot("자유게시판", category1.getId());

        // then
        assertThat(exists).isFalse(); // 자기 자신은 제외하므로 false
    }

    @Test
    @DisplayName("활성 카테고리만 표시 순서대로 조회")
    void findByActiveOrderByDisplayOrderAsc_OnlyActive() {
        // when
        List<Category> activeCategories = categoryRepository.findByActiveOrderByDisplayOrderAsc(true);

        // then
        assertThat(activeCategories).hasSize(1);
        assertThat(activeCategories.get(0).getName()).isEqualTo("자유게시판");
        assertThat(activeCategories.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("비활성 카테고리 조회")
    void findByActiveOrderByDisplayOrderAsc_Inactive() {
        // when
        List<Category> inactiveCategories = categoryRepository.findByActiveOrderByDisplayOrderAsc(false);

        // then
        assertThat(inactiveCategories).hasSize(1);
        assertThat(inactiveCategories.get(0).getName()).isEqualTo("질문게시판");
        assertThat(inactiveCategories.get(0).isActive()).isFalse();
    }
}