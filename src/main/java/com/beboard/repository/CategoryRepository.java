package com.beboard.repository;

import com.beboard.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
    boolean existsByName(String name);

    /**
     * 카테고리 이름 존재 여부 확인 (특정 ID 제외)
     * @param name 확인할 카테고리 이름
     * @param id 제외할 카테고리 ID
     * @return 존재 여부
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * 활성화된 모든 카테고리 조회 (표시 순서대로)
     * @return 활성화된 카테고리 목록
     */
    List<Category> findByActiveOrderByDisplayOrderAsc(boolean active);

    /**
     * 특정 카테고리의 게시글 수 조회
     * @param categoryId 카테고리 ID
     * @return 게시글 수
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.category.id = :categoryId AND p.deleted = false")
    long countPostsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 모든 카테고리의 게시글 수 조회
     * @return 카테고리별 게시글 수 목록 (Object[] 배열의 리스트로, [0]은 카테고리 ID, [1]은 게시글 수)
     */
    @Query("SELECT c.id, COUNT(p) FROM Category c LEFT JOIN Post p ON c.id = p.category.id AND p.deleted = false GROUP BY c.id")
    List<Long[]> countPostsByCategories();
}
