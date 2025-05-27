// productplan_list.js

// 사원 검색 관련 전역 변수
let employeeSearchDebounceTimer;
const EMPLOYEE_SEARCH_DEBOUNCE_DELAY = 300; // 0.3초 디바운싱 지연 시간

// 품목 검색 관련 전역 변수 (새로 추가)
let itemSearchDebounceTimer;
const ITEM_SEARCH_DEBOUNCE_DELAY = 300; // 0.3초 디바운싱 지연 시간

document.addEventListener('DOMContentLoaded', function() {
    // --- 1. 주요 DOM 요소 참조 ---
    const productionPlanRegisterModalElement = document.getElementById('productionPlanRegisterModal');
    const productionPlanRegisterModal = new bootstrap.Modal(productionPlanRegisterModalElement);
    const productionPlanForm = document.getElementById('productionPlanForm');

    // 품목 관련 요소 (새로 추가)
    const itemNameDisplay = document.getElementById('itemNameDisplay'); // 품목 이름 표시 필드
    const itemCodeInput = document.getElementById('itemCode');         // 품목 코드 (hidden) 필드
    const openItemSearchInputBtn = document.getElementById('openItemSearchInputBtn'); // '품목 검색' 버튼
    const itemSearchSection = document.getElementById('itemSearchSection');     // 품목 검색 섹션 전체

    // 품목 검색 섹션 내부 요소들 (새로 추가)
    const itemSearchInput = document.getElementById('itemSearchInput');
    const executeItemSearchBtn = document.getElementById('executeItemSearchBtn');
    const itemSearchResultsTableBody = document.getElementById('itemSearchResults');

    // 담당자 (사원) 관련 요소
    const empNameDisplay = document.getElementById('empNameDisplay'); // 담당자 이름 표시 필드
    const empNoInput = document.getElementById('empNo');             // 담당자 사번 (hidden) 필드
    const openEmployeeSearchInputBtn = document.getElementById('openEmployeeSearchInputBtn'); // '사원 검색' 버튼
    const employeeSearchSection = document.getElementById('employeeSearchSection'); // 사원 검색 섹션 전체

    // 사원 검색 섹션 내부 요소들
    const employeeSearchInput = document.getElementById('employeeSearchInput');
    const executeEmployeeSearchBtn = document.getElementById('executeEmployeeSearchBtn');
    const employeeSearchResultsTableBody = document.getElementById('employeeSearchResults');


    // --- 2. 이벤트 리스너 등록 ---

    // 2-1. 생산 계획 등록 모달 열릴 때 폼 및 검색 섹션 초기화
    productionPlanRegisterModalElement.addEventListener('shown.bs.modal', function () {
        productionPlanForm.reset(); // 폼 내용 초기화
        // document.getElementById('itemCode').focus(); // 품목 코드 필드는 이제 hidden이므로 포커스 변경
        if (itemNameDisplay) itemNameDisplay.focus(); // 품목 이름 필드에 포커스

        // 품목 관련 필드 및 섹션 초기화
        if (itemNameDisplay) itemNameDisplay.value = '';
        if (itemCodeInput) itemCodeInput.value = '';
        if (itemSearchSection) {
            itemSearchSection.style.display = 'none';
        }
        if (itemSearchInput) {
            itemSearchInput.value = '';
        }
        if (itemSearchResultsTableBody) {
            itemSearchResultsTableBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';
        }

        // 담당자 (사원) 관련 필드 및 섹션 초기화
        if (empNameDisplay) empNameDisplay.value = '';
        if (empNoInput) empNoInput.value = '';
        if (employeeSearchSection) {
            employeeSearchSection.style.display = 'none';
        }
        if (employeeSearchInput) {
            employeeSearchInput.value = '';
        }
        if (employeeSearchResultsTableBody) {
            employeeSearchResultsTableBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';
        }
    });

    // 2-2. 생산 계획 등록 모달 닫힐 때 검색 섹션 초기화
    productionPlanRegisterModalElement.addEventListener('hidden.bs.modal', function () {
        // 품목 검색 섹션 초기화
        if (itemSearchSection) itemSearchSection.style.display = 'none';
        if (itemSearchInput) itemSearchInput.value = '';
        if (itemSearchResultsTableBody) itemSearchResultsTableBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';

        // 사원 검색 섹션 초기화
        if (employeeSearchSection) employeeSearchSection.style.display = 'none';
        if (employeeSearchInput) employeeSearchInput.value = '';
        if (employeeSearchResultsTableBody) employeeSearchResultsTableBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';
    });

    // --- 품목 검색 관련 이벤트 리스너 (새로 추가) ---
    // 2-3. '품목 검색' 버튼 클릭 시 품목 검색 섹션 토글
    if (openItemSearchInputBtn) {
        openItemSearchInputBtn.addEventListener('click', function() {
            if (itemSearchSection.style.display === 'none') {
                itemSearchSection.style.display = 'block';
                if (itemSearchInput) {
                    itemSearchInput.focus();
                }
                // 다른 검색 섹션이 열려있다면 닫기 (동시에 하나만 열리도록)
                if (employeeSearchSection && employeeSearchSection.style.display !== 'none') {
                    employeeSearchSection.style.display = 'none';
                }
            } else {
                itemSearchSection.style.display = 'none';
            }
        });
    }

    // 2-4. 품목 검색 입력 필드 키보드 입력 시 디바운싱 적용
    if (itemSearchInput) {
        itemSearchInput.addEventListener('keyup', handleItemSearchInputKeyup);
    }

    // 2-5. 품목 검색 섹션 내 '검색' 버튼 클릭 시
    if (executeItemSearchBtn) {
        executeItemSearchBtn.addEventListener('click', handleExecuteItemSearchClick);
    }

    // 2-6. 품목 검색 결과 테이블 내 '선택' 버튼 클릭 시 (이벤트 위임)
    if (itemSearchResultsTableBody) {
        itemSearchResultsTableBody.addEventListener('click', handleItemSearchResultsClick);
    }

    // --- 사원 검색 관련 이벤트 리스너 (기존 로직) ---
    // 2-7. '사원 검색' 버튼 클릭 시 사원 검색 섹션 토글
    if (openEmployeeSearchInputBtn) {
        openEmployeeSearchInputBtn.addEventListener('click', function() {
            if (employeeSearchSection.style.display === 'none') {
                employeeSearchSection.style.display = 'block';
                if (employeeSearchInput) {
                    employeeSearchInput.focus();
                }
                // 다른 검색 섹션이 열려있다면 닫기 (동시에 하나만 열리도록)
                if (itemSearchSection && itemSearchSection.style.display !== 'none') {
                    itemSearchSection.style.display = 'none';
                }
            } else {
                employeeSearchSection.style.display = 'none';
            }
        });
    }

    // 2-8. 사원 검색 입력 필드 키보드 입력 시 디바운싱 적용
    if (employeeSearchInput) {
        employeeSearchInput.addEventListener('keyup', handleEmployeeSearchInputKeyup);
    }

    // 2-9. 사원 검색 섹션 내 '검색' 버튼 클릭 시
    if (executeEmployeeSearchBtn) {
        executeEmployeeSearchBtn.addEventListener('click', handleExecuteEmployeeSearchClick);
    }

    // 2-10. 사원 검색 결과 테이블 내 '선택' 버튼 클릭 시 (이벤트 위임)
    if (employeeSearchResultsTableBody) {
        employeeSearchResultsTableBody.addEventListener('click', handleEmployeeSearchResultsClick);
    }

    // 2-11. 생산 계획 등록 폼 제출 시 (AJAX 처리)
    productionPlanForm.addEventListener('submit', function(event) {
        event.preventDefault(); // 기본 폼 제출 방지

        const formData = new FormData(productionPlanForm);
        const data = Object.fromEntries(formData.entries());

        fetch('/productplan/write', { // 실제 API 경로로 변경하세요.
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errorData => {
                    throw new Error(errorData.message || `서버 오류: ${response.status}`);
                });
            }
            return response.json();
        })
        .then(result => {
            if (result.success) {
                alert('생산 계획이 성공적으로 등록되었습니다.');
                productionPlanRegisterModal.hide();
                location.reload();
            } else {
                alert('등록 실패: ' + result.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('오류 발생: ' + error.message);
        });
    });
}); // <--- DOMContentLoaded 이벤트 리스너 끝

