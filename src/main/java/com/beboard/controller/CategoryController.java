package com.beboard.controller;

import com.beboard.dto.CategoryDto;
import com.beboard.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

        return ResponseEntity.ok(categories);
    }
}
