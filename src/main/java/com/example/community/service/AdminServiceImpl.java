package com.example.community.service;

import com.example.community.domain.user.UserDto;
import com.example.community.domain.user.UserEntity;
import com.example.community.domain.user.UserRole;
import com.example.community.persistence.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;


    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable, String role, String keyword) {
        log.info("Get all users - role: {}, keyword: {}", role, keyword);

        Page<UserEntity> userEntities;

        // 역할 필터링
        if (role != null && !role.equals("ALL")){
            UserRole userRole = UserRole.valueOf(role);

            // 키워드 검색 + 역할 필터
            if (keyword != null && !keyword.isEmpty()){
                userEntities = userRepository.findByRoleAndUsernameContainingOrRoleAndEmailContaining(
                        userRole, keyword, userRole ,keyword, pageable);
            } else{
                userEntities = userRepository.findByRole(userRole, pageable);
            }
        }else {
            if (keyword != null && !keyword.isEmpty()){
                userEntities = userRepository.findByUsernameContainingOrEmailContaining(
                        keyword, keyword, pageable);
            }else {
                userEntities = userRepository.findAll(pageable);
            }
        }
        return userEntities.map(UserDto::from);
    }

    // 사용자 상세 조회
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.info("Get user by id : {}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID:"+userId));
        return UserDto.from(userEntity);
    }


    // 사용자 권한 변경
    @Override
    public void changeUserRole(Long userId, String newRole, String reason, Long adminId) {
        log.info("Change user role - userId: {}, newRole: {}, reason:{}", userId, newRole, reason);

        //사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID:"+userId));

        //자기 자신 권한 변경 불가
        if (userId.equals(adminId)){
            throw new IllegalArgumentException("자신의 권한은 변경할 수 없습니다.");
        }

        //최소 1명의 ADMIN 유지 체크
        if (user.getRole() == UserRole.ADMIN && !newRole.equals("ADMIN")){
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1){
                throw new IllegalArgumentException("최소 1명의 관리자는 유지되어야 합니다.");
            }
        }

        // 사유 체크( 구현하면 사용할것
        if (reason == null || reason.trim().isEmpty()){
            throw new IllegalArgumentException("권한 변경 사유를 입력해주시요");
        }

        // 권한 변경
        UserRole userRole = UserRole.valueOf(newRole);
        user.setRole(userRole);
        userRepository.save(user);
        log.info("User role changed - userId: {}, oldRole: {}, newRole:{}",userId, user.getRole(), newRole);
    }

    // 사용자 정지
    @Override
    public void banUser(Long userId, String reason, Integer duration, String note, Long adminId) {
        log.info("Ban user - userId: {}, reason: {}, duration: {}", userId, reason, duration);

        //사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID:" + userId));

        // 사용자 비활성화
        user.setActive(false);
        userRepository.save(user);
        log.info("User banned - userId: {}, reason: {}", userId, reason);
    }

    @Override
    public void activateUser(Long userId) {
        log.info("Activate user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.setActive(true);
        userRepository.save(user);

        log.info("User activated - userId: {}", userId);
    }

    @Override
    public void deactivateUser(Long userId) {
        log.info("Deactivate user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.setActive(false);
        userRepository.save(user);

        log.info("User deactivated - userId: {}", userId);
    }
}