// --- 품목 검색 관련 이벤트 핸들러 함수들 (새로 추가) ---

/**
 * 품목 검색 입력 필드 키보드 입력 시 디바운싱 처리
 */
function handleItemSearchInputKeyup(event) {
    clearTimeout(itemSearchDebounceTimer);
    const query = event.target.value.trim();
    itemSearchDebounceTimer = setTimeout(() => {
        searchItems(query);
    }, ITEM_SEARCH_DEBOUNCE_DELAY);
}

/**
 * 품목 검색 섹션 내 '검색' 버튼 클릭 처리
 */
function handleExecuteItemSearchClick() {
    const itemSearchInput = document.getElementById('itemSearchInput');
    if (itemSearchInput) {
        searchItems(itemSearchInput.value.trim());
    }
}

/**
 * 품목 검색 결과 테이블 내 '선택' 버튼 클릭 처리 (이벤트 위임)
 */
function handleItemSearchResultsClick(event) {
    if (event.target.classList.contains('select-item-btn')) {
        const itemName = event.target.dataset.itemName;
        const itemCode = event.target.dataset.itemCode;
        selectItem(itemName, itemCode);
    }
}


// --- 사원 검색 관련 이벤트 핸들러 함수들 (기존 로직) ---

/**
 * 사원 검색 입력 필드 키보드 입력 시 디바운싱 처리
 */
