package com.beboard.service;

import com.beboard.dto.response.PostResponseDto;
import com.beboard.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPosts(int page, int size, String sort) {
        sort = sort.equalsIgnoreCase("newest") ? "createdAt" : "viewCount";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc(sort)));

        return postRepository.findAll(pageable).map(PostResponseDto::from);
    }

}
