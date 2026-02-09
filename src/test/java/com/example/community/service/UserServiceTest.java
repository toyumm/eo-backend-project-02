package com.example.community.service;

import com.example.community.domain.user.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    // Service Bean 존재 확인
    @Test
    public void testExists(){
        assertNotNull(userService);
        log.info("userService = {}", userService);
    }

    // 회원가입 테스트
    @Test
    public void testCreate(){
        UserDto userDto = UserDto.builder()
                .username("junit_test")
                .password("password123!")
                .name("JUnit Test")
                .nickname("tester")
                .email("junit@test.com")
                .build();

        log.info("Before create: userDto = {}", userDto);

        userService.create(userDto);

        log.info("After create: userDto = {}", userDto);

        assertNotNull(userDto.getId());
    }

    // 조회 테스트
    /*
    회원번호로 유저 조회 테스트
     */
    @Test
    public void testReadById(){

        UserDto createDto = UserDto.builder()
                .username("junit_test")
                .password("password123!")
                .name("JUnit Test")
                .nickname("tester")
                .email("junit@test.com")
                .build();

        userService.create(createDto);

        Long id = createDto.getId();

        log.info("Created ID = {}", id);

        Optional<UserDto> userDto = userService.read(id);

        assertTrue(userDto.isPresent());

        log.info("Found UserDto = {}", userDto.get());

    }
    /*
    유저아이디로 조회 테스트
     */
    @Test
    public void testReadByUsername(){
        UserDto createDto = UserDto.builder()
                .username("junit_test")
                .password("password123!")
                .name("JUnit Test")
                .nickname("tester")
                .email("junit@test.com")
                .build();

        userService.create(createDto);

        log.info("Created username = {}", createDto.getUsername());

        String username = "junit_test";

        Optional<UserDto> userDto = userService.read(username);

        assertTrue(userDto.isPresent());

        log.info("Found UserDto = {}", userDto.get());
    }

    /*
    정보 수정 테스트
     */
    @Test
    public void testUpdate() {

        UserDto createDto = UserDto.builder()
                .username("update_test")
                .password("password123!")
                .name("Update Test")
                .nickname("original_nickname")
                .email("original@test.com")
                .build();

        userService.create(createDto);
        Long id = createDto.getId();

        log.info("Created user: id={}, nickname={}, email={}",
                id, createDto.getNickname(), createDto.getEmail());


        Optional<UserDto> userDto = userService.read(id);
        assertTrue(userDto.isPresent());

        UserDto dto = userDto.get();


        dto.setNickname("updated_nickname");
        dto.setEmail("updated@test.com");

        log.info("Updating: nickname={}, email={}",
                dto.getNickname(), dto.getEmail());

        Optional<UserDto> updatedDto = userService.update(dto);


        assertTrue(updatedDto.isPresent());
        assertThat(updatedDto.get().getNickname()).isEqualTo("updated_nickname");
        assertThat(updatedDto.get().getEmail()).isEqualTo("updated@test.com");

        log.info("Updated user: {}", updatedDto.get());
    }

    // 삭제 test
    @Test
    public void testDelete() {

        UserDto createDto = UserDto.builder()
                .username("delete_test")
                .password("password123!")
                .name("Delete Test")
                .nickname("delete_user")
                .email("delete@test.com")
                .build();

        userService.create(createDto);
        Long id = createDto.getId();
        log.info("Created user for deletion: id={}", id);

        //  삭제
        boolean result = userService.delete(id);

        assertTrue(result);

        log.info("Delete result = {}", result);


        Optional<UserDto> deletedUser = userService.read(id);

        assertFalse(deletedUser.isPresent());

        log.info("User completely deleted: present={}", deletedUser.isPresent());
    }


    // 닉네임 중복 테스트
    @Test
    public void testCreateFail_DuplicateUsername() {
        //  먼저 생성
        UserDto firstUser = UserDto.builder()
                .username("duplicate_username")
                .password("password123!")
                .name("First User")
                .nickname("first_nick")
                .email("first@test.com")
                .build();

        userService.create(firstUser);

        log.info("First user created: username={}", firstUser.getUsername());

        //  같은 username으로 두 번째 생성 시도
        UserDto secondUser = UserDto.builder()
                .username("duplicate_username")  // 중복!
                .password("password123!")
                .name("Second User")
                .nickname("second_nick")
                .email("second@test.com")
                .build();

        // 예외 발생 확인
        assertThrows(IllegalArgumentException.class, () -> {
            userService.create(secondUser);
        });

        // 예외 발생 메시지 출력
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() ->{
           userService.create(secondUser);
        });

        log.info("Exception message = {}", exception.getMessage());

        log.info("Duplicate username test passed");
    }

    @Test
    public void testCreateFail_DuplicateNickname() {
        //  먼저 생성
        UserDto firstUser = UserDto.builder()
                .username("user_for_nick_test")
                .password("password123!")
                .name("First User")
                .nickname("duplicate_nickname")
                .email("nick1@test.com")
                .build();

        userService.create(firstUser);

        log.info("First user created: nickname={}", firstUser.getNickname());

        //  같은 nickname으로 두 번째 생성 시도
        UserDto secondUser = UserDto.builder()
                .username("another_user")
                .password("password123!")
                .name("Second User")
                .nickname("duplicate_nickname")  // 중복!
                .email("nick2@test.com")
                .build();

        //  예외 발생 확인
        assertThrows(IllegalArgumentException.class, () -> {
            userService.create(secondUser);
        });

        log.info("Duplicate nickname test passed");
    }

    @Test
    public void testCreateFail_DuplicateEmail() {
        //  먼저 생성
        UserDto firstUser = UserDto.builder()
                .username("user_for_email_test")
                .password("password123!")
                .name("First User")
                .nickname("email_nick1")
                .email("duplicate@test.com")
                .build();

        userService.create(firstUser);

        log.info("First user created: email={}", firstUser.getEmail());

        //  같은 email로 두 번째 생성 시도
        UserDto secondUser = UserDto.builder()
                .username("another_user_email")
                .password("password123!")
                .name("Second User")
                .nickname("email_nick2")
                .email("duplicate@test.com")  // 중복!
                .build();

        //  예외 발생 확인
        assertThrows(IllegalArgumentException.class, () -> {
            userService.create(secondUser);
        });

        log.info("Duplicate email test passed");
    }
}