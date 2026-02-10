/**
 * 메인 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {

    // 검색 폼 초기화
    initSearchForm();

    // 페이지 로드 시 검색어 하이라이트
    highlightSearchKeyword();

    // 게시글 상세보기 기능 초기화
    initPostDetail();

});

/**
 * 검색 폼 초기화
 */
function initSearchForm() {
    const searchForm = document.getElementById('search-section');

    if (!searchForm) return;

    // 검색어 입력 시 엔터키 처리
    const searchInput = searchForm.querySelector('input[name="keyword"]');

    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchForm.submit();
            }
        });
    }
}

/**
 * 검색어 하이라이트 표시
 */
function highlightSearchKeyword() {
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('keyword');
    const searchType = urlParams.get('searchType');

    if (!keyword) return;

    // 제목에 검색어 하이라이트
    if (searchType === 'title' || searchType === 'titleContent') {
        highlightInElements('.title a', keyword);
    }
}

/**
 * 특정 요소들 내에서 키워드 하이라이트
 */
function highlightInElements(selector, keyword) {
    const elements = document.querySelectorAll(selector);

    elements.forEach(element => {
        const text = element.textContent;
        const regex = new RegExp(`(${keyword})`, 'gi');

        if (regex.test(text)) {
            element.innerHTML = text.replace(regex, '<mark style="background-color: #ffeb3b; padding: 2px 4px;">$1</mark>');
        }
    });
}

/**
 * 로그아웃 확인
 */
function confirmLogout() {
    return confirm('로그아웃 하시겠습니까?');
}

/**
 * 게시글 상세보기 기능 초기화
 */
function initPostDetail() {
    const postTable = document.getElementById('post-table');
    const mainContent = document.getElementById('main-content');

    if (!postTable) return;

    // 게시글 제목 클릭 이벤트
    postTable.addEventListener('click', function(e) {
        const target = e.target;

        // 제목 링크 클릭 시
        if (target.classList.contains('post-title-link')) {
            e.preventDefault();

            const postId = target.dataset.postId;
            const postTitle = target.dataset.postTitle;
            const postContent = target.dataset.postContent;
            const postWriter = target.dataset.postWriter;
            const postDate = target.dataset.postDate;
            const postViews = target.dataset.postViews;

            showPostDetail(postId, postTitle, postContent, postWriter, postDate, postViews);
        }
    });
}

/**
 * 게시글 상세보기 표시
 */
function showPostDetail(id, title, content, writer, date, views) {
    const mainContent = document.getElementById('main-content');

    // 기존 내용 백업
    if (!mainContent.dataset.originalContent) {
        mainContent.dataset.originalContent = mainContent.innerHTML;
    }

    // 상세보기 HTML 생성
    const detailHTML = `
        <div id="post-detail">
            <div style="margin-bottom: 20px;">
                <button onclick="goBackToList()" style="padding: 8px 15px; 
                background-color: #666; color: white; border: none; border-radius: 4px; cursor: pointer;">
                    ← 목록으로
                </button>
            </div>
            
            <div style="background: white; padding: 30px; border-radius: 8px; border: 1px solid #e0e0e0;">
                <h2 style="font-size: 24px; margin-bottom: 20px; color: #333;">${title}</h2>
                
                <div style="border-top: 2px solid #333; border-bottom: 1px solid #ddd; padding: 15px 0;
                 margin-bottom: 30px; display: flex; gap: 20px; font-size: 14px; color: #666;">
                    <span><strong>작성자:</strong> ${writer}</span>
                    <span><strong>작성일:</strong> ${date}</span>
                    <span><strong>조회수:</strong> ${views}</span>
                </div>
                
                <div style="line-height: 1.8; font-size: 15px; color: #333; white-space: pre-wrap; min-height: 300px;">
                    ${content}
                </div>
                
                <div style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd;">
                    <button onclick="goBackToList()" style="padding: 10px 20px; background-color: #667eea; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 14px;">
                        목록으로 돌아가기
                    </button>
                </div>
            </div>
        </div>
    `;

    // 상세보기로 교체
    mainContent.innerHTML = detailHTML;

    // 상단으로 스크롤
    mainContent.scrollIntoView({ behavior: 'smooth' });
}

/**
 * 목록으로 돌아가기
 */
function goBackToList() {
    const mainContent = document.getElementById('main-content');

    if (mainContent.dataset.originalContent) {
        mainContent.innerHTML = mainContent.dataset.originalContent;
        delete mainContent.dataset.originalContent;

        // 이벤트 리스너 재초기화
        initPostDetail();
    }
}