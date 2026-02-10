package com.example.community.persistence;

import com.example.community.domain.user.UserEntity;
import com.example.community.domain.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    //중복 체크
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    // 조회
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByNickname(String nickname);
    Optional<UserEntity> findByEmail(String email);

    // 비밀번호 찾기
    Optional<UserEntity> findByUsernameAndEmail(String username, String email);

    // 로그인

    Optional<UserEntity> findByUsernameAndActiveTrue(String username);


    /**
     * 역할별 사용자 조회 (페이징)
     */
    Page<UserEntity> findByRole(UserRole role, Pageable pageable);

    /**
     * username 또는 email로 검색 (페이징)
     */
    Page<UserEntity> findByUsernameContainingOrEmailContaining(
            String username, String email, Pageable pageable);

    /**
     * 역할 + username 또는 email로 검색 (페이징)
     */
    Page<UserEntity> findByRoleAndUsernameContainingOrRoleAndEmailContaining(
            UserRole role1, String username, UserRole role2, String email, Pageable pageable);

    /**
     * 역할별 사용자 수 카운트
     */
    long countByRole(UserRole role);

}
