package com.example.community.service;

import com.example.community.domain.post.PostDto;
import com.example.community.domain.post.PostEntity;
import com.example.community.persistence.PostRepository;
import com.example.community.persistence.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
class PostServiceTest {
    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private PostEntity anyPost() {
        return postRepository.findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")))
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("posts 더미데이터가 없습니다."));
    }

    @Test
    public void testExists() {
        assertNotNull(postService);
        log.info("postService = {}", postService);
    }

    @Test
    public void testCreate() {
        Long boardId = 1L;
        Long userId = 1L;

        PostDto postDto = PostDto.builder()
                .title("[TEST] PostServiceTest#testCreate")
                .content("[TEST] PostServiceTest#testCreate")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        log.info("postDto(before) = {}", postDto);

        Long createdId = postService.create(boardId, postDto, userId);

        log.info("createdId = {}", createdId);
        log.info("postDto(after) = {}", postDto);

        assertNotNull(createdId);
        assertThat(createdId).isGreaterThan(0L);
    }

    @Test
    public void testRead() {
        Long boardId = 1L;
        Long userId = 1L;

        PostDto createDto = PostDto.builder()
                .title("[TEST] PostServiceTest#testRead")
                .content("read content")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        Long postId = postService.create(boardId, createDto, userId);

        int before = postRepository.findById(postId).orElseThrow().getViewCount();

        PostDto postDto = postService.read(postId);

        assertNotNull(postDto);
        log.info("postDto = {}", postDto);

        int after = postRepository.findById(postId).orElseThrow().getViewCount();
        assertThat(after).isEqualTo(before + 1);
    }

    @Test
    public void testUpdate() {
        Long boardId = 1L;
        Long userId = 1L;

        PostDto createDto = PostDto.builder()
                .title("[TEST] before update")
                .content("before update")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        Long postId = postService.create(boardId, createDto, userId);

        PostDto updateDto = PostDto.builder()
                .id(postId)
                .title("[TEST] PostServiceTest#testUpdate")
                .content("update content")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        log.info("updateDto = {}", updateDto);

        // 작성자 수정
        boolean result = postService.update(updateDto, userId);

        // DB 확인
        assertTrue(result);

        PostEntity updated = postRepository.findById(postId).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("[TEST] PostServiceTest#testUpdate");
        assertThat(updated.getContent()).isEqualTo("update content");
    }

    @Test
    // 글 작성자 삭제 성공 테스트
    public void testDelete_owner_success() {
        Long boardId = 1L;
        Long userId = 1L;

        PostDto createDto = PostDto.builder()
                .title("[TEST] delete owner")
                .content("delete owner")
                .postType((short) 0)
                .fixed((short) 0)
                .build();

        Long postId = postService.create(boardId, createDto, userId);

        boolean result = postService.delete(postId, userId);

        assertTrue(result);
        assertThat(postRepository.findById(postId)).isEmpty();
    }

    @Test
    public void testDelete_notOwner_fail() {
        Long boardId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        Long postId = postService.create(boardId,
                PostDto.builder().title("t").content("c").postType((short)0).fixed((short)0).build(),
                ownerId);

        boolean result = postService.delete(postId, otherUserId);

        assertFalse(result);
        assertThat(postRepository.findById(postId)).isPresent();
    }

    // 새로 추가된 메서드 테스트
    @Test
    public void testGetAllPosts() {
        // 테스트 게시글 생성
        Long boardId = 1L;
        Long userId = 1L;

        postService.create(boardId,
                PostDto.builder()
                        .title("[TEST] PostServiceTest#testGetAllPosts - 1")
                        .content("전체 조회 테스트 1")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .build(),
                userId);

        postService.create(boardId,
                PostDto.builder()
                        .title("[TEST] PostServiceTest#testGetAllPosts - 2")
                        .content("전체 조회 테스트 2")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .build(),
                userId);

        // 전체 게시글 조회
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<PostDto> result = postService.getAllPosts(pageable);

        log.info("전체 게시글 개수 = {}", result.getTotalElements());
        log.info("현재 페이지 게시글 개수 = {}", result.getContent().size());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().size()).isLessThanOrEqualTo(10);

        // 작성자 정보 포함 확인
        result.getContent().forEach(post -> {
            assertThat(post.getWriter()).isNotNull();
            log.info("게시글 = {}, 작성자 = {}", post.getTitle(), post.getWriter());
        });
    }

    @Test
    public void testSearchPosts_byTitle() {
        // 테스트 게시글 생성
        Long boardId = 1L;
        Long userId = 1L;
        String keyword = "제목검색키워드";

        postService.create(boardId,
                PostDto.builder()
                        .title("[TEST] " + keyword + " PostServiceTest#testSearchPosts")
                        .content("내용")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .build(),
                userId);

        // 제목 검색
        var pageable = PageRequest.of(0, 10);
        Page<PostDto> result = postService.searchPosts("title", keyword, pageable);

        log.info("검색 결과 개수 = {}", result.getTotalElements());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).anyMatch(post -> post.getTitle().contains(keyword));
    }

    @Test
    public void testSearchPosts_byContent() {
        // 테스트 게시글 생성
        Long boardId = 1L;
        Long userId = 1L;
        String keyword = "내용검색키워드";

        postService.create(boardId,
                PostDto.builder()
                        .title("[TEST] PostServiceTest#testSearchPosts")
                        .content(keyword + " 테스트 내용입니다")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .build(),
                userId);

        // 내용 검색
        var pageable = PageRequest.of(0, 10);
        Page<PostDto> result = postService.searchPosts("content", keyword, pageable);

        log.info("검색 결과 개수 = {}", result.getTotalElements());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).anyMatch(post -> post.getContent().contains(keyword));
    }

    @Test
    public void testSearchPosts_byTitleContent() {
        // 테스트 게시글 생성
        Long boardId = 1L;
        Long userId = 1L;
        String keyword = "통합검색키워드";

        // 제목에 키워드
        postService.create(boardId,
                PostDto.builder()
                        .title("[TEST] " + keyword + " 제목")
                        .content("내용")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .build(),
                userId);

        // 내용에 키워드
        postService.create(boardId,
                PostDto.builder()
                        .title("[TEST] 제목")
                        .content(keyword + " 내용")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .build(),
                userId);

        // 제목+내용 검색
        var pageable = PageRequest.of(0, 10);
        Page<PostDto> result = postService.searchPosts("titleContent", keyword, pageable);

        log.info("검색 결과 개수 = {}", result.getTotalElements());

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void testSearchPosts_emptyKeyword() {
        // 빈 키워드로 검색 시 전체 목록 반환
        var pageable = PageRequest.of(0, 10);
        Page<PostDto> result = postService.searchPosts("title", "", pageable);

        log.info("전체 게시글 개수 = {}", result.getTotalElements());

        assertThat(result).isNotNull();
    }

    @Test
    public void testGetPopularPosts() {
        // 조회수가 다른 테스트 게시글 생성
        Long boardId = 1L;
        Long userId = 1L;

        // 조회수 100
        Long postId1 = postService.create(boardId,
                PostDto.builder()
                        .title("[TEST] PostServiceTest#testGetPopularPosts - 인기글1")
                        .content("인기글 1")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .build(),
                userId);
        PostEntity entity1 = postRepository.findById(postId1).orElseThrow();
        for (int i = 0; i < 100; i++) {
            entity1.increaseViewCount();
        }
        postRepository.save(entity1);

        // 조회수 50
        Long postId2 = postService.create(boardId,
                PostDto.builder()
                        .title("[TEST] PostServiceTest#testGetPopularPosts - 인기글2")
                        .content("인기글 2")
                        .postType((short) 0)
                        .fixed((short) 0)
                        .build(),
                userId);
        PostEntity entity2 = postRepository.findById(postId2).orElseThrow();
        for (int i = 0; i < 50; i++) {
            entity2.increaseViewCount();
        }
        postRepository.save(entity2);

        // 인기 게시글 TOP 10 조회
        var pageable = PageRequest.of(0, 10);
        Page<PostDto> result = postService.getPopularPosts(pageable);

        log.info("인기 게시글 개수 = {}", result.getTotalElements());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();

        // 조회수 내림차순 정렬 확인
        for (int i = 0; i < result.getContent().size() - 1; i++) {
            assertThat(result.getContent().get(i).getViewCount())
                    .isGreaterThanOrEqualTo(result.getContent().get(i + 1).getViewCount());

            log.info("순위 {} - 제목 = {}, 조회수 = {}",
                    i + 1,
                    result.getContent().get(i).getTitle(),
                    result.getContent().get(i).getViewCount());
        }
    }
}