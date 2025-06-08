package com.beboard.service;

import com.beboard.dto.PostDto;
import com.beboard.entity.*;
import com.beboard.repository.BookmarkRepository;
import com.beboard.repository.CategoryRepository;
import com.beboard.repository.PostRepository;
import com.beboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    /**
     * 게시글 목록 조회
     *
     * @param categoryId 카테고리 ID (선택)
     * @param search     검색어 (선택)
     * @param pageable   페이징 정보
     * @return 게시글 목록
     */
    public Page<PostDto.ListResponse> getPosts(Pageable pageable, Long categoryId, String sort, String search) {

        Pageable optimizedPageable = createOptimizedPageable(pageable, sort);
        Page<Post> postsPage;

        if (categoryId != null && search != null && !search.isBlank()) {
            // 카테고리 + 검색어 필터링
            postsPage = postRepository.searchPostsByCategory(categoryId, search, optimizedPageable);
        } else if (categoryId != null) {
            // 카테고리만 필터링
            postsPage = postRepository.findByCategoryIdAndNotDeleted(categoryId, optimizedPageable);
        } else if (search != null && !search.isBlank()) {
            // 검색어만 필터링
            postsPage = postRepository.searchPosts(search, optimizedPageable);
        } else {
            // 필터 없음
            postsPage = postRepository.findAll(optimizedPageable);
        }

        return postsPage.map(PostDto.ListResponse::from);
    }

    /**
     * 특정 사용자가 작성한 게시글 목록 조회
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 게시글 목록
     */
    public Page<PostDto.ListResponse> getPostsByAuthor(Long userId, String sort, Pageable pageable) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId);
        }

        Pageable optimizedPageable = createOptimizedPageable(pageable, sort);

        // 게시글 조회
        Page<Post> postsPage = postRepository.findByAuthorIdAndNotDeleted(userId, optimizedPageable);

        return postsPage.map(PostDto.ListResponse::from);
    }

    /**
     * 특정 사용자가 북마크한 게시글 목록 조회
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 게시글 목록
     */
    public Page<PostDto.ListResponse> getBookmarkedPosts(Long userId, String sort, Pageable pageable) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId);
        }

        Pageable optimizedPageable = createOptimizedPageable(pageable, sort);

        // 북마크한 게시글 조회
        Page<Post> postsPage = postRepository.findBookmarkedByUserIdAndNotDeleted(userId, optimizedPageable);

        return postsPage.map(PostDto.ListResponse::from);
    }

    /**
     * 게시글 상세 조회
     *
     * @param postId 게시글 ID
     * @param userId 현재 사용자 ID (북마크 상태 확인용, 없을 경우 null)
     * @return 게시글 상세 정보
     */
    public PostDto.DetailResponse getPost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 북마크 상태 확인
        boolean bookmarked = false;
        if (userId != null) {
            bookmarked = postRepository.isBookmarkedByUser(postId, userId);
        }

        return PostDto.DetailResponse.from(post, bookmarked);
    }

    /**
     * 게시글 생성
     *
     * @param requestDto 게시글 생성 요청 DTO
     * @param authorId   작성자 ID
     * @return 생성된 게시글 정보
     */
    @Transactional
    public PostDto.DetailResponse createPost(PostDto.Request requestDto, Long authorId) {
        // 카테고리 조회
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다. ID: " + requestDto.getCategoryId()));

        // 작성자 조회
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + authorId));

        // 게시글 엔티티 생성
        Post post = Post.builder()
                .author(author)
                .category(category)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .build();

        // 게시글 저장
        Post savedPost = postRepository.save(post);
        log.info("새 게시글 생성: ID={}, 제목={}", savedPost.getId(), savedPost.getTitle());

        return PostDto.DetailResponse.from(savedPost, false);
    }

    /**
     * 게시글 수정
     *
     * @param postId     게시글 ID
     * @param requestDto 게시글 수정 요청 DTO
     * @param userId     현재 사용자 ID
     * @return 수정된 게시글 정보
     */
    @Transactional
    public PostDto.DetailResponse updatePost(Long postId, PostDto.Request requestDto, Long userId) {
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 수정 권한 확인 (작성자 또는 관리자)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        if (!post.isAuthor(userId) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("게시글을 수정할 권한이 없습니다.");
        }

        // 카테고리 변경이 있으면 카테고리 조회
        Category category = null;
        if (requestDto.getCategoryId() != null) {
            category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다. ID: " + requestDto.getCategoryId()));
        }

        // 게시글 정보 업데이트
        post.update(requestDto.getTitle(), requestDto.getContent(), category);
        Post updatedPost = postRepository.save(post);
        log.info("게시글 수정: ID={}, 제목={}", updatedPost.getId(), updatedPost.getTitle());

        // 북마크 상태 확인
        boolean bookmarked = postRepository.isBookmarkedByUser(postId, userId);

        return PostDto.DetailResponse.from(updatedPost, bookmarked);
    }

    /**
     * 게시글 삭제 (소프트 삭제)
     *
     * @param postId 게시글 ID
     * @param userId 현재 사용자 ID
     */
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 삭제 권한 확인 (작성자 또는 관리자)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        if (!post.isAuthor(userId) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");
        }

        // 게시글 삭제 처리
        post.markAsDeleted();
        postRepository.save(post);
        log.info("게시글 삭제: ID={}, 제목={}", post.getId(), post.getTitle());
    }

    /**
     * 게시글 조회수 증가
     *
     * @param postId 게시글 ID
     */
    @Transactional
    public void incrementViews(Long postId) {
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. ID: " + postId));

        post.incrementViews();
        postRepository.save(post);
    }

    /**
     * 게시글 북마크 추가
     *
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return 북마크 추가 여부
     */
    @Transactional
    public boolean addBookmark(Long postId, Long userId) {
        // 이미 북마크한 경우 무시
        if (bookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            return false;
        }

        // 게시글 조회
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 북마크 추가
        bookmarkRepository.save(Bookmark.builder()
                .post(post)
                .user(user)
                .build());

        log.info("북마크 추가: 사용자={}, 게시글={}", userId, postId);
        return true;
    }

    /**
     * 게시글 북마크 제거
     *
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return 북마크 제거 여부
     */
    @Transactional
    public boolean removeBookmark(Long postId, Long userId) {
        // 북마크 레코드 삭제
        long deletedCount = bookmarkRepository.deleteByUserIdAndPostId(userId, postId);

        if (deletedCount > 0) {
            log.info("북마크 제거: 사용자={}, 게시글={}", userId, postId);
            return true;
        }

        return false;
    }

    /**
     * 인기 게시글 목록 조회
     *
     * @param limit 조회할 게시글 수
     * @return 인기 게시글 목록
     */
    public List<PostDto.ListResponse> getPopularPosts(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "views"));
        List<Post> popularPosts = postRepository.findPopularPosts(pageRequest);

        return popularPosts.stream()
                .map(PostDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 최근 게시글 수 조회 (특정 기간 이후)
     *
     * @param since 기준 시간
     * @return 최근 게시글 수
     */
    public long countRecentPosts(LocalDateTime since) {
        return postRepository.countPostsSince(since);
    }

    private Pageable createOptimizedPageable(Pageable pageable, String sortType) {
        Sort sortStrategy = switch (sortType) {
            case "popular" -> {
                /**
                 * 인기순: 조회수와 댓글 수를 종합적으로 고려
                 * 최근 활동도 반영하여 '핫한' 콘텐츠 우선
                 */
                yield Sort.by(
                        Sort.Order.desc("viewCount"),
                        Sort.Order.desc("commentCount"),
                        Sort.Order.desc("createdAt")
                );
            }
            case "newest" -> {
                yield Sort.by(Sort.Order.desc("createdAt"));
            }
            default -> {
                log.warn("알 수 없는 정렬 방식: {}, 기본값(newest) 적용", sortType);
                yield Sort.by(Sort.Order.desc("createdAt"));
            }
        };

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortStrategy
        );
    }
}
