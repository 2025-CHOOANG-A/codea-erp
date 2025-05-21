/**
 * 사용자에게 메시지를 표시하는 커스텀 알림 함수 (alert() 대체)
 * @param {string} message - 표시할 메시지
 * @param {string} type - 'success', 'error', 'info' 등의 메시지 타입
 */
function showMessage(message, type = 'info') {
    const messageBox = document.getElementById('messageBox');
    const messageText = document.getElementById('messageText');

    if (!messageBox || !messageText) {
        console.warn('Message box elements not found. Falling back to console log.');
        console.log(`Message (${type}): ${message}`);
        return;
    }

    messageText.textContent = message;
    messageBox.className = `alert alert-${type} alert-dismissible fade show`; // Bootstrap alert classes
    messageBox.style.display = 'block';

    // 메시지 박스 닫기 버튼 이벤트 리스너 추가 (Bootstrap 기본 동작)
    const closeButton = messageBox.querySelector('.btn-close');
    if (closeButton) {
        closeButton.onclick = () => {
            messageBox.style.display = 'none';
        };
    }

    // 5초 후 자동으로 메시지 숨기기
    setTimeout(() => {
        const bsAlert = bootstrap.Alert.getInstance(messageBox) || new bootstrap.Alert(messageBox);
        bsAlert.close();
    }, 5000);
}


/**
 * fetch API를 사용하여 PUT 요청을 보내는 함수
 * @param {string} url - PUT 요청을 보낼 URL
 * @param {object} data - 서버로 보낼 데이터 객체
 * @returns {Promise<object>} - 서버 응답 데이터를 포함하는 Promise
 */
async function sendPutRequest(url, data) {
    try {
        // CSRF 토큰 및 헤더 이름 가져오기 (Spring Security 사용 시)
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

        const headers = {
            'Content-Type': 'application/json',
        };
        if (csrfToken) {
            headers[csrfHeader] = csrfToken;
        }

        const response = await fetch(url, {
            method: 'PUT', // PUT 메소드 지정
            headers: headers,
            body: JSON.stringify(data) // JS 객체를 JSON 문자열로 변환
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: response.statusText }));
            throw new Error(`HTTP error! Status: ${response.status}, Message: ${errorData.message || 'Unknown error'}`);
        }

        // 응답 본문이 없을 경우를 대비하여 처리 (예: 204 No Content)
        const responseText = await response.text();
        return responseText ? JSON.parse(responseText) : {};

    } catch (error) {
        console.error('PUT 요청 실패:', error);
        showMessage('데이터 업데이트에 실패했습니다: ' + error.message, 'error');
        throw error;
    }
}

/**
 * fetch API를 사용하여 PATCH 요청을 보내는 함수 (부분 업데이트)
 * @param {string} url - PATCH 요청을 보낼 URL
 * @param {object} data - 서버로 보낼 부분 데이터 객체
 * @returns {Promise<object>} - 서버 응답 데이터를 포함하는 Promise
 */
async function sendPatchRequest(url, data) {
    try {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

        const headers = {
            'Content-Type': 'application/json',
        };
        if (csrfToken) {
            headers[csrfHeader] = csrfToken;
        }

        const response = await fetch(url, {
            method: 'PATCH', // PATCH 메소드 지정
            headers: headers,
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: response.statusText }));
            throw new Error(`HTTP error! Status: ${response.status}, Message: ${errorData.message || 'Unknown error'}`);
        }

        const responseText = await response.text();
        return responseText ? JSON.parse(responseText) : {};

    } catch (error) {
        console.error('PATCH 요청 실패:', error);
        showMessage('부분 데이터 업데이트에 실패했습니다: ' + error.message, 'error');
        throw error;
    }
}


