package com.example.community.controller;

import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.UserRepository;
import com.example.community.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MypageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private CustomUserDetails principal;

    // 테스트용 유저 저장
    @BeforeEach
    public void setUp() {
        UserEntity userEntity = UserEntity.builder()
                .username("TEST_USER")
                .password(passwordEncoder.encode("1234"))
                .name("TEST_NAME")
                .nickname("TEST_NICKNAME")
                .email("testuser@test.com")
                .build();

        userRepository.save(userEntity);
        principal = new CustomUserDetails(userEntity);
    }

    // 로그인된 사용자가 mypage 접근
    @Test
    public void mypage_shouldReturnUser_whenLoggedIn() throws Exception {
        // 로그인 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        // mypage 요청 후 검증
        mockMvc.perform(get("/mypage").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/mypage"))
                .andExpect(model().attributeExists("user"));
    }


    //로그인 된 사용자가 닉네임 변경
    @Test
    public void nickname_shouldRedirectWithMessage_whenSuccess() throws Exception {
        // 로그인 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        mockMvc.perform(post("/mypage/nickname")
                        .with(authentication(auth))
                        .param("nickname", "New_Nickname"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attributeExists("message"));
    }

    // 로그인 된 사용자가 닉네임 변경(중복이면 실패)
    @Test
    public void nickname_shouldRedirectWithError_whenDuplicateNickname() throws Exception {
        // 중복 닉네임을 가진 다른 유저를 미리 저장
        UserEntity other = UserEntity.builder()
                .username("OTHER_USER")
                .password(passwordEncoder.encode("1234"))
                .name("OTHER_NAME")
                .nickname("DUP_NICKNAME")
                .email("other@test.com")
                .build();
        userRepository.save(other);

        // 로그인 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        // DUP_NICKNAME 으로 변경 시도
        mockMvc.perform(post("/mypage/nickname")
                        .with(authentication(auth))
                        .param("nickname", "DUP_NICKNAME"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attributeExists("error"));
    }

    // 로그인 된 사용자가 비밀번호 변경
    @Test
    public void password_shouldRedirectWithMessage_whenSuccess() throws Exception {
        // 로그인 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        mockMvc.perform(post("/mypage/password")
                        .with(authentication(auth))
                        .param("currentPassword", "1234")
                        .param("newPassword", "newpass123!")
                        .param("newPasswordConfirm", "newpass123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attributeExists("message"));
    }

    // 로그인 된 사용자가 비밀번호 변경(현재 비밀번호 틀리면 실패)
    @Test
    public void password_shouldRedirectWithError_whenCurrentPasswordWrong() throws Exception {
        // 로그인 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        mockMvc.perform(post("/mypage/password")
                        .with(authentication(auth))
                        .param("currentPassword", "wrong")
                        .param("newPassword", "newpass123!")
                        .param("newPasswordConfirm", "newpass123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attributeExists("error"));
    }

    // 로그인 된 사용자가 내 게시글 목록 접근
    @Test
    public void myPosts_shouldReturnPage_whenLoggedIn() throws Exception {
        // 로그인 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        mockMvc.perform(get("/mypage/posts").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/mypage-posts"))
                .andExpect(model().attributeExists("postPage"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attributeExists("size"));
    }

    // 로그인 된 사용자가 내 댓글 목록 접근
    @Test
    public void myComments_shouldReturnPage_whenLoggedIn() throws Exception {
        // 로그인 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        mockMvc.perform(get("/mypage/comments").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/mypage-comments"))
                .andExpect(model().attributeExists("commentPage"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attributeExists("size"));
    }

    // 로그인 된 사용자가 회원 탈퇴(성공)
    @Test
    public void removeAccount_shouldRedirectHome_whenSuccess() throws Exception {
        // 로그인 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        // 탈퇴 전에는 사용자 존재
        Long userId = principal.getUser().getId();
        assertTrue(userRepository.findById(userId).isPresent());

        // 회원 탈퇴 요청
        mockMvc.perform(post("/mypage/removeAccount").with(authentication(auth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));

        // 탈퇴 후에는 사용자 삭제됨(= 조회 안 됨)
        assertTrue(userRepository.findById(userId).isEmpty());
    }
}
