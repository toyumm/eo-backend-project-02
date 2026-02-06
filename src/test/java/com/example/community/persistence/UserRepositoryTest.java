package com.example.community.persistence;

import com.example.community.domain.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // Repository Bean 확인
    @Test
    public void testExists(){
        assertNotNull(userRepository);
        log.info("UserRepository Test = {}" , userRepository);
    }

    // 저장 테스트
    @Test
    public void testSave(){
        UserEntity user = UserEntity.builder()
                .username("park_test")
                .password("password123!")
                .name("Repository Test")
                .nickname("park_tester")
                .email("park@naver.com")
                .build();
        log.info("Before save : user = {}", user);

        UserEntity savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertThat(savedUser.getUsername()).isEqualTo("park_test");
        log.info("After save : user = {}", savedUser);
    }

    //Helper method
    private UserEntity createUser(String username, String name, String nickname, String email) {
        return UserEntity.builder()
                .username(username)
                .password("password123!")
                .name(name)
                .nickname(nickname)
                .email(email)
                .build();
    }

    // 아이디 찾기 테스트
    @Test
    public void testFindById(){

        UserEntity user = createUser("park_test", "Find TEST", "Finder_id", "park@naver.com");
        UserEntity savedUser = userRepository.save(user);
        Long id = savedUser.getId();

        log.info("Saved user id = {}", id);

        // 조회
        Optional<UserEntity> found = userRepository.findById(id);

        assertTrue(found.isPresent());
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getUsername()).isEqualTo("park_test");

        log.info("Found user = {}", found.get());
    }

    // 유저 이름 찾기 테스트
    @Test
    public void testFindByUsername() {
        // 먼저 저장
        UserEntity user = createUser("find_username", "Username Test", "username_nick", "username@test.com");
        userRepository.save(user);

        log.info("Saved username = {}", user.getUsername());

        // username으로 조회
        Optional<UserEntity> found = userRepository.findByUsername("find_username");

        assertTrue(found.isPresent());
        assertThat(found.get().getUsername()).isEqualTo("find_username");

        log.info("Found by username = {}", found.get());
    }

    //별명으로 찾기
    @Test
    public void testFindByNickname() {
        // 먼저 저장
        UserEntity user = createUser("nick_user", "Nickname Test", "find_nickname", "nickname@test.com");
        userRepository.save(user);

        log.info("Saved nickname = {}", user.getNickname());

        // nickname으로 조회
        Optional<UserEntity> found = userRepository.findByNickname("find_nickname");

        assertTrue(found.isPresent());
        assertThat(found.get().getNickname()).isEqualTo("find_nickname");

        log.info("Found by nickname = {}", found.get());
    }

    // 이메일로 찾기
    @Test
    public void testFindByEmail() {
        // 먼저 저장
        UserEntity user = createUser("email_user", "Email Test", "email_nick", "find@email.com");
        userRepository.save(user);

        log.info("Saved email = {}", user.getEmail());

        // email로 조회
        Optional<UserEntity> found = userRepository.findByEmail("find@email.com");

        assertTrue(found.isPresent());
        assertThat(found.get().getEmail()).isEqualTo("find@email.com");

        log.info("Found by email = {}", found.get());
    }


}