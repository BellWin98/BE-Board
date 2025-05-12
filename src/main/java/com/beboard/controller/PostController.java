package com.beboard.controller;

import com.beboard.dto.response.PostResponseDto;
import com.beboard.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPosts(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String sort
    ){
        Page<PostResponseDto> posts = postService.getPosts(page, size, sort);

        return ResponseEntity.ok(posts);
    }
}
