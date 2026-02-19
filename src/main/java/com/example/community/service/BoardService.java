package com.example.community.service;


import com.example.community.domain.board.BoardDto;
import java.util.List;
import java.util.Optional;

public interface BoardService {

    // 게시판 생성 (관리자용)
    void create(BoardDto boardDto);

    // 게시판 상세 조회
    Optional<BoardDto> read(Long id);

    // 게시판 정보 수정 (관리자용)
    Optional<BoardDto> update(BoardDto boardDto);

    // 게시판 삭제 (관리자용)
    boolean delete(Long id);

    // 전체 게시판 목록 조회
    List<BoardDto> getList();

    // 공지 게시판 목록 조회
    List<BoardDto> getNoticeBoardList();

    List<BoardDto> getByIds(List<Long> ids);
}
