package com.example.community.controller;

import com.example.community.domain.board.BoardDto;
import com.example.community.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 메인 페이지 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final BoardService boardService;

    /**
     * 메인 페이지 (인덱스)
     * "http://localhost:8080"로 접속해서 확인
     *
     * @param page 페이지 번호 (기본값: 1)
     * @param size 페이지 크기 (기본값: 10)
     * @param searchType 검색 타입 (title, content, writer, titleContent)
     * @param keyword 검색 키워드
     * @param model 뷰에 전달할 데이터
     * @return 메인 페이지 템플릿
     */
    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            Model model) {

        log.info("=== 메인 페이지 접속 ===");
        log.info("page = {}, size = {}, searchType = {}, keyword = {}",
                page, size, searchType, keyword);

        try {
            // 게시판 목록 조회
            List<BoardDto> boardList = new ArrayList<>();
            try {
                boardList = boardService.getList();
                log.info("게시판 목록 조회 완료: {} 개", boardList.size());
            } catch (Exception e) {
                log.warn("게시판 목록 조회 실패", e);
                boardList = new ArrayList<>();
            }

            // 임시 게시글 데이터 생성 (나중에 실제 데이터로 교체)
            List<Map<String, Object>> tempPosts = createTempPosts();

            // 빈 페이지 객체 생성 (게시글이 없어도 에러 방지)
            Page<?> postPage = Page.empty();

            // 모델에 데이터 추가
            model.addAttribute("boardList", boardList);
            model.addAttribute("postPage", postPage);
            model.addAttribute("tempPosts", tempPosts);  // 임시 게시글 추가
            model.addAttribute("searchType", searchType != null ? searchType : "");
            model.addAttribute("keyword", keyword != null ? keyword : "");

            log.info("=== 메인 페이지 로드 성공 ===");

            return "index";

        } catch (Exception e) {
            log.error("=== 메인 페이지 로드 중 에러 발생 ===", e);
            throw e;
        }
    }

    /**
     * 임시 게시글 데이터 생성
     * TODO: 나중에 실제 PostService로 교체
     */
    private List<Map<String, Object>> createTempPosts() {
        List<Map<String, Object>> posts = new ArrayList<>();

        // 게시글 1
        Map<String, Object> post1 = new HashMap<>();
        post1.put("id", 1);
        post1.put("title", "광주 맛집 추천 - 양동 시장 근처 맛집");
        post1.put("content", "광주 양동 시장 근처에 있는 숨은 맛집을 소개합니다. " +
                "30년 전통의 국밥집으로 진한 국물과 푸짐한 고기가 일품입니다. " +
                "위치: 광주광역시 동구 금남로 123\n" +
                "영업시간: 08:00 - 21:00\n" +
                "추천 메뉴: 소머리국밥, 수육\n" +
                "가격: 소머리국밥 9,000원, 수육(중) 35,000원");
        post1.put("writer", "맛집탐방러");
        post1.put("date", "02-09");
        post1.put("views", 156);
        posts.add(post1);

        // 게시글 2
        Map<String, Object> post2 = new HashMap<>();
        post2.put("id", 2);
        post2.put("title", "부산 해운대 맛집 - 회센타 추천");
        post2.put("content", "부산 해운대에서 회를 먹고 싶다면 이곳을 추천합니다. " +
                "싱싱한 회와 다양한 밑반찬이 제공됩니다. " +
                "위치: 부산광역시 해운대구 중동 456\n" +
                "영업시간: 11:00 - 22:00\n" +
                "추천 메뉴: 모듬회, 물회\n" +
                "가격: 모듬회(소) 40,000원, 물회 12,000원");
        post2.put("writer", "부산토박이");
        post2.put("date", "02-08");
        post2.put("views", 203);
        posts.add(post2);

        // 게시글 3
        Map<String, Object> post3 = new HashMap<>();
        post3.put("id", 3);
        post3.put("title", "전주 한옥마을 맛집 - 전통 한정식");
        post3.put("content", "전주 한옥마을에서 맛볼 수 있는 전통 한정식 맛집입니다. " +
                "정갈한 상차림과 정성 가득한 음식이 일품입니다. " +
                "위치: 전라북도 전주시 완산구 한옥마을길 789\n" +
                "영업시간: 12:00 - 20:00 (브레이크타임 15:00-17:00)\n" +
                "추천 메뉴: 전주 비빔밥 정식, 전주 한정식\n" +
                "가격: 비빔밥 정식 15,000원, 한정식 35,000원");
        post3.put("writer", "전주맛객");
        post3.put("date", "02-07");
        post3.put("views", 178);
        posts.add(post3);

        // 게시글 4
        Map<String, Object> post4 = new HashMap<>();
        post4.put("id", 4);
        post4.put("title", "서울 강남 맛집 - 삼겹살 맛집");
        post4.put("content", "강남역 근처 삼겹살 맛집을 소개합니다. " +
                "두툼한 삼겹살과 신선한 야채가 무한 리필입니다. " +
                "위치: 서울특별시 강남구 강남대로 234\n" +
                "영업시간: 17:00 - 01:00\n" +
                "추천 메뉴: 한돈 삼겹살, 목살\n" +
                "가격: 1인분 13,000원");
        post4.put("writer", "강남맛집러");
        post4.put("date", "02-06");
        post4.put("views", 289);
        posts.add(post4);

        // 게시글 5
        Map<String, Object> post5 = new HashMap<>();
        post5.put("id", 5);
        post5.put("title", "대전 성심당 - 베스트 빵집");
        post5.put("content", "대전 대표 빵집 성심당을 다녀왔습니다. " +
                "튀김소보로와 부추빵이 시그니처 메뉴입니다. " +
                "위치: 대전광역시 중구 은행동 567\n" +
                "영업시간: 08:00 - 22:00\n" +
                "추천 메뉴: 튀김소보로, 부추빵, 앙버터\n" +
                "가격: 튀김소보로 1,800원, 부추빵 2,000원");
        post5.put("writer", "빵덕후");
        post5.put("date", "02-05");
        post5.put("views", 412);
        posts.add(post5);

        return posts;
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