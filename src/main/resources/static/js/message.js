document.addEventListener('DOMContentLoaded', () => {

    const messageList = document.getElementById('messageList');
    const checkAll = document.getElementById('checkAll');
    const deleteButton = document.getElementById('deleteButton');

    /**
     * 1. 실시간 안 읽은 쪽지 수 업데이트
     * unread-count 필요 시 추가
     */
    const updateUnreadCount = () => {
        fetch('/messages/api/unread-count') // 경로 규칙에 맞춰 /api 추가
            .then(response => response.ok ? response.json() : 0)
            .then(count => {
                const badge = document.querySelector('.unreadBadge');
                if (badge) badge.textContent = count;
            })
            .catch(error => console.error('Count update error:', error));
    };

    /**
     * 2. 쪽지 행 클릭 시 상세 페이지 이동 (이벤트 위임)
     */
    if (messageList) {
        messageList.addEventListener('click', (event) => {
            const currentRow = event.target.closest('.messageRow');

            if (!currentRow || event.target.type === 'checkbox' || event.target.tagName === 'BUTTON') {
                return;
            }

            const messageId = currentRow.dataset.id;
            if (messageId) {
                // /api/read 주소와 매칭
                // 현재는 API 호출 데이터를 가져옴
                // 만약 상세 화면 페이지로 이동하고 싶다면 별도의 뷰 매핑이 필요
                location.href = `/messages/api/read?id=${messageId}`;
            }
        });
    }

    /**
     * 3. 전체 선택 로직
     */
    if (checkAll) {
        checkAll.addEventListener('change', () => {
            const rowChecks = document.querySelectorAll('.rowCheck');
            rowChecks.forEach(checkbox => {
                checkbox.checked = checkAll.checked;
            });
        });
    }

    /**
     * 4. 선택 삭제 (Bulk Delete) 기능
     */
    if (deleteButton) {
        deleteButton.addEventListener('click', () => {
            const selectedChecks = document.querySelectorAll('.rowCheck:checked');

            if (selectedChecks.length === 0) {
                alert('삭제할 쪽지를 선택해주세요.');
                return;
            }

            if (confirm('선택한 쪽지를 삭제하시겠습니까?')) {
                const idsToDelete = Array.from(selectedChecks).map(cb =>
                    // 숫자로 변환하여 전송
                    Number(cb.closest('.messageRow').dataset.id)
                );

                fetch('/messages/api/delete/bulk', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ ids: idsToDelete })
                })
                    .then(response => {
                        if (response.ok) {
                            alert('삭제 처리되었습니다.');
                            location.reload();
                        } else {
                            alert('삭제 중 오류가 발생했습니다.');
                        }
                    })
                    .catch(error => console.error('Delete error:', error));
            }
        });
    }

    /**
     * 5. 개별 복구(Restore) 기능
     * 컨트롤러 @PostMapping("/api/restore")와 매칭
     */
    window.restoreMessage = (id, userType) => {
        if (confirm('이 쪽지를 복구하시겠습니까?')) {
            // URL 파라미터로 전달
            fetch(`/messages/api/restore?id=${id}&userType=${userType}`, {
                method: 'POST'
            })
                .then(response => {
                    if (response.ok) {
                        alert('복구되었습니다.');
                        location.href = '/messages/all';
                    }
                });
        }
    };
});