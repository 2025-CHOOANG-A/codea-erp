// js/storage_write.js

// 커스텀 알림 모달 표시 함수 (alert() 대체)
function showCustomAlert(message) {
    const modalElement = document.getElementById('customAlertModal');
    if (!modalElement) {
        console.error("customAlertModal 요소를 찾을 수 없습니다.");
        return;
    }

    // 모달 내용 동적 생성
    modalElement.innerHTML = `
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="customAlertModalLabel">알림</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body" id="customAlertModalBody">
                    ${message}
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" data-bs-dismiss="modal">확인</button>
                </div>
            </div>
        </div>
    `;
    const customAlertModal = new bootstrap.Modal(modalElement);
    customAlertModal.show();
}

// 커스텀 확인 모달 표시 함수 (confirm() 대체)
function showCustomConfirm(message) {
    return new Promise((resolve) => {
        const modalElement = document.getElementById('customConfirmModal');
        if (!modalElement) {
            console.error("customConfirmModal 요소를 찾을 수 없습니다.");
            resolve(confirm(message)); // 요소가 없으면 기본 confirm 사용
            return;
        }

        // 모달 내용 동적 생성
        modalElement.innerHTML = `
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="customConfirmModalLabel">확인</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body" id="customConfirmModalBody">
                        ${message}
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" id="confirmCancelBtn">취소</button>
                        <button type="button" class="btn btn-primary" id="confirmOkBtn">확인</button>
                    </div>
                </div>
            </div>
        `;

        const customConfirmModal = new bootstrap.Modal(modalElement);
        const okBtn = document.getElementById('confirmOkBtn');
        const cancelBtn = document.getElementById('confirmCancelBtn');

        // 이벤트 리스너를 한 번만 등록하고, 모달이 닫힐 때 제거하는 함수
        const handleOk = () => {
            customConfirmModal.hide();
            resolve(true);
        };

        const handleCancel = () => {
            customConfirmModal.hide();
            resolve(false);
        };

        // 기존 리스너가 혹시 남아있을까봐 제거하고 다시 등록
        okBtn.removeEventListener('click', handleOk);
        cancelBtn.removeEventListener('click', handleCancel);

        okBtn.addEventListener('click', handleOk);
        cancelBtn.addEventListener('click', handleCancel);

        customConfirmModal.show();

        // 모달이 완전히 닫힐 때 이벤트 리스너를 정리하여 메모리 누수 방지
        modalElement.addEventListener('hidden.bs.modal', () => {
            okBtn.removeEventListener('click', handleOk);
            cancelBtn.removeEventListener('click', handleCancel);
        }, { once: true }); // 이 리스너는 한 번만 실행되도록
    });
}

// Daum 우편번호 검색 함수 (이전과 동일)
function searchPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = ''; // 주소 변수
            var extraAddr = ''; // 참고항목 변수

            // 사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 도로명 주소를 선택 했을경우
                addr = data.roadAddress;
            } else { // 지번 주소를 선택 했을경우(J)
                addr = data.jibunAddress;
            }

            // 사용자가 선택한 주소가 도로명 타입일때 참고항목을 조합한다.
            if(data.userSelectedType === 'R'){
                // 법정동명이 있을 경우 추가한다. (법정리는 제외)
                // 법정동의 경우 마지막 글자가 "동" "로" "가" 인 경우만.
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraAddr += data.bname;
                }
                // 건물명이 있고, 공동주택일 경우 추가한다.
                if(data.buildingName !== '' && data.apartment === 'Y'){
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
                if(extraAddr !== ''){
                    extraAddr = ' (' + extraAddr + ')';
                }
                // 조합된 참고항목을 상세주소 필드에 넣는다.
                document.getElementById('addressDetail').value = extraAddr;
            } else {
                document.getElementById('addressDetail').value = '';
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('postCode').value = data.zonecode;
            document.getElementById('address').value = addr;
            // 커서를 상세주소 필드로 이동한다.
            document.getElementById('addressDetail').focus();

            // 유효성 검사 피드백 제거
            document.getElementById('postCode').classList.remove('is-invalid');
            const postcodeFeedback = document.getElementById('postCode').nextElementSibling;
            if (postcodeFeedback && postcodeFeedback.classList.contains('invalid-feedback')) {
                 postcodeFeedback.style.display = 'none';
            }
            document.getElementById('address').classList.remove('is-invalid');
            const addressFeedback = document.getElementById('address').nextElementSibling;
            if (addressFeedback && addressFeedback.classList.contains('invalid-feedback')) {
                addressFeedback.style.display = 'none';
            }
        }
    }).open();
}

// 사원 검색 관련 전역 변수
let employeeSearchDebounceTimer;
const EMPLOYEE_SEARCH_DEBOUNCE_DELAY = 300; // 0.3초 디바운싱

