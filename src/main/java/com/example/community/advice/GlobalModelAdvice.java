package com.example.community.advice;

import com.example.community.domain.board.BoardDto;
import com.example.community.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {
    private final BoardService boardService;

    @ModelAttribute("boardList")
    public List<BoardDto> boardList() {
        return boardService.getList();
    }

    @ModelAttribute("noticeBoardList")
    public List<BoardDto> noticeBoardList() {
        return boardService.getNoticeBoardList();
    }
}