package com.example.community.service;

import com.example.community.domain.post.PostDto;
import com.example.community.domain.post.PostEntity;
import com.example.community.domain.user.UserEntity;
import com.example.community.domain.user.UserRole;
import com.example.community.persistence.PostRepository;
import com.example.community.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private String getNickname(Long userId) {
        return userRepository.findById(userId)
                .map(UserEntity::getNickname)
                .orElse("unknown");
    }

    private boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    /**
     * 게시글 생성
     * 게시글 ID 반환
     */
    @Override
    public Long create(Long boardId, PostDto postDto, Long userId) {
        log.info("CREATE: boardId={}, postDto={}, userId={}", boardId, postDto, userId);

        PostEntity postEntity = PostEntity.builder()
                .boardId(boardId)
                .userId(userId)
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .postType(postDto.getPostType())
                .fixed(postDto.getFixed())
                .viewCount(0)
                .commentsCount(0)
                .likesCount(0)
                .build();

        PostEntity savedEntity = postRepository.save(postEntity);
        log.info("CREATE: saved={}", savedEntity);

        postDto.setId(savedEntity.getId());
        return savedEntity.getId();
    }

    /**
     * 게시글 단건 조회
     * 작성자 닉네임 포함 DTO 반환
     */
    @Override
    @Transactional
    public PostDto read(Long id) {
        log.info("READ: id = {}", id);

        // 게시글 조회
        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));

        // 조회수 증가
        postEntity.increaseViewCount();

        String nickname = "unknown";

        try {
            // 작성자 닉네임 조회
            nickname = userRepository.findById(postEntity.getUserId())
                    .map(user -> user.getNickname())
                    .orElse("unknown");
        } catch (Exception e) {
            log.warn("READ: failed to load writer nickname. userId={}, reason={}",
                    postEntity.getUserId(), e.getMessage());
        }

        return PostDto.from(postEntity, nickname);
    }

    /**
     * 게시글 수정
     * 작성자 본인만 수정가능
     * 권한 불일치 시 false 반환
     */
    @Override
    @Transactional
    public boolean update(PostDto postDto, Long userId) {
        log.info("UPDATE: postId={}, userId={}, title={}", postDto.getId(), userId, postDto.getTitle());

        return postRepository.findById(postDto.getId()).map(postEntity -> {
                    // 작성자 검증
                    if (!postEntity.getUserId().equals(userId)) {
                        log.info("UPDATE DENIED: postId={}, requestUserId={}, ownerUserId={}",
                                postDto.getId(), userId, postEntity.getUserId());
                        return false;
                    }

                    if (postDto.getTitle() != null) postEntity.updateTitle(postDto.getTitle());
                    if (postDto.getContent() != null) postEntity.updateContent(postDto.getContent());
                    postEntity.updatePostType(postDto.getPostType());
                    postEntity.updateFixed(postDto.getFixed());

                    postRepository.save(postEntity);
                    return true;
                })
                .orElse(false);
    }


    /**
     *게시글 삭제
     *
     * 작성자 또는 관리자만 삭제 가능
     * 권한이 없으면 삭제 거부
     */
    @Override
    @Transactional
    public boolean delete(Long id, Long userId) {
        log.info("DELETE: id = {}, userId={}", id, userId);

        return postRepository.findById(id).map(postEntity -> {
            boolean isOwner = postEntity.getUserId().equals(userId);
            boolean admin = isAdmin(userId);

            if (!isOwner && !admin) {
                log.info("DELETE DENIED: postId={}, requestUserId={}, ownerUserId={}, isAdmin={}",
                        id, userId, postEntity.getUserId(), admin);
                return false;
            }

            postRepository.delete(postEntity);
            postRepository.flush();

            return true;
        }).orElse(false);
    }

    /**
     * 특정 게시판의 게시글 목록 조회(페이징)
     * 게시판 ID 기준 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getList(Long boardId, Pageable pageable) {
        log.info("GET LIST: boardId={}, pageable={}", boardId, pageable);

        return postRepository.findByBoardId(boardId, pageable)
                .map(this::convertToDto);
    }

    /**
     *전체 게시글 목록 조회
     * 관리자 화면 또는 메인 통합 목록에서 사용
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(Pageable pageable) {
        log.info("GET ALL POSTS: pageable={}", pageable);

        return postRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * 게시글 검색
     * 검색 타입에 따라 분기 처리
     * 키워드가 비어있으면 전체 목록 반환
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> searchPosts(String searchType, String keyword, Pageable pageable) {
        log.info("SEARCH POSTS: searchType={}, keyword={}, pageable={}", searchType, keyword, pageable);

        // 키워드가 비어있으면 전체 목록 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPosts(pageable);
        }

        Page<PostEntity> resultPage;

        switch (searchType) {
            case "title":
                resultPage = postRepository.searchByTitle(keyword, pageable);
                break;
            case "content":
                resultPage = postRepository.searchByContent(keyword, pageable);
                break;
            case "writer":
                resultPage = postRepository.searchByWriter(keyword, pageable);
                break;
            case "titleContent":
                resultPage = postRepository.searchByTitleOrContent(keyword, pageable);
                break;
            case "commentContent":
                resultPage = postRepository.searchByCommentContent(keyword, pageable);
                break;
            case "commentWriter":
                resultPage = postRepository.searchByCommentWriter(keyword, pageable);
                break;
            default:
                log.warn("SEARCH: invalid searchType={}, using titleContent", searchType);
                resultPage = postRepository.searchByTitleOrContent(keyword, pageable);
        }

        return resultPage.map(this::convertToDto);
    }

    /**
     * 인기 게시글 조회
     * 조회수 기준 상위 게시글 반환
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getPopularPosts(Pageable pageable) {
        log.info("GET POPULAR POSTS: pageable={}", pageable);

        return postRepository.findTopByViewCount(pageable)
                .map(this::convertToDto);
    }

    /**
     * PostEntity를 PostDto로 변환 (닉네임 포함)
     * @param postEntity 게시글 엔티티
     * @return 게시글 DTO
     */
    private PostDto convertToDto(PostEntity postEntity) {
        String nickname = userRepository.findById(postEntity.getUserId())
                .map(user -> user.getNickname())
                .orElse("unknown");

        return PostDto.from(postEntity, nickname);
    }

    /**
     * 특정 게시판 내에서 게시글 검색
     * 게시판 범위를 제한한 검색 기능
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> searchPostsInBoard(Long boardId, String searchType, String keyword, Pageable pageable) {
        log.info("SEARCH IN BOARD: boardId={}, searchType={}, keyword={}", boardId, searchType, keyword);

        Page<PostEntity> entities = postRepository.findByBoardIdAndSearchType(boardId, searchType, keyword, pageable);

        return entities.map(this::convertToDto);
    }

    /**
     * 로그인 사용자가 작성한 게시글 전체 목록 조회
     * @param userId 작성자(로그인 사용자) ID
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Override
    public Page<PostDto> getMyPosts(Long userId, Pageable pageable) {
        log.info("내 게시글 조회: userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        String nickname = userRepository.findById(userId)
                .map(UserEntity::getNickname)
                .orElse("알수없음");

        return postRepository.findByUserId(userId, pageable)
                .map(post -> PostDto.from(post, nickname));
    }

    /**
     * 마이페이지 대시보드용 최신 게시글 10개 조회
     * @param userId 작성자(로그인 사용자) ID
     * @return 최신 게시글 10개 목록
     */
    @Override
    @Transactional(readOnly = true)
        public List<PostDto> findTop10ByUserId(Long userId) {

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        return postRepository.findByUserId(userId, pageable)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}