// 사원 검색 모달 열기 함수
function openEmployeeSearchModal() {
    const modalElement = document.getElementById('employeeSearchModal');
    if (!modalElement) {
        console.error("employeeSearchModal 요소를 찾을 수 없습니다.");
        return;
    }

    // 모달 내용 동적 생성
    modalElement.innerHTML = `
        <div class="modal-dialog modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="employeeSearchModalLabel">사원 검색</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="input-group mb-3">
                        <input type="text" class="form-control" id="employeeSearchInput" placeholder="사원명 또는 사번 입력">
                        <button class="btn btn-outline-secondary" type="button" id="executeEmployeeSearchBtn">검색</button>
                    </div>
                    <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                        <table class="table table-hover table-sm">
                            <thead>
                                <tr>
                                    <th>사원명</th>
                                    <th>사번</th>
                                    <th>선택</th>
                                </tr>
                            </thead>
                            <tbody id="employeeSearchResults">
                                <tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    `;

    const employeeSearchModal = new bootstrap.Modal(modalElement);
    employeeSearchModal.show();

    // 모달이 열릴 때 검색 입력 필드에 포커스
    const employeeSearchInput = document.getElementById('employeeSearchInput');
    if (employeeSearchInput) {
        employeeSearchInput.focus();
        // DOMContentLoaded에서 등록된 이벤트 리스너를 다시 연결
        employeeSearchInput.removeEventListener('keyup', handleEmployeeSearchInputKeyup); // 기존 리스너 제거
        employeeSearchInput.addEventListener('keyup', handleEmployeeSearchInputKeyup); // 새 리스너 등록
    }

    // 모달 내 '검색' 버튼에도 이벤트 리스너 다시 연결
    const executeEmployeeSearchBtn = document.getElementById('executeEmployeeSearchBtn');
    if (executeEmployeeSearchBtn) {
        executeEmployeeSearchBtn.removeEventListener('click', handleExecuteEmployeeSearchClick); // 기존 리스너 제거
        executeEmployeeSearchBtn.addEventListener('click', handleExecuteEmployeeSearchClick); // 새 리스너 등록
    }

    // 사원 검색 결과 테이블에 이벤트 리스너 다시 연결
    const employeeSearchResultsTableBody = document.getElementById('employeeSearchResults');
    if (employeeSearchResultsTableBody) {
        employeeSearchResultsTableBody.removeEventListener('click', handleEmployeeSearchResultsClick); // 기존 리스너 제거
        employeeSearchResultsTableBody.addEventListener('click', handleEmployeeSearchResultsClick); // 새 리스너 등록
    }

    // 모달 열릴 때 이전 검색 결과 초기화 (이전 검색어가 있다면 초기 검색)
    const initialQuery = employeeSearchInput ? employeeSearchInput.value.trim() : '';
    if (initialQuery) {
        searchEmployees(initialQuery);
    } else {
        const employeeSearchResults = document.getElementById('employeeSearchResults');
        if (employeeSearchResults) {
            employeeSearchResults.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';
        }
    }
}

