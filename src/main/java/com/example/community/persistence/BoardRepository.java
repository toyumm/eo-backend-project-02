package com.example.community.persistence;

import com.example.community.domain.board.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    /**
     * 게시판 이름으로 중복 여부 확인
     * @param title 확인할 게시판 제목
     * @return 존재 여부
     */
    boolean existsByTitle(String title);

    List<BoardEntity> findByCategory(String category);
}
