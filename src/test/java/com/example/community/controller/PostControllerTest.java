package com.example.community.controller;

import com.example.community.domain.user.UserEntity;
import com.example.community.domain.user.UserRole;
import com.example.community.persistence.UserRepository;
import com.example.community.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@Transactional
@Rollback
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

//     boardId 테스트용
    private static final String BOARD_ID = "1";

//     모든 테스트에서 사용할 "로그인 인증 정보"
    private Authentication testAuth;

    /**
     * 테스트 실행 전에 "로그인 사용자"를 DB에 만들고,
     * 컨트롤러가 @AuthenticationPrincipal CustomUserDetails 를 받기에
     * 테스트에서도 인증 Principal을 CustomUserDetails로 맞춰줘야 함.
     */
    @BeforeEach
    public void setUpAuth() {
        long uniq = System.nanoTime();

        UserEntity saved = userRepository.saveAndFlush(
                UserEntity.builder()
                        .username("testuser_" + uniq)
                        .password("pw")
                        .name("TESTUSER")
                        .nickname("tester_" + uniq)
                        .email("testuser_" + uniq + "@example.com")
                        .role(UserRole.USER)
                        .active(true)
                        .build()
        );

        CustomUserDetails principal = new CustomUserDetails(saved);

        this.testAuth = new UsernamePasswordAuthenticationToken(
                principal,
                principal.getPassword(),
                principal.getAuthorities()
        );
    }

    /**
     * 인증 정보를 주입하는 PostProcessor
     * SecurityContextHolder에도 세팅
     * 요청에도 authentication(auth) 적용
     */
    private RequestPostProcessor testUser() {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(testAuth);
            return authentication(testAuth).postProcessRequest(request);
        };
    }


//     게시글 생성 후, redirect URL에서 생성된 게시글 id를 추출한다.
    private String createPostAndGetId(String title, String content) throws Exception {
        MvcResult writeResult = mockMvc.perform(
                        post("/board/{boardId}/post/write", BOARD_ID)
                                .with(testUser())
                                .with(csrf())
                                .param("title", title)
                                .param("content", content)
                )
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectUrl = writeResult.getResponse().getRedirectedUrl();

        return UriComponentsBuilder.fromUriString(redirectUrl)
                .build()
                .getQueryParams()
                .getFirst("id");
    }


//     게시글 목록 페이지 접근 테스트

    @Test
    void testList() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.get("/board/{boardId}/post", BOARD_ID)
                        .with(testUser());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andDo(print());
    }

//    게시글 목록, 페이징 파라미터 테스트
    @Test
    void testListWithPaging() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.get("/board/{boardId}/post/list", BOARD_ID)
                        .with(testUser())
                        .param("page", "1")
                        .param("size", "10");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andDo(print());
    }

//    게시글 작성 테스트
    @Test
    void testWrite() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.post("/board/{boardId}/post/write", BOARD_ID)
                        .with(testUser())
                        .with(csrf())
                        .param("title", "[TEST] PostControllerTest#testWrite")
                        .param("content", "[TEST] PostControllerTest#testWrite");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andDo(print());
    }

//    게시글 상세 조회 테스트
    @Test
    void testRead() throws Exception {
        String createdId = createPostAndGetId("[TEST] PostControllerTest#testRead", "[TEST] content");

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/board/{boardId}/post/read", BOARD_ID)
                                .with(testUser())
                                .param("id", createdId)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

//    게시글 상세 조회, 페이징 파라미터 유지 테스트
    @Test
    void testReadWithPaging() throws Exception {
        String createdId = createPostAndGetId("[TEST] PostControllerTest#testReadWithPaging", "[TEST] content");

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/board/{boardId}/post/read", BOARD_ID)
                                .with(testUser())
                                .param("id", createdId)
                                .param("page", "2")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

//    게시글 수정 폼 진입 테스트
    @Test
    void testUpdateForm() throws Exception {
        String createdId = createPostAndGetId("[TEST] update form", "[TEST] content");

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/board/{boardId}/post/update", BOARD_ID)
                                .with(testUser())
                                .param("id", createdId)
                                .param("page", "1")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

//    게시글 수정 테스트
    @Test
    void testUpdate() throws Exception {
        String createdId = createPostAndGetId("[TEST] before update", "[TEST] before content");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/board/{boardId}/post/update", BOARD_ID)
                                .with(testUser())
                                .with(csrf())
                                .param("id", createdId)
                                .param("title", "[TEST] PostControllerTest#testUpdate")
                                .param("content", "[TEST] updated content")
                                .param("page", "1")
                                .param("size", "10")
                )
                .andExpect(status().is3xxRedirection())
                .andDo(print());

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/board/{boardId}/post/read", BOARD_ID)
                                .with(testUser())
                                .param("id", createdId)
                                .param("page", "1")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

//    게시글 삭제 처리 테스트
    @Test
    void testDelete() throws Exception {
        String createdId = createPostAndGetId("[TEST] delete", "[TEST] content");

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/board/{boardId}/post/delete", BOARD_ID)
                                .with(testUser())
                                .param("id", createdId)
                                .param("page", "1")
                                .param("size", "10")
                )
                .andExpect(status().is3xxRedirection())
                .andDo(print());
    }
}