// 사원 검색 실행 함수 (API 호출)
async function searchEmployees(query) {
    const employeeSearchResultsBody = document.getElementById('employeeSearchResults');
    if(!employeeSearchResultsBody) return;

    employeeSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 중...</td></tr>';

    if (!query || query.length < 2) {
        employeeSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">두 글자 이상 입력해주세요.</td></tr>';
        return;
    }

    try {
        // ✨ 컨트롤러 @RequestMapping("/storage")와 @GetMapping("/api/employees/search")에 맞춰 경로 수정
        // URL을 다시 한번 정확하게 확인해주세요.
        const response = await fetch(`/storage/api/employees/search?query=${encodeURIComponent(query)}`);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('사원 검색 실패:', response.status, errorText);
            employeeSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">사원 검색에 실패했습니다. (HTTP ' + response.status + ')</td></tr>';
            return;
        }

        const employees = await response.json();

        employeeSearchResultsBody.innerHTML = '';

        if (employees && employees.length > 0) {
            employees.forEach(emp => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${emp.empName || ''}</td>
                    <td>${emp.empNo || ''}</td>
                    <td>
                        <button type="button" class="btn btn-sm btn-outline-success select-employee-btn"
                                data-emp-name="${emp.empName || ''}"
                                data-emp-no="${emp.empNo || ''}">선택</button>
                    </td>
                `;
                employeeSearchResultsBody.appendChild(row);
            });
        } else {
            employeeSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';
        }

    } catch (error) {
        console.error('사원 검색 중 오류 발생:', error);
        employeeSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">사원 검색 중 네트워크 오류가 발생했습니다.</td></tr>';
    }
}

// 사원 선택 처리 함수 (이전과 동일)
function selectEmployee(empName, empNo) {
    const empNameInput = document.getElementById('empName');
    const empNoInput = document.getElementById('empNo');
    const clearEmployeeBtn = document.getElementById('clearEmployeeBtn');

    if (empNameInput) {
        empNameInput.value = empName;
        empNameInput.classList.remove('is-invalid');
        const feedback = empNameInput.nextElementSibling;
        if (feedback && feedback.classList.contains('invalid-feedback')) {
            feedback.style.display = 'none';
        }
    }
    if (empNoInput) {
        empNoInput.value = empNo;
        empNoInput.classList.remove('is-invalid');
        const feedback = empNoInput.nextElementSibling;
        if (feedback && feedback.classList.contains('invalid-feedback')) {
            feedback.style.display = 'none';
        }
    }
    if (clearEmployeeBtn) {
        clearEmployeeBtn.style.display = 'inline-block';
    }

    const employeeSearchModal = bootstrap.Modal.getInstance(document.getElementById('employeeSearchModal'));
    if (employeeSearchModal) {
        employeeSearchModal.hide();
    }
}

// 담당자 초기화 (X 버튼 클릭 시) (이전과 동일)
function clearEmployeeSelection() {
    const empNameInput = document.getElementById('empName');
    const empNoInput = document.getElementById('empNo');
    const clearEmployeeBtn = document.getElementById('clearEmployeeBtn');

    if (empNameInput) {
        empNameInput.value = '';
        empNameInput.classList.remove('is-invalid');
        const feedback = empNameInput.nextElementSibling;
        if (feedback && feedback.classList.contains('invalid-feedback')) {
            feedback.style.display = 'none';
        }
    }
    if (empNoInput) {
        empNoInput.value = '';
        empNoInput.classList.remove('is-invalid');
        const feedback = empNoInput.nextElementSibling;
        if (feedback && feedback.classList.contains('invalid-feedback')) {
            feedback.style.display = 'none';
        }
    }
    if (clearEmployeeBtn) {
        clearEmployeeBtn.style.display = 'none';
    }
}

// 입력 필드 값에 따라 'X' 버튼 가시성 업데이트 (이전과 동일)
function updateClearButtonVisibility() {
    const empNameInput = document.getElementById('empName');
    const empNoInput = document.getElementById('empNo');
    const clearEmployeeBtn = document.getElementById('clearEmployeeBtn');

    if (empNameInput && empNoInput && clearEmployeeBtn) {
        if (empNameInput.value.trim() !== '' || empNoInput.value.trim() !== '') {
            clearEmployeeBtn.style.display = 'inline-block';
        } else {
            clearEmployeeBtn.style.display = 'none';
        }
    }
}

// 창고 등록 및 수정 처리 함수 (이전과 동일)
async function submitWarehouseForm() {
    const warehouseForm = document.getElementById("warehouseForm");
    const formMessageDiv = document.getElementById("formMessage");

    if (!warehouseForm) {
        console.error("Error: warehouseForm not found.");
        return;
    }

    formMessageDiv.innerHTML = '';

    const requiredFields = [
        "whCode", "whName", "postCode", "address", "addressDetail",
        "empName", "empNo", "tel",
    ];
    let valid = true;
    let firstInvalid = null;

    requiredFields.forEach((id) => {
        const el = document.getElementById(id);
        const feedback = el.nextElementSibling;

        if (el && !el.value.trim()) {
            el.classList.add("is-invalid");
            if (feedback && feedback.classList.contains('invalid-feedback')) {
                feedback.style.display = 'block';
            }
            if (!firstInvalid) firstInvalid = el;
            valid = false;
        } else if (el) {
            el.classList.remove("is-invalid");
            if (feedback && feedback.classList.contains('invalid-feedback')) {
                feedback.style.display = 'none';
            }
        }
    });

    if (!valid) {
        formMessageDiv.innerHTML = '<div class="alert alert-danger" role="alert">필수 입력 항목을 모두 입력해 주세요.</div>';
        if (firstInvalid) firstInvalid.focus();
        return;
    }

    const hiddenMethodField = warehouseForm.querySelector('input[name="_method"]');
    const whIdField = document.getElementById('whId');

    const isUpdateMode = hiddenMethodField && hiddenMethodField.value.toLowerCase() === 'put';
    const currentWhId = whIdField ? whIdField.value : null;

    let apiPath;
    let httpMethod;
    let confirmMessage;
    let successMessage;
    let failMessage;

    if (isUpdateMode && currentWhId) {
        apiPath = `/storage/${currentWhId}`;
        httpMethod = 'PUT';
        confirmMessage = "창고 정보를 수정하시겠습니까?";
        successMessage = "창고 정보가 성공적으로 수정되었습니다.";
        failMessage = "창고 수정에 실패했습니다.";
    } else {
        apiPath = '/storage';
        httpMethod = 'POST';
        confirmMessage = "창고 정보를 등록하시겠습니까?";
        successMessage = "창고 정보가 성공적으로 등록되었습니다.";
        failMessage = "창고 등록에 실패했습니다.";
    }

    const confirmed = await showCustomConfirm(confirmMessage);
    if (!confirmed) {
        return;
    }

    const formData = new FormData(warehouseForm);
    const jsonData = {};
    formData.forEach((value, key) => {
        jsonData[key] = value;
    });

    if (jsonData._method) {
        delete jsonData._method;
    }

    try {
        const response = await fetch(apiPath, {
            method: httpMethod,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(jsonData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("Server response was not OK:", response.status, errorText);
            let errorMessage = '서버 오류가 발생했습니다.';
            try {
                const errorData = JSON.parse(errorText);
                errorMessage = errorData.message || errorMessage;
            } catch (e) {
                errorMessage = `서버 응답 오류 (HTTP ${response.status}). 자세한 내용은 콘솔을 확인하세요.`;
            }

            formMessageDiv.innerHTML = `<div class="alert alert-danger" role="alert">${failMessage}: ${errorMessage}</div>`;
            showCustomAlert(`${failMessage}: ${errorMessage}`);
            return;
        }

        const result = await response.json();

        if (response.status === 200 || response.status === 201) {
            formMessageDiv.innerHTML = `<div class="alert alert-success" role="alert">${successMessage}</div>`;
            showCustomAlert(successMessage);
            setTimeout(() => {
                location.href = '/storage';
            }, 1000);
        } else {
            formMessageDiv.innerHTML = `<div class="alert alert-danger" role="alert">${failMessage}: ${result.message || '알 수 없는 오류'}</div>`;
            showCustomAlert(`${failMessage}: ${result.message || '알 수 없는 오류'}`);
        }

    } catch (error) {
        console.error("Error during warehouse operation:", error);
        formMessageDiv.innerHTML = '<div class="alert alert-danger" role="alert">네트워크 오류 또는 서버 응답 처리 중 문제가 발생했습니다.</div>';
        showCustomAlert("네트워크 오류가 발생했습니다.");
    }
}

// 이벤트 핸들러를 별도의 함수로 분리하여 동적으로 생성되는 요소에 재연결 가능하도록 함
const handleEmployeeSearchInputKeyup = (event) => {
    clearTimeout(employeeSearchDebounceTimer);
    const query = event.target.value.trim();
    employeeSearchDebounceTimer = setTimeout(() => {
        searchEmployees(query);
    }, EMPLOYEE_SEARCH_DEBOUNCE_DELAY);
};

const handleExecuteEmployeeSearchClick = () => {
    const employeeSearchInput = document.getElementById('employeeSearchInput');
    if (employeeSearchInput) {
        searchEmployees(employeeSearchInput.value.trim());
    }
};

const handleEmployeeSearchResultsClick = (event) => {
    const selectButton = event.target.closest('.select-employee-btn');
    if (selectButton) {
        const empName = selectButton.dataset.empName;
        const empNo = selectButton.dataset.empNo;
        selectEmployee(empName, empNo);
    }
};


// DOMContentLoaded 이벤트 리스너
document.addEventListener("DOMContentLoaded", () => {
    const warehouseForm = document.getElementById("warehouseForm");
    if (warehouseForm) {
        warehouseForm.addEventListener("submit", function(event) {
            event.preventDefault();
            submitWarehouseForm();
        });
    }

    const cancelBtn = document.getElementById("cancelBtn");
    if (cancelBtn) {
        cancelBtn.addEventListener("click", async () => { // async 추가
            const confirmed = await showCustomConfirm("작성을 취소하고 목록으로 돌아가시겠습니까?");
            if (confirmed) {
                location.href = '/storage';
            }
        });
    }

    const searchEmployeeBtn = document.getElementById('searchEmployeeBtn');
    if (searchEmployeeBtn) {
        searchEmployeeBtn.addEventListener('click', () => {
            openEmployeeSearchModal(); // 모달 오픈 및 내용 동적 생성
        });
    }

    // 사원 정보 초기화 (X 버튼) 관련 로직 (이전과 동일)
    const clearEmployeeBtn = document.getElementById('clearEmployeeBtn');
    const empNameInput = document.getElementById('empName');
    const empNoInput = document.getElementById('empNo');

    if (clearEmployeeBtn && empNameInput && empNoInput) {
        updateClearButtonVisibility(); // 페이지 로드 시 초기 가시성 설정
        empNameInput.addEventListener('input', updateClearButtonVisibility);
        empNoInput.addEventListener('input', updateClearButtonVisibility);
        clearEmployeeBtn.addEventListener('click', clearEmployeeSelection);
    }
});