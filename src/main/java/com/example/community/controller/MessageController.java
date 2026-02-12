package com.example.community.controller;

import com.example.community.domain.message.MessageDto;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    /**
     * 전체 쪽지함 화면 조회
     * 모든 메뉴 클릭 시 에러 방지를 위해 여러 경로를 수용
     */
    @GetMapping({"/all", "/received", "/sent", "/trash", "/write"})
    public String allMessagesPage(@RequestParam(defaultValue = "1") int page, Model model) {
        String username = getCurrentUsername();
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "id"));

        // 초기 화면은 기본적으로 'all' 리스트를 보여준다.
        Page<MessageDto> messagePage = messageService.getMessages("all", username, pageable);
        model.addAttribute("messages", messagePage);
        return "message/message";
    }

    /**
     * 목록 조회 (API)
     * 겹치는 경로 문제를 피하기 위해 /api/list로 변경
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Page<MessageDto>> list(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") int page) {
        String username = getCurrentUsername();
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(messageService.getMessages(type, username, pageable));
    }

    /**
     * 쪽지 발송 (API)
     * 주소창 입력(GET) 시 405 방지를 위해 경로를 /api/write로 분리
     */
    @PostMapping("/api/write")
    @ResponseBody
    public ResponseEntity<String> write(@Valid @RequestBody MessageDto messageDto) {
        String username = getCurrentUsername();
        messageService.sendMessage(messageDto, username);
        return ResponseEntity.ok("success");
    }

    /**
     * 상세 조회 (API)
     */
    @GetMapping("/api/read")
    @ResponseBody
    public ResponseEntity<MessageDto> read(@RequestParam Long id) {
        String username = getCurrentUsername();
        return ResponseEntity.of(messageService.getMessageDetail(id, username));
    }

    /**
     * 휴지통 이동 (API)
     */
    @PostMapping("/api/trash")
    @ResponseBody
    public ResponseEntity<Void> moveToTrash(@RequestParam Long id, @RequestParam String userType) {
        String username = getCurrentUsername();
        messageService.moveToTrash(id, username, userType);
        return ResponseEntity.ok().build();
    }

    /**
     * 복구 하기 (API)
     */
    @PostMapping("/api/restore")
    @ResponseBody
    public ResponseEntity<Void> restore(@RequestParam Long id, @RequestParam String userType) {
        String username = getCurrentUsername();
        messageService.restoreMessage(id, username, userType);
        return ResponseEntity.ok().build();
    }

    /**
     * 선택 삭제 (다중 삭제 API)
     */
    @PostMapping("/api/delete/bulk")
    @ResponseBody
    public ResponseEntity<Void> deleteBulk(@RequestBody Map<String, List<Long>> payload) {
        String username = getCurrentUsername();
        List<Long> ids = payload.get("ids");
        // messageService.deleteBulk(ids, username);
        return ResponseEntity.ok().build();
    }

    /**
     * 영구 삭제 (API)
     */
    @PostMapping("/api/delete")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestParam Long id, @RequestParam String userType) {
        String username = getCurrentUsername();
        messageService.permanentDelete(id, username, userType);
        return ResponseEntity.ok().build();
    }

    /**
     * 유효성 검사 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}