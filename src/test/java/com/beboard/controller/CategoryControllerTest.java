package com.beboard.controller;

import com.beboard.dto.CategoryDto;
import com.beboard.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CategoryController 통합 테스트
 *
 * Spring MVC 테스트를 통해 HTTP 요청-응답 흐름을 검증합니다.
 * 인증/인가, JSON 직렬화/역직렬화, 유효성 검사 등을 포함합니다.
 */
@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController 테스트")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDto.Response categoryResponse;
    private CategoryDto.CreateRequest createRequest;
    private CategoryDto.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 응답 데이터
        categoryResponse = CategoryDto.Response.builder()
                .id(1L)
                .name("자유게시판")
                .description("자유롭게 소통하는 공간")
                .displayOrder(1)
                .active(true)
                .createdAt("2025-01-01 10:00:00")
                .updatedAt("2025-01-01 10:00:00")
                .postCount(10L)
                .build();

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
    @DisplayName("카테고리 목록 조회 - 성공")
    void getCategories_Success() throws Exception {
        // given
        List<CategoryDto.Response> categories = Arrays.asList(categoryResponse);
        given(categoryService.getAllCategories()).willReturn(categories);

        // when & then
        mockMvc.perform(get("/api/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("자유게시판"))
                .andExpect(jsonPath("$[0].postCount").value(10));

        verify(categoryService).getAllCategories();
    }

    @Test
    @DisplayName("카테고리 상세 조회 - 성공")
    void getCategory_Success() throws Exception {
        // given
        Long categoryId = 1L;
        given(categoryService.getCategoryById(categoryId)).willReturn(categoryResponse);

        // when & then
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("자유게시판"))
                .andExpect(jsonPath("$.description").value("자유롭게 소통하는 공간"));

        verify(categoryService).getCategoryById(categoryId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("카테고리 생성 - 성공 (관리자)")
    void createCategory_Success_Admin() throws Exception {
        // given
        given(categoryService.createCategory(any(CategoryDto.CreateRequest.class)))
                .willReturn(categoryResponse);

        // when & then
        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("자유게시판"));

        verify(categoryService).createCategory(any(CategoryDto.CreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("카테고리 생성 - 권한 없음 (일반 사용자)")
    void createCategory_Forbidden_User() throws Exception {
        // when & then
        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("카테고리 생성 - 유효성 검사 실패")
    void createCategory_ValidationFailed() throws Exception {
        // given
        createRequest.setName(""); // 빈 이름으로 설정

        // when & then
        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("입력 데이터 검증에 실패했습니다"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("카테고리 수정 - 성공")
    void updateCategory_Success() throws Exception {
        // given
        Long categoryId = 1L;
        given(categoryService.updateCategory(eq(categoryId), any(CategoryDto.UpdateRequest.class)))
                .willReturn(categoryResponse);

        // when & then
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("자유게시판"));

        verify(categoryService).updateCategory(eq(categoryId), any(CategoryDto.UpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("카테고리 삭제 - 성공")
    void deleteCategory_Success() throws Exception {
        // given
        Long categoryId = 1L;
        willDoNothing().given(categoryService).deleteCategory(categoryId);

        // when & then
        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(categoryId);
    }

    @Test
    @DisplayName("카테고리별 게시글 수 조회 - 성공")
    void getCategoryPostCount_Success() throws Exception {
        // given
        Long categoryId = 1L;
        given(categoryService.getPostCountByCategory(categoryId)).willReturn(25L);

        // when & then
        mockMvc.perform(get("/api/categories/{id}/post-count", categoryId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(25));

        verify(categoryService).getPostCountByCategory(categoryId);
    }
}