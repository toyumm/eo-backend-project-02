package com.example.community.service;

import com.example.community.domain.user.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    /*
    * 전제 사용자 목록 조회 (페이징)
     */
    Page<UserDto> getAllUsers(Pageable pageable, String role, String keyword);

    /*
    *사용자 권한 변경
     */
    UserDto getUserById(Long userid);

    /*
    * 사용자 권한 변경
     */
    void changeUserRole(Long userid, String newRole, String reason, Long adminId);

    /*
    *사용자 정지
     */
    void banUser(Long userId, String reason, Integer duration, String note, Long adminId);

    /*
    * 사용자 활성화
     */
    void activateUser(Long userId);

    /*
    *사용자 비활성화
     */
    void deactivateUser(Long userId);
}
