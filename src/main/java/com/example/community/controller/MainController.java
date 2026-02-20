package com.example.community.controller;

import com.example.community.domain.board.BoardDto;
import com.example.community.domain.post.PostDto;
import com.example.community.service.BoardService;
import com.example.community.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 메인 페이지 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final PostService postService;
    private final BoardService boardService;

    /**
     * 메인 페이지 조회
     *
     * 게시글 목록 조회 및 검색 기능 지원
     * 공지 카테고리와 일반 게시판 분리
     * 인기 게시글 TOP 10 표시
     *
     * @param page 페이지 번호 (기본값: 1)
     * @param size 페이지 크기 (기본값: 15)
     * @param searchType 검색 타입 (title, content, writer, titleContent)
     * @param keyword 검색 키워드
     * @param model 메인 화면에 필요한 데이터 전달
     * @return index 템플릿
     */
    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "") String searchType,
            @RequestParam(defaultValue = "") String keyword,
            Model model) {

        log.info("index - page={}, size={}, searchType={}, keyword={}", page, size, searchType, keyword);

        // 1) 전체 게시판 조회
        List<BoardDto> allBoards = boardService.getList();

        // 2) 공지 카테고리 / 일반 게시판 분리
        List<BoardDto> noticeBoardList = allBoards.stream()
                .filter(b -> "NOTICE".equals(b.getCategory()))
                .toList();

        List<BoardDto> boardList = allBoards.stream()
                .filter(b -> b.getCategory() == null || !"NOTICE".equals(b.getCategory()))
                .toList();

        model.addAttribute("noticeBoardList", noticeBoardList);
        model.addAttribute("boardList", boardList);

        // 3) NOTICE 카테고리의 첫 번째 게시판 선택
        // final 변수로 선언
        final Long noticeBoardId;
        if (!noticeBoardList.isEmpty()) {
            noticeBoardId = noticeBoardList.get(0).getId();
            Pageable noticePageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
            var noticePage = postService.getList(noticeBoardId, noticePageable);
            model.addAttribute("noticeList", noticePage.getContent());
            log.info("공지사항 로드 성공: boardId={}, title={}, postCount={}",
                    noticeBoardId, noticeBoardList.get(0).getTitle(), noticePage.getContent().size());
        } else {
            log.warn("NOTICE 카테고리의 게시판이 없습니다");
            model.addAttribute("noticeList", List.of());
            noticeBoardId = null;
        }

        // 4) 게시글 목록 조회 (검색 또는 전체) - 공지사항 제외!
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "viewCount"));
        Page<PostDto> postPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색
            postPage = postService.searchPosts(searchType, keyword, pageable);
            log.info("검색 결과: {} 건", postPage.getTotalElements());
        } else {
            // 전체 목록 조회
            postPage = postService.getAllPosts(pageable);

            // final noticeBoardId
            if (noticeBoardId != null) {
                List<PostDto> filteredContent = postPage.getContent().stream()
                        .filter(post -> !post.getBoardId().equals(noticeBoardId))
                        .collect(Collectors.toList());

                postPage = new PageImpl<>(filteredContent, pageable, postPage.getTotalElements());
                log.info("공지사항 게시판 제외: 원본 {} 건 → 필터링 후 {} 건",
                        postPage.getContent().size() + filteredContent.size(),
                        filteredContent.size());
            }
        }

        model.addAttribute("postPage", postPage);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        // 5) 인기 게시글 TOP 10 (오른쪽 사이드바용)
        Pageable popularPageable = PageRequest.of(0, 10);
        Page<PostDto> popularPosts = postService.getPopularPosts(popularPageable);
        model.addAttribute("popularPosts", popularPosts.getContent());

        return "index";
    }

    /**
     * 에러 페이지
     * "http://localhost:8080/error"로 접속해서 확인
     */
    @GetMapping("/error")
    public String error() {
        log.info("에러 페이지 접속");
        return "error";
    }
}