package com.example.community.persistence;

import com.example.community.domain.comment.CommentEntity;
import com.example.community.domain.post.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    /**
     * 특정 게시물의 댓글 목록을 조회
     *
     * @param postEntity 확인할 게시물
     * @return 댓글 목록
     */

    List<CommentEntity> findByPostEntity(PostEntity postEntity);

    /**
     * 특정 게시물의 댓글 목록을 조회
     *
     * @param postId 확인할 게시물의 ID
     * @return 댓글 목록
     */

    List<CommentEntity> findByPostEntityId(Long postId);

    /**
     * 마이페이지에서 내 댓글 조회
     * @param userId 확인할 유저의 ID
     * @param pageable
     * @return 댓글이 달린 페이지
     */
    Page<CommentEntity> findByUserId(Long userId, Pageable pageable);
}