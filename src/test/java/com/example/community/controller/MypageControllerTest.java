package com.example.community.controller;

import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.UserRepository;
import com.example.community.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Slf4j
class MypageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private CustomUserDetails principal;
    private Long userId;

    @BeforeEach
    public void setUp() {
        log.info("테스트세팅 시작");

        UserEntity userEntity = UserEntity.builder()
                .username("TEST_USER")
                .password(passwordEncoder.encode("1234"))
                .name("TEST_NAME")
                .nickname("TEST_NICKNAME")
                .email("testuser@test.com")
                .build();

        UserEntity saved = userRepository.save(userEntity);
        userId = saved.getId();
        principal = new CustomUserDetails(saved);

        log.info("테스트세팅 완료 - userId={}", userId);
    }

    @Test
    public void mypage_shouldReturnUser_whenLoggedIn() throws Exception {
        log.info("테스트시작 mypage_shouldReturnUser_whenLoggedIn");

        mockMvc.perform(get("/mypage")
                        .with(user(principal)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/mypage"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void nickname_shouldRedirectWithMessage_whenSuccess() throws Exception {
        log.info("테스트시작 nickname_shouldRedirectWithMessage_whenSuccess");

        mockMvc.perform(post("/mypage/nickname")
                        .with(user(principal))
                        .with(csrf())
                        .param("nickname", "New_Nickname"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    public void nickname_shouldRedirectWithError_whenDuplicateNickname() throws Exception {
        log.info("테스트시작 nickname_shouldRedirectWithError_whenDuplicateNickname");

        UserEntity other = UserEntity.builder()
                .username("OTHER_USER")
                .password(passwordEncoder.encode("1234"))
                .name("OTHER_NAME")
                .nickname("DUP_NICKNAME")
                .email("other@test.com")
                .build();
        userRepository.save(other);

        mockMvc.perform(post("/mypage/nickname")
                        .with(user(principal))
                        .with(csrf())
                        .param("nickname", "DUP_NICKNAME"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    public void password_shouldRedirectWithMessage_whenSuccess() throws Exception {
        log.info("테스트시작 password_shouldRedirectWithMessage_whenSuccess");

        mockMvc.perform(post("/mypage/password")
                        .with(user(principal))
                        .with(csrf())
                        .param("currentPassword", "1234")
                        .param("newPassword", "newpass123!")
                        .param("newPasswordConfirm", "newpass123!"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    public void password_shouldRedirectWithError_whenCurrentPasswordWrong() throws Exception {
        log.info("테스트시작 password_shouldRedirectWithError_whenCurrentPasswordWrong");

        mockMvc.perform(post("/mypage/password")
                        .with(user(principal))
                        .with(csrf())
                        .param("currentPassword", "wrong")
                        .param("newPassword", "newpass123!")
                        .param("newPasswordConfirm", "newpass123!"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    public void myPosts_shouldReturnPage_whenLoggedIn() throws Exception {
        log.info("테스트시작 myPosts_shouldReturnPage_whenLoggedIn");

        mockMvc.perform(get("/mypage/posts")
                        .with(user(principal)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/mypage-posts"))
                .andExpect(model().attributeExists("postPage"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attributeExists("size"));
    }

    @Test
    public void myComments_shouldReturnPage_whenLoggedIn() throws Exception {
        log.info("테스트시작 myComments_shouldReturnPage_whenLoggedIn");

        mockMvc.perform(get("/mypage/comments")
                        .with(user(principal)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/mypage-comments"))
                .andExpect(model().attributeExists("commentPage"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attributeExists("size"));
    }

    @Test
    public void removeAccount_shouldRedirectHome_whenSuccess() throws Exception {
        log.info("테스트시작 removeAccount_shouldRedirectHome_whenSuccess");

        // 탈퇴 전에는 사용자 존재
        assertTrue(userRepository.findById(userId).isPresent());
        log.info("탈퇴 전 사용자 존재 확인 완료 - userId={}", userId);

        mockMvc.perform(post("/mypage/removeAccount")
                        .with(user(principal))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));

        // 탈퇴 후에는 사용자 삭제됨(= 조회 안 됨)
        assertTrue(userRepository.findById(userId).isEmpty());
        log.info("탈퇴 후 사용자 삭제 확인 완료 - userId={}", userId);

        log.info("테스트종료 removeAccount_shouldRedirectHome_whenSuccess");
    }
}
