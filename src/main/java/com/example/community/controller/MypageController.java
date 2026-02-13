package com.example.community.controller;

import com.example.community.domain.user.UserDto;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.CommentService;
import com.example.community.service.PostService;
import com.example.community.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j
public class MypageController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    /**
     * 마이페이지 조회
     * - 로그인한 사용자의 정보를 조회하여 화면에 전달
     * - 비로그인 사용자는 접근 불가
     */
    @GetMapping("")
    public String mypage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        log.info("마이페이지 접근 시도");

        if (userDetails == null) {
            model.addAttribute("error", "로그인이 필요한 서비스입니다");
            return "mypage/mypage";
        }

        String username = userDetails.getUsername();
        log.info("마이페이지 조회 요청: username={}", username);

        UserDto user = userService.read(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        model.addAttribute("user", user);
        log.info("마이페이지 조회 성공: username={}", username);

        return "mypage/mypage";
    }
    /*
     * 닉네임 변경
     * - 로그인한 사용자의 닉네임 수정
     * - 닉네임 중복 시 예외 발생
     */
    @PostMapping("/nickname")
    public String updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String nickname,
                                 RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getUser().getId();
        log.info("닉네임 변경 요청: userId={}, newNickname={}", userId, nickname);

        try {
            userService.updateNickname(userId, nickname);
            log.info("닉네임 변경 성공: userId={}", userId);
            redirectAttributes.addFlashAttribute("message", "닉네임이 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("닉네임 변경 실패: userId={}, reason={}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mypage";
    }

    /**
     * 비밀번호 변경
     * - 현재 비밀번호 일치 여부 확인
     * - 새 비밀번호 형식 및 확인 일치 검사
     * - 성공 시 암호화 후 저장
     */
    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String newPasswordConfirm,
                                 RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getUser().getId();

        log.info("비밀번호 변경 요청: userId={}", userId);

        try {
            userService.changePassword(userId, currentPassword, newPassword, newPasswordConfirm);
            log.info("비밀번호 변경 성공: userId={}", userId);
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 변경 실패: userId={}, reason={}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mypage";
    }

    /**
     * 내가 작성한 게시글 목록
     * - page, size로 페이징
     * - 최신순 정렬
     * - 게시글 클릭 시 게시글 상세로 이동
     */
    @GetMapping("/posts")
    public String myPosts(@AuthenticationPrincipal CustomUserDetails userDetails,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {

        Long userId = userDetails.getUser().getId();
        log.info("내 게시글 목록 조회: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var postPage = postService.getMyPosts(userId, pageable);

        model.addAttribute("postPage", postPage);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        log.info("내 게시글 목록 조회 성공: userId={}, totalElements={}", userId, postPage.getTotalElements());

        return "mypage/mypage-posts";
    }

    /**
     * 내가 작성한 댓글 목록
     * - page, size로 페이징
     * - 최신순 정렬
     * - 댓글 클릭 시 해당 게시글로 이동
     */
    @GetMapping("/comments")
    public String myComments(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model) {

        Long userId = userDetails.getUser().getId();
        log.info("내 댓글 목록 조회: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var commentPage = commentService.getMyComments(userId, pageable);

        model.addAttribute("commentPage", commentPage);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        log.info("내 댓글 목록 조회 성공: userId={}, totalElements={}", userId, commentPage.getTotalElements());

        return "mypage/mypage-comments";
    }

    @PostMapping("/removeAccount")
    public String removeAccount(@AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request,
                                HttpServletResponse response) {

        if (userDetails == null) {
            log.warn("비로그인 사용자의 탈퇴 시도");
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        Long userId = userDetails.getUser().getId();
        log.info("회원 탈퇴 요청(계정 삭제): userId={}", userId);

        try {
            boolean deleted = userService.delete(userId);

            if (!deleted) {
                log.warn("회원 탈퇴 실패: userId={}, reason=not found", userId);
                redirectAttributes.addFlashAttribute("error", "사용자를 찾을 수 없습니다.");
                return "redirect:/mypage";
            }

            // 로그아웃 처리
            new SecurityContextLogoutHandler().logout(request, response, null);

            log.info("회원 탈퇴 성공: userId={}", userId);
            redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
            return "redirect:/";
        } catch (Exception e) {
            log.error("회원 탈퇴 중 예외 발생: userId={}, reason={}", userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "회원 탈퇴 처리 중 오류가 발생했습니다.");
            return "redirect:/mypage";
        }

    }
}
