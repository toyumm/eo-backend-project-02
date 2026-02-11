/**
 * 메인 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {

    // 검색 폼 초기화
    initSearchForm();

    // 페이지 로드 시 검색어 하이라이트
    highlightSearchKeyword();

    // 로그아웃 확인 이벤트
    initLogoutConfirm();

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
            element.innerHTML = text.replace(regex, '<mark class="search-highlight">$1</mark>');
        }
    });
}

/**
 * 로그아웃 확인 이벤트 초기화
 */
function initLogoutConfirm() {
    const logoutBtn = document.querySelector('.logout-btn');

    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            if (!confirm('로그아웃 하시겠습니까?')) {
                e.preventDefault();
                return false;
            }
        });
    }
}

/**
 * 로그아웃 확인 (전역 함수 - 하위 호환성)
 */
function confirmLogout() {
    return confirm('로그아웃 하시겠습니까?');
}