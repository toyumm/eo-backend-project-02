package com.example.community.controller;

import com.example.community.domain.message.MessageDto;
import com.example.community.domain.message.MessageEntity;
import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.MessageRepository;
import com.example.community.persistence.UserRepository;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.MessageService;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@Transactional
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

    private final String MESSAGES_URI = "/messages";
    private final String API_URI = "/messages/api";

    @BeforeEach
    void setUp() {
        UserEntity sender = userRepository.findByUsername("sender").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .username("sender")
                        .password("password123!")
                        .name("발신자")
                        .nickname("보내는사람")
                        .email("sender@test.com")
                        .active(true)
                        .build())
        );

        userRepository.findByUsername("receiver").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .username("receiver")
                        .password("password456!")
                        .name("수신자")
                        .nickname("받는사람")
                        .email("receiver@test.com")
                        .active(true)
                        .build())
        );

        CustomUserDetails userDetails = new CustomUserDetails(sender);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("all - 전체 쪽지함 페이지 뷰 및 모델 검증")
    void testAllMessagesPage() throws Exception {
        log.info("testAllMessagesPage");
        // /all, /received, /sent 등 모든 GET 요청은 View를 반환
        mockMvc.perform(get(MESSAGES_URI + "/received"))
                .andExpect(status().isOk())
                .andExpect(view().name("message/message"))
                .andExpect(model().attributeExists("messages"));
        log.info(" testAllMessagesPage");
    }

    @Test
    @DisplayName("api/list - 받은 쪽지함 데이터 로드 확인")
    void testReadReceivedData() throws Exception {
        log.info("testReadReceivedData");
        // JSON 데이터를 받는 경로는 /api/list 임을 검증
        mockMvc.perform(get(API_URI + "/list")
                        .param("type", "received")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
        log.info("testReadReceivedData");
    }

    @Test
    @DisplayName("api/write - 쪽지 발송 성공 테스트")
    void testWrite() throws Exception {
        log.info("testWrite 시작");
        MessageDto messageDto = MessageDto.builder()
                .receiverUsername("receiver")
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        String json = objectMapper.writeValueAsString(messageDto);

        // POST경로 - /api/write로 변경
        mockMvc.perform(post(API_URI + "/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
        log.info("testWrite");
    }

    @Test
    @DisplayName("api/trash - 휴지통 이동 성공 테스트")
    void testMoveToTrash() throws Exception {
        log.info("testMoveToTrash");
        MessageDto messageDto = MessageDto.builder()
                .receiverUsername("receiver")
                .title("삭제될 쪽지")
                .content("내용")
                .build();
        messageService.sendMessage(messageDto, "sender");

        List<MessageEntity> messages = messageRepository.findAll();
        Long targetId = messages.get(messages.size() - 1).getId();

        //API 경로 /api/trash 로 변경
        mockMvc.perform(post(API_URI + "/trash")
                        .param("id", targetId.toString())
                        .param("userType", "sender"))
                .andDo(print())
                .andExpect(status().isOk());
        log.info("testMoveToTrash");
    }

    @Test
    @DisplayName("api/restore - 쪽지 복구 성공 테스트")
    void testRestore() throws Exception {
        log.info("testRestore");

        MessageDto messageDto = MessageDto.builder()
                .receiverUsername("receiver")
                .title("복구 테스트 쪽지")
                .content("내용")
                .build();
        messageService.sendMessage(messageDto, "sender");

        List<MessageEntity> messages = messageRepository.findAll();
        Long targetId = messages.get(messages.size() - 1).getId();

        // API 경로 /api/restore 로 변경
        mockMvc.perform(post(API_URI + "/restore")
                        .param("id", targetId.toString())
                        .param("userType", "sender"))
                .andExpect(status().isOk());

        log.info("testRestore");
    }
}