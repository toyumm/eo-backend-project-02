package com.example.community.service;

import com.example.community.domain.board.BoardDto;
import com.example.community.domain.post.PostDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MypageServiceImpl 단위 테스트
 *
 * - 외부 의존성(PostService, CommentService, BoardService)은 Mock으로 대체
 * - getMyPosts()에서 게시글에 boardTitle이 정상적으로 채워지는지 검증
 */
public class MypageServiceImplTest {

    private PostService postService;
    private CommentService commentService;
    private BoardService boardService;

    private MypageServiceImpl mypageService;

    /**
     * 각 테스트 실행 전 Mock 객체 초기화
     */
    @BeforeEach
    public void setUp() {
        postService = mock(PostService.class);
        commentService = mock(CommentService.class);
        boardService = mock(BoardService.class);

        mypageService = new MypageServiceImpl(postService, commentService, boardService);
    }

    /**
     * [정상 케이스]
     * - 내가 작성한 게시글이 2개 존재
     * - boardService에서 boardId에 해당하는 게시판 제목을 정상 반환
     * - 결과 Page<PostDto>에 boardTitle이 정확히 채워지는지 검증
     */
    @Test
    public void getMyPosts_shouldFillBoardTitle() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // given: postService가 게시글 2개 반환
        PostDto p1 = PostDto.builder().id(10L).boardId(100L).title("글1").build();
        PostDto p2 = PostDto.builder().id(11L).boardId(200L).title("글2").build();

        Page<PostDto> postPage = new PageImpl<>(List.of(p1, p2), pageable, 2);
        when(postService.getMyPosts(userId, pageable)).thenReturn(postPage);

        // given: boardService가 boardId에 대응하는 게시판 제목 반환
        BoardDto b1 = BoardDto.builder().id(100L).title("자유게시판").build();
        BoardDto b2 = BoardDto.builder().id(200L).title("공지게시판").build();

        when(boardService.getByIds(List.of(100L, 200L))).thenReturn(List.of(b1, b2));

        // when
        Page<PostDto> result = mypageService.getMyPosts(userId, pageable);

        // then: boardTitle이 정확히 매핑되었는지 검증
        assertEquals("자유게시판", result.getContent().get(0).getBoardTitle());
        assertEquals("공지게시판", result.getContent().get(1).getBoardTitle());

        // 의존 서비스 호출 여부 검증
        verify(postService, times(1)).getMyPosts(userId, pageable);
        verify(boardService, times(1)).getByIds(List.of(100L, 200L));
        verifyNoMoreInteractions(boardService);
    }

    /**
     * [예외 케이스]
     * - 게시글은 존재하지만
     * - boardService에서 해당 boardId를 찾지 못하는 경우
     *
     * → boardTitle이 "알 수 없음"으로 채워지는지 검증
     */
    @Test
    public void getMyPosts_whenBoardMissing_shouldUseDefault() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        PostDto p1 = PostDto.builder().id(10L).boardId(999L).title("글1").build();
        Page<PostDto> postPage = new PageImpl<>(List.of(p1), pageable, 1);

        when(postService.getMyPosts(userId, pageable)).thenReturn(postPage);

        // boardService가 빈 리스트 반환(해당 boardId 없음)
        when(boardService.getByIds(List.of(999L))).thenReturn(List.of());

        Page<PostDto> result = mypageService.getMyPosts(userId, pageable);

        // then: 기본값으로 설정되는지 확인
        assertEquals("알 수 없음", result.getContent().get(0).getBoardTitle());
    }

    /**
     * [빈 결과 케이스]
     * - 내가 작성한 게시글이 없는 경우
     *
     * → 그대로 빈 Page를 반환해야 하며
     * → boardService는 호출되지 않아야 한다.
     */
    @Test
    public void getMyPosts_whenEmpty_shouldReturnAsIs() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Page<PostDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(postService.getMyPosts(userId, pageable)).thenReturn(emptyPage);

        Page<PostDto> result = mypageService.getMyPosts(userId, pageable);

        assertTrue(result.getContent().isEmpty());

        // 게시글이 없으면 boardService 호출 안 되는 게 정상
        verify(boardService, never()).getByIds(any());
    }
}