// --- 페이지 로드 후 이벤트 리스너 설정 ---
document.addEventListener('DOMContentLoaded', () => {
    const clientEditForm = document.getElementById('clientEditForm');
    if (clientEditForm) {
        clientEditForm.addEventListener('submit', async (event) => {
            event.preventDefault(); // 기본 폼 제출 방지

            const bpId = clientEditForm.dataset.bpId; // HTML에서 bpId를 data-bp-id로 가져옴
            const url = `/client/${bpId}`; // PUT 요청 URL

            // 폼 데이터 수집
            const formData = new FormData(clientEditForm);
            const data = {};
            for (const [key, value] of formData.entries()) {
                data[key] = value;
            }

            try {
                const result = await sendPutRequest(url, data);
                console.log('거래처 정보 업데이트 성공:', result);
                showMessage('거래처 정보가 성공적으로 업데이트되었습니다.', 'success');
                // 성공 후 상세 페이지로 리다이렉트 또는 UI 업데이트
                // window.location.href = `/client/${bpId}`;
            } catch (error) {
                // 오류는 sendPutRequest에서 이미 처리됨
            }
        });
    }

    // 담당자 수정 모달 폼 제출 처리
    const editContactForm = document.getElementById('editContactForm');
    if (editContactForm) {
        editContactForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const bpId = editContactForm.dataset.bpId; // HTML에서 bpId를 data-bp-id로 가져옴
            const bcId = document.getElementById('editBcId').value;
            const url = `/client/${bpId}/contact/${bcId}`; // PATCH 요청 URL

            const formData = new FormData(editContactForm);
            const data = {};
            for (const [key, value] of formData.entries()) {
                data[key] = value;
            }
            // bcId는 URL에 포함되므로 body에서 제거 (선택 사항이지만 깔끔함)
            delete data.bcId;

            try {
                const result = await sendPatchRequest(url, data);
                console.log('담당자 정보 업데이트 성공:', result);
                showMessage('담당자 정보가 성공적으로 업데이트되었습니다.', 'success');
                // 모달 닫기
                const editContactModal = bootstrap.Modal.getInstance(document.getElementById('editContactModal'));
                if (editContactModal) editContactModal.hide();
                // 페이지 새로고침하여 업데이트된 담당자 목록 반영
                location.reload();
            } catch (error) {
                // 오류는 sendPatchRequest에서 이미 처리됨
            }
        });
    }

    // 담당자 추가 모달 폼 제출 처리
    const addContactForm = document.getElementById('addContactForm');
    if (addContactForm) {
        addContactForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const bpId = addContactForm.dataset.bpId;
            const url = `/client/${bpId}/contact`; // POST 요청 URL

            const formData = new FormData(addContactForm);
            const data = {};
            for (const [key, value] of formData.entries()) {
                data[key] = value;
            }

            try {
                const result = await fetch(url, { // POST는 sendPut/Patch와 유사하지만, 생성 의미가 강함
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
                    },
                    body: JSON.stringify(data)
                });

                if (!result.ok) {
                    const errorData = await result.json().catch(() => ({ message: result.statusText }));
                    throw new Error(`HTTP error! Status: ${result.status}, Message: ${errorData.message || 'Unknown error'}`);
                }

                console.log('담당자 추가 성공:', await result.json());
                showMessage('새 담당자가 성공적으로 추가되었습니다.', 'success');
                const addContactModal = bootstrap.Modal.getInstance(document.getElementById('addContactModal'));
                if (addContactModal) addContactModal.hide();
                location.reload(); // 페이지 새로고침
            } catch (error) {
                console.error('담당자 추가 실패:', error);
                showMessage('담당자 추가에 실패했습니다: ' + error.message, 'error');
            }
        });
    }
});

// 담당자 수정 모달 열기 함수 (기존과 동일)
function openEditContactModal(button) {
    const bcId = button.dataset.bcId;
    const bcName = button.dataset.bcName;
    const bcPosition = button.dataset.bcPosition;
    const tel = button.dataset.bcTel;
    const hp = button.dataset.bcHp;
    const email = button.dataset.bcEmail;
    const remark = button.dataset.bcRemark;

    document.getElementById('editBcId').value = bcId;
    document.getElementById('editBcName').value = bcName;
    document.getElementById('editBcPosition').value = bcPosition;
    document.getElementById('editContactTel').value = tel;
    document.getElementById('editContactHp').value = hp;
    document.getElementById('editContactEmail').value = email;
    document.getElementById('editContactRemark').value = remark;

    // 폼의 action URL 설정 (PATCH 요청을 위해)
    const bpId = button.closest('main').querySelector('[data-bp-id]').dataset.bpId; // 상위 요소에서 bpId 찾기
    document.getElementById('editContactForm').action = `/client/${bpId}/contact/${bcId}`;
    document.getElementById('editContactForm').dataset.bpId = bpId; // bpId를 폼에도 저장하여 submit 리스너에서 사용

    const editContactModal = new bootstrap.Modal(document.getElementById('editContactModal'));
    editContactModal.show();
}

// 담당자 삭제 함수 (기존과 동일)
async function deleteContact(bcId) {
    if (confirm('정말로 이 담당자를 삭제하시겠습니까?')) {
        const bpId = document.querySelector('[data-bp-id]').dataset.bpId; // HTML에서 bpId를 data-bp-id로 가져옴
        const url = `/client/${bpId}/contact/${bcId}`;

        try {
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

            const headers = {};
            if (csrfToken) {
                headers[csrfHeader] = csrfToken;
            }

            const response = await fetch(url, {
                method: 'DELETE',
                headers: headers,
            });

            if (response.ok) {
                showMessage('담당자가 성공적으로 삭제되었습니다.', 'success');
                location.reload(); // 페이지 새로고침
            } else {
                const errorData = await response.json().catch(() => ({ message: response.statusText }));
                throw new Error(`HTTP error! Status: ${response.status}, Message: ${errorData.message || 'Unknown error'}`);
            }
        } catch (error) {
            console.error('담당자 삭제 실패:', error);
            showMessage('담당자 삭제에 실패했습니다: ' + error.message, 'error');
        }
    }
}