function handleEmployeeSearchInputKeyup(event) {
    clearTimeout(employeeSearchDebounceTimer);
    const query = event.target.value.trim();
    employeeSearchDebounceTimer = setTimeout(() => {
        searchEmployees(query);
    }, EMPLOYEE_SEARCH_DEBOUNCE_DELAY);
}

/**
 * 사원 검색 섹션 내 '검색' 버튼 클릭 처리
 */
function handleExecuteEmployeeSearchClick() {
    const employeeSearchInput = document.getElementById('employeeSearchInput');
    if (employeeSearchInput) {
        searchEmployees(employeeSearchInput.value.trim());
    }
}

/**
 * 사원 검색 결과 테이블 내 '선택' 버튼 클릭 처리 (이벤트 위임)
 */
function handleEmployeeSearchResultsClick(event) {
    if (event.target.classList.contains('select-employee-btn')) {
        const empName = event.target.dataset.empName;
        const empNo = event.target.dataset.empNo;
        selectEmployee(empName, empNo);
    }
}


// --- 핵심 로직 함수들 ---

/**
 * 품목 검색 API 호출 및 결과 표시 (새로 추가)
 * @param {string} query 검색어 (품목명 또는 품목 코드)
 */
async function searchItems(query) {
    const itemSearchResultsBody = document.getElementById('itemSearchResults');
    if (!itemSearchResultsBody) return;

    itemSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 중...</td></tr>';

    if (!query || query.length < 2) {
        itemSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">두 글자 이상 입력해주세요.</td></tr>';
        return;
    }

    try {
        const response = await fetch(`/productplan/api/item/search?query=${encodeURIComponent(query)}`);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('품목 검색 실패:', response.status, errorText);
            itemSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">품목 검색에 실패했습니다. (HTTP ' + response.status + ')</td></tr>';
            return;
        }

        const items = await response.json();

        itemSearchResultsBody.innerHTML = ''; // 기존 결과 지우기

        if (items && items.length > 0) {
            items.forEach(item => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${item.itemName || ''}</td>
                    <td>${item.itemCode || ''}</td>
                    <td>
                        <button type="button" class="btn btn-sm btn-primary select-item-btn"
                                data-item-name="${item.itemName || ''}"
                                data-item-code="${item.itemCode || ''}">선택</button>
                    </td>
                `;
                itemSearchResultsBody.appendChild(row);
            });
        } else {
            itemSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';
        }

    } catch (error) {
        console.error('품목 검색 중 오류 발생:', error);
        itemSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">품목 검색 중 네트워크 오류가 발생했습니다.</td></tr>';
    }
}

/**
 * 품목 선택 처리 함수: 선택된 품목 정보를 생산 계획 등록 모달의 품목 필드에 채우고 검색 섹션 숨기기 (새로 추가)
 * @param {string} itemName 선택된 품목 이름
 * @param {string} itemCode 선택된 품목 코드
 */
function selectItem(itemName, itemCode) {
    const itemNameDisplay = document.getElementById('itemNameDisplay'); // 품목 이름 표시 필드
    const itemCodeInput = document.getElementById('itemCode');         // 품목 코드 (hidden) 필드
    const itemSearchSection = document.getElementById('itemSearchSection'); // 품목 검색 섹션

    if (itemNameDisplay) {
        itemNameDisplay.value = itemName; // 이름 필드에 품목명 채우기
    }
    if (itemCodeInput) {
        itemCodeInput.value = itemCode; // 코드 필드에 품목 코드 채우기
    }
    if (itemSearchSection) {
        itemSearchSection.style.display = 'none'; // 품목 검색 섹션 숨기기
    }
    // 필요하다면, 여기에 품목 관련 유효성 검사 피드백 제거 로직을 추가할 수 있습니다.
}


/**
 * 사원 검색 API 호출 및 결과 표시 (기존 로직)
 * @param {string} query 검색어 (사원명 또는 사번)
 */
async function searchEmployees(query) {
    const employeeSearchResultsBody = document.getElementById('employeeSearchResults');
    if (!employeeSearchResultsBody) return;

    employeeSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 중...</td></tr>';

    if (!query || query.length < 2) {
        employeeSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">두 글자 이상 입력해주세요.</td></tr>';
        return;
    }

    try {
        const response = await fetch(`/storage/api/employees/search?query=${encodeURIComponent(query)}`);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('사원 검색 실패:', response.status, errorText);
            employeeSearchResultsBody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">사원 검색에 실패했습니다. (HTTP ' + response.status + ')</td></tr>';
            return;
        }

        const employees = await response.json();

        employeeSearchResultsBody.innerHTML = ''; // 기존 결과 지우기

        if (employees && employees.length > 0) {
            employees.forEach(emp => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${emp.empName || ''}</td>
                    <td>${emp.empNo || ''}</td>
                    <td>
                        <button type="button" class="btn btn-sm btn-primary select-employee-btn"
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

/**
 * 사원 선택 처리 함수: 선택된 사원 정보를 생산 계획 등록 모달의 담당자 필드에 채우고 검색 섹션 숨기기 (기존 로직)
 * @param {string} empName 선택된 사원 이름
 * @param {string} empNo 선택된 사원 사번
 */
function selectEmployee(empName, empNo) {
    const empNameDisplay = document.getElementById('empNameDisplay'); // 담당자 이름 표시 필드
    const empNoInput = document.getElementById('empNo');             // 담당자 사번 (숨겨진) 필드
    const employeeSearchSection = document.getElementById('employeeSearchSection'); // 사원 검색 섹션

    if (empNameDisplay) {
        empNameDisplay.value = empName; // 이름 필드에 사원명 채우기
    }
    if (empNoInput) {
        empNoInput.value = empNo; // 사번 필드에 사번 채우기
    }
    if (employeeSearchSection) {
        employeeSearchSection.style.display = 'none'; // 사원 검색 섹션 숨기기
    }
    // 담당자 초기화 버튼이 있다면 여기에 가시성 로직 추가
    // updateClearButtonVisibility(); // 필요 시
}

// 이 함수들은 현재 HTML에 관련 UI가 없으므로 주석 처리된 상태를 유지합니다.
/*
function clearEmployeeSelection() { ... }
function updateClearButtonVisibility() { ... }
*/