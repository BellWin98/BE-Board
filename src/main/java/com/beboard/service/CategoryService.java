package com.beboard.service;

import com.beboard.dto.CategoryDto;
import com.beboard.entity.Category;
import com.beboard.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 목록 조회 (캐싱 적용)
     * @return 카테고리 목록
     */
    @Cacheable("categories")
    public List<CategoryDto.Response> getAllCategories() {
        List<Category> categories = categoryRepository.findByActiveOrderByDisplayOrderAsc(true);
        return categories.stream()
                .map(CategoryDto.Response::from)
                .collect(Collectors.toList());
    }
}
