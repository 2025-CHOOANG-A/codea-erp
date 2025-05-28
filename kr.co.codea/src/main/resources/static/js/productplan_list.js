// productplan_list.js

// 사원 검색 관련 전역 변수 (이들은 DOMContentLoaded 스코프 밖에서도 사용 가능)
let employeeSearchDebounceTimer;
const EMPLOYEE_SEARCH_DEBOUNCE_DELAY = 300; // 0.3초 디바운싱 지연 시간

// 품목 검색 관련 전역 변수 (이들은 DOMContentLoaded 스코프 밖에서도 사용 가능)
let itemSearchDebounceTimer;
const ITEM_SEARCH_DEBOUNCE_DELAY = 300; // 0.3초 디바운싱 지연 시간


document.addEventListener('DOMContentLoaded', function() {
    // --- 1. 주요 DOM 요소 참조 (모든 변수는 이 DOMContentLoaded 스코프 안에 선언됩니다) ---
    const productionPlanRegisterModalElement = document.getElementById('productionPlanRegisterModal');
    const productionPlanRegisterModal = new bootstrap.Modal(productionPlanRegisterModalElement);
    const productionPlanForm = document.getElementById('productionPlanForm');

    // 모달 제목과 제출 버튼 참조
    const modalTitle = document.getElementById('productionPlanRegisterModalLabel');
    const modalSubmitButton = document.getElementById('modalSubmitButton');

    // 품목 관련 요소
    const itemNameDisplay = document.getElementById('itemNameDisplay');
    const itemCodeInput = document.getElementById('itemCode');
    const openItemSearchInputBtn = document.getElementById('openItemSearchInputBtn');
    const itemSearchSection = document.getElementById('itemSearchSection');
    const itemSearchInput = document.getElementById('itemSearchInput');
    const executeItemSearchBtn = document.getElementById('executeItemSearchBtn');
    const itemSearchResultsTableBody = document.getElementById('itemSearchResults');

    // 담당자 (사원) 관련 요소
    const empNameDisplay = document.getElementById('empNameDisplay');
    const modalEmpNoInput = document.getElementById('modalEmpNo'); // 이 ID가 HTML에 정확히 'modalEmpNo'로 되어있어야 함
    const openEmployeeSearchInputBtn = document.getElementById('openEmployeeSearchInputBtn');
    const employeeSearchSection = document.getElementById('employeeSearchSection');
    const employeeSearchInput = document.getElementById('employeeSearchInput');
    const executeEmployeeSearchBtn = document.getElementById('executeEmployeeSearchBtn');
    const employeeSearchResultsTableBody = document.getElementById('employeeSearchResults');

    // 모달 폼 필드들을 모두 가져옵니다 (동적으로 제어하기 위해)
    // IMPORTANT: 검색 버튼들은 여기서 제외하여 setFormFieldsReadOnly에 영향을 받지 않도록 합니다.
    const formFields = productionPlanForm.querySelectorAll('input:not(#openItemSearchInputBtn, #openEmployeeSearchInputBtn, #executeItemSearchBtn, #executeEmployeeSearchBtn), select, textarea');


    // 모달 내 개별 필드 참조
    const modalPlanQtyInput = document.getElementById('modalPlanQty');
    const modalStartDateInput = document.getElementById('modalStartDate');
    const modalDueDateInput = document.getElementById('modalDueDate');
    const modalCompletionDateInput = document.getElementById('modalCompletionDate');
    const modalActualQtyInput = document.getElementById('modalActualQty');
    const modalStatusSelect = document.getElementById('modalStatus');
    const remarkInput = document.getElementById('remark');

    // --- 새로 추가된 요소 참조 ---
    const completionDateDiv = modalCompletionDateInput.closest('.mb-3'); // 부모 div 찾기
    const actualQtyDiv = modalActualQtyInput.closest('.mb-3'); // 부모 div 찾기

    // 현재 모달 모드를 저장할 변수 (register, detail, edit)
    let currentModalMode = 'register'; // 초기값은 'register'

    // --- 핵심 로직 함수들 ---

    /**
     * 품목/사원 검색 섹션을 숨기고 초기화합니다.
     */
    function resetSearchSections() {
        if (itemSearchSection) itemSearchSection.style.display = 'none';
        if (itemSearchInput) itemSearchInput.value = '';
        if (itemSearchResultsTableBody) itemSearchResultsTableBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';

        if (employeeSearchSection) employeeSearchSection.style.display = 'none';
        if (employeeSearchInput) employeeSearchInput.value = '';
        if (employeeSearchResultsTableBody) employeeSearchResultsTableBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';

        // 모달 닫을 때 empNameDisplay와 modalEmpNoInput 값도 초기화
        if (empNameDisplay) empNameDisplay.value = '';
        if (modalEmpNoInput) modalEmpNoInput.value = '';
    }

    /**
     * 폼 필드의 읽기 전용 상태를 설정합니다. (검색 버튼 제외)
     * @param {boolean} readOnly true면 읽기 전용, false면 읽기 전용 해제
     */
    function setFormFieldsReadOnly(readOnly) {
        formFields.forEach(field => {
            // Hidden 필드는 건드리지 않음
            if (field.type === 'hidden') {
                return;
            }

            // input, select, textarea 필드 처리
            if (field.tagName === 'INPUT' || field.tagName === 'SELECT' || field.tagName === 'TEXTAREA') {
                field.readOnly = readOnly;
                // select 요소는 readOnly 대신 disabled 사용
                if (field.tagName === 'SELECT') {
                    field.disabled = readOnly;
                }
                // 날짜(type="date") 필드의 달력 아이콘 비활성화 및 배경색 변경
                if (field.type === 'date') {
                    field.style.pointerEvents = readOnly ? 'none' : 'auto'; // 클릭 이벤트 비활성화
                    field.style.backgroundColor = readOnly ? '#e9ecef' : ''; // 배경색 변경으로 시각적 피드백
                }
                // 품목명 표시 필드 (itemNameDisplay)와 담당자명 표시 필드 (empNameDisplay)는 항상 readonly이므로 별도 처리
                if (field.id === 'itemNameDisplay' || field.id === 'empNameDisplay') {
                     field.readOnly = true; // 항상 읽기 전용
                }
            }
        });
    }

    /**
     * 검색 버튼들의 활성화/비활성화 상태를 설정합니다.
     * @param {boolean} disabled true면 비활성화, false면 활성화
     */
    function setSearchButtonsDisabled(disabled) {
        if (openItemSearchInputBtn) openItemSearchInputBtn.disabled = disabled;
        if (executeItemSearchBtn) executeItemSearchBtn.disabled = disabled;
        if (itemSearchResultsTableBody) {
            itemSearchResultsTableBody.querySelectorAll('.select-item-btn').forEach(btn => btn.disabled = disabled);
        }

        if (openEmployeeSearchInputBtn) openEmployeeSearchInputBtn.disabled = disabled;
        if (executeEmployeeSearchBtn) executeEmployeeSearchBtn.disabled = disabled;
        if (employeeSearchResultsTableBody) {
            employeeSearchResultsTableBody.querySelectorAll('.select-employee-btn').forEach(btn => btn.disabled = disabled);
        }
    }


    /**
     * 품목 검색 API 호출 및 결과 표시
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
                itemSearchResultsTableBody.innerHTML = '<tr><td colspan="3" class="text-center text-secondary">검색 결과가 없습니다.</td></tr>';
            }

        } catch (error) {
            console.error('품목 검색 중 오류 발생:', error);
            itemSearchResultsTableBody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">품목 검색 중 네트워크 오류가 발생했습니다.</td></tr>';
        }
    }

    /**
     * 품목 선택 처리 함수: 선택된 품목 정보를 생산 계획 등록 모달의 품목 필드에 채우고 검색 섹션 숨기기
     * @param {string} itemName 선택된 품목 이름
     * @param {string} string itemCode 선택된 품목 코드
     */
    function selectItem(itemName, itemCode) {
        if (itemNameDisplay) {
            itemNameDisplay.value = itemName;
        }
        if (itemCodeInput) {
            itemCodeInput.value = itemCode;
        }
        if (itemSearchSection) {
            itemSearchSection.style.display = 'none';
        }
    }

    /**
     * 사원 검색 API 호출 및 결과 표시
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
     * 사원 선택 처리 함수: 선택된 사원 정보를 생산 계획 등록 모달의 담당자 필드에 채우고 검색 섹션 숨기기
     * @param {string} empName 선택된 사원 이름
     * @param {string} empNo 선택된 사원 사번
     */
    function selectEmployee(empName, empNo) {
        console.log("선택된 사번:", empNo);
        if (empNameDisplay) {
            empNameDisplay.value = empName;
        }
        if (modalEmpNoInput) {
            modalEmpNoInput.value = empNo;
            console.log("모달의 empNo hidden 필드에 값 할당됨 (modalEmpNoInput):", modalEmpNoInput.value);
        }
        if (employeeSearchSection) {
            employeeSearchSection.style.display = 'none';
        }
    }

    /**
     * 생산 계획 데이터 전송을 위한 공통 AJAX 함수
     * @param {string} url 전송할 URL
     * @param {string} method HTTP 메서드 (POST, PUT)
     * @param {object} data 전송할 데이터 객체
     */
    async function sendProductPlanData(url, method, data) {
        console.log("----- 전송 전 최종 data 객체 확인 -----");
        console.log(JSON.stringify(data, null, 2)); // 2는 들여쓰기 칸 수 (가독성 향상)
        console.log("---------------------------------------");

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `서버 오류: ${response.status} - ${response.statusText}`);
            }

            const result = await response.json();
            if (result.success) {
               console.log(`생산 계획이 성공적으로 ${method === 'POST' ? '등록' : '수정'}되었습니다.`);
                productionPlanRegisterModal.hide();
                location.reload(); // 페이지 새로고침하여 목록 업데이트
            } else {
                console.error(`${method === 'POST' ? '등록' : '수정'} 실패: ` + result.message);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('오류 발생: ' + error.message);
        }
    }

    // --- 이벤트 핸들러 함수 정의 (removeEventListener를 위해 명명) ---
    // '등록' 모드일 때 modalSubmitButton의 클릭 핸들러
    const registerModeSubmitHandler = async function() {
        const formData = new FormData(productionPlanForm);
        const data = Object.fromEntries(formData.entries());
        const url = '/productplan/write';
        await sendProductPlanData(url, 'POST', data);
    };

    // '저장' (수정) 모드일 때 modalSubmitButton의 클릭 핸들러
    const saveModeSubmitHandler = async function() {
        const planId = document.getElementById('planId')?.value;
        if (!planId) {
            alert('수정할 계획 ID를 찾을 수 없습니다.');
            return;
        }
        const formData = new FormData(productionPlanForm);
        const data = Object.fromEntries(formData.entries());
        data.planId = planId; // planId를 데이터에 추가
        const url = `/productplan/${planId}`;
        await sendProductPlanData(url, 'PUT', data);
    };

    // '수정' 버튼 (상세 보기에서 편집 모드로 전환) 클릭 핸들러
    const switchToEditModeHandler = function() {
        // 이 핸들러는 한 번 실행되면 제거되어야 중복 실행 방지
        modalSubmitButton.removeEventListener('click', switchToEditModeHandler);

        setFormFieldsReadOnly(false); // 필드 수정 가능하게 변경
        setSearchButtonsDisabled(false); // 검색 버튼 활성화!
        modalTitle.textContent = '생산 계획 수정'; // 타이틀 변경
        modalSubmitButton.textContent = '저장'; // 버튼 텍스트 변경
        modalSubmitButton.classList.remove('btn-warning');
        modalSubmitButton.classList.add('btn-success');

        currentModalMode = 'edit'; // 모드 변경: 상세 -> 수정

        // 이제 '저장' 버튼이 되었을 때의 클릭 핸들러를 새로 등록합니다.
        modalSubmitButton.addEventListener('click', saveModeSubmitHandler);
    };


    // --- 2. 이벤트 리스너 등록 ---

    // 2-1. 생산 계획 등록/상세 모달 열릴 때
    productionPlanRegisterModalElement.addEventListener('show.bs.modal', function (event) {
        const button = event.relatedTarget; // 모달을 트리거한 버튼
        const mode = button.getAttribute('data-mode'); // 버튼의 data-mode 값 (register, detail)
        const planIdFromButton = button.getAttribute('data-plan-id'); // 상세/수정 모드일 경우 planId

        // 모달 UI 초기화 (기본은 등록 모드처럼 보이도록)
        productionPlanForm.reset();
        resetSearchSections(); // 이 함수가 empNameDisplay와 modalEmpNoInput을 초기화

        // 기존 planId hidden 필드 제거
        const existingPlanIdInput = document.getElementById('planId');
        if (existingPlanIdInput) {
            existingPlanIdInput.remove();
        }

        // ******** 중요: 모달이 열릴 때마다 modalSubmitButton의 모든 이벤트 리스너를 제거합니다. ********
        // 이렇게 하면 중복 리스너나 잘못된 리스너가 붙는 것을 방지할 수 있습니다.
        modalSubmitButton.removeEventListener('click', registerModeSubmitHandler);
        modalSubmitButton.removeEventListener('click', saveModeSubmitHandler);
        modalSubmitButton.removeEventListener('click', switchToEditModeHandler);

        currentModalMode = mode; // 현재 모달 모드 저장

        if (mode === 'register') {
            modalTitle.textContent = '생산 계획 등록';
            modalSubmitButton.textContent = '등록';
            modalSubmitButton.classList.remove('btn-warning', 'btn-success');
            modalSubmitButton.classList.add('btn-primary');
            modalSubmitButton.style.display = 'block'; // 버튼 표시

            setFormFieldsReadOnly(false); // 필드 편집 가능
            setSearchButtonsDisabled(false); // 검색 버튼 활성화

            // --- '등록' 모드일 때 실제 완료일/실제 수량 숨기기 ---
            if (completionDateDiv) completionDateDiv.style.display = 'none';
            if (actualQtyDiv) actualQtyDiv.style.display = 'none';
            // ----------------------------------------------------

            // '등록' 버튼 클릭 시의 핸들러 연결
            modalSubmitButton.addEventListener('click', registerModeSubmitHandler);

        } else if (mode === 'detail') {
            modalTitle.textContent = '생산 계획 상세';
            modalSubmitButton.textContent = '수정'; // 초기에는 '수정' 버튼으로 표시
            modalSubmitButton.classList.remove('btn-primary', 'btn-success');
            modalSubmitButton.classList.add('btn-warning'); // 상세 보기 상태에서는 '수정' 버튼이 주황색

            setFormFieldsReadOnly(true); // 필드 읽기 전용 (상세 보기 상태)
            setSearchButtonsDisabled(true); // 검색 버튼 비활성화 (상세 보기 상태에서는 검색 불가능)
            modalSubmitButton.style.display = 'block'; // 버튼 표시

            // --- '상세' 모드일 때 실제 완료일/실제 수량 보이기 ---
            if (completionDateDiv) completionDateDiv.style.display = 'block';
            if (actualQtyDiv) actualQtyDiv.style.display = 'block';
            // ----------------------------------------------------


            // 생산 계획 데이터 불러오기 (AJAX)
            if (planIdFromButton) {
                console.log('상세보기 요청 planId:', planIdFromButton);

                fetch(`/productplan/${planIdFromButton}`) // 상세 정보 API 엔드포인트
                    .then(response => {
                        if (!response.ok) {
                            return response.json().then(errorData => {
                                throw new Error(errorData.message || `서버 오류: ${response.status}`);
                            }).catch(() => {
                                throw new Error(`서버 오류: ${response.status} - ${response.statusText}`);
                            });
                        }
                        return response.json();
                    })
                    .then(planData => {
                        console.log('서버 응답 planData:', planData);

                        itemNameDisplay.value = planData.itemName || '';
                        itemCodeInput.value = planData.itemCode || '';
                        modalPlanQtyInput.value = planData.planQty || '';
                        modalStartDateInput.value = planData.startDate || '';
                        modalDueDateInput.value = planData.dueDate || '';
                        modalCompletionDateInput.value = planData.completionDate || '';
                        modalActualQtyInput.value = planData.actualQty || '';
                        modalStatusSelect.value = planData.status || '계획'; // 기본값 설정
                        empNameDisplay.value = planData.empName || '';
                        modalEmpNoInput.value = planData.empNo || '';
                        remarkInput.value = planData.remark || '';

                        // planId를 hidden 필드로 추가하여 수정 시 사용
                        let planIdInput = document.getElementById('planId');
                        if (!planIdInput) {
                            planIdInput = document.createElement('input');
                            planIdInput.type = 'hidden';
                            planIdInput.id = 'planId';
                            planIdInput.name = 'planId'; // name 속성도 추가
                            productionPlanForm.appendChild(planIdInput);
                        }
                        planIdInput.value = planData.planId; // planId 저장

                        // '수정' 버튼 클릭 시의 핸들러 연결 (편집 모드로 전환)
                        modalSubmitButton.addEventListener('click', switchToEditModeHandler);

                    })
                    .catch(error => {
                        console.error('Error fetching plan details:', error);
                        alert('생산 계획 상세 정보를 불러오는 중 오류가 발생했습니다: ' + error.message);
                        productionPlanRegisterModal.hide(); // 오류 발생 시 모달 닫기
                    });
            } else {
                 console.warn("planId가 없습니다. 상세 정보를 불러올 수 없습니다.");
                 alert('상세 정보를 불러올 계획 번호가 없습니다.');
                 productionPlanRegisterModal.hide();
            }
        }
    });

    // 2-2. 생산 계획 등록 모달 닫힐 때
    productionPlanRegisterModalElement.addEventListener('hidden.bs.modal', function () {
        productionPlanForm.reset(); // 폼 초기화
        resetSearchSections(); // 검색 섹션과 emp/item Display 값 초기화
        const planIdInput = document.getElementById('planId');
        if (planIdInput) {
            planIdInput.remove();
        }
        // ******** 중요: 모달 닫힐 때 modalSubmitButton의 모든 이벤트 리스너를 제거합니다. ********
        modalSubmitButton.removeEventListener('click', registerModeSubmitHandler);
        modalSubmitButton.removeEventListener('click', saveModeSubmitHandler);
        modalSubmitButton.removeEventListener('click', switchToEditModeHandler);

        // 모달 닫힐 때 모드 초기화
        currentModalMode = 'register';
        setSearchButtonsDisabled(false); // 모달이 닫히면 검색 버튼들은 다시 활성화
        // 모달이 닫히면 항상 필드를 다시 보이도록 (다음 등록을 위해)
        if (completionDateDiv) completionDateDiv.style.display = 'block';
        if (actualQtyDiv) actualQtyDiv.style.display = 'block';
    });


    // --- 품목 검색 관련 이벤트 리스너 ---
    // 각 검색 버튼은 이제 `setSearchButtonsDisabled` 함수에 의해 직접 제어됩니다.
    // 따라서 개별 클릭 핸들러 내에서 `currentModalMode` 체크를 제거합니다.
    if (openItemSearchInputBtn) {
        openItemSearchInputBtn.addEventListener('click', function() {
            if (itemSearchSection.style.display === 'none') {
                itemSearchSection.style.display = 'block';
                if (itemSearchInput) {
                    itemSearchInput.focus();
                }
                if (employeeSearchSection && employeeSearchSection.style.display !== 'none') {
                    employeeSearchSection.style.display = 'none';
                }
            } else {
                itemSearchSection.style.display = 'none';
            }
        });
    }
    if (itemSearchInput) {
        itemSearchInput.addEventListener('keyup', handleItemSearchInputKeyup);
    }
    if (executeItemSearchBtn) {
        executeItemSearchBtn.addEventListener('click', handleExecuteItemSearchClick);
    }
    if (itemSearchResultsTableBody) {
        itemSearchResultsTableBody.addEventListener('click', handleItemSearchResultsClick);
    }

    // --- 사원 검색 관련 이벤트 리스너 ---
    // 각 검색 버튼은 이제 `setSearchButtonsDisabled` 함수에 의해 직접 제어됩니다.
    // 따라서 개별 클릭 핸들러 내에서 `currentModalMode` 체크를 제거합니다.
    if (openEmployeeSearchInputBtn) {
        openEmployeeSearchInputBtn.addEventListener('click', function() {
            if (employeeSearchSection.style.display === 'none') {
                employeeSearchSection.style.display = 'block';
                if (employeeSearchInput) {
                    employeeSearchInput.focus();
                }
                if (itemSearchSection && itemSearchSection.style.display !== 'none') {
                    itemSearchSection.style.display = 'none';
                }
            } else {
                employeeSearchSection.style.display = 'none';
            }
        });
    }
    if (employeeSearchInput) {
        employeeSearchInput.addEventListener('keyup', handleEmployeeSearchInputKeyup);
    }
    if (executeEmployeeSearchBtn) {
        executeEmployeeSearchBtn.addEventListener('click', handleExecuteEmployeeSearchClick);
    }
    if (employeeSearchResultsTableBody) {
        employeeSearchResultsTableBody.addEventListener('click', handleEmployeeSearchResultsClick);
    }

    // --- 품목 검색 관련 이벤트 핸들러 함수들 ---
    function handleItemSearchInputKeyup(event) {
        clearTimeout(itemSearchDebounceTimer);
        const query = event.target.value.trim();
        itemSearchDebounceTimer = setTimeout(() => {
            searchItems(query);
        }, ITEM_SEARCH_DEBOUNCE_DELAY);
    }

    function handleExecuteItemSearchClick() {
        const itemSearchInput = document.getElementById('itemSearchInput');
        if (itemSearchInput) {
            searchItems(itemSearchInput.value.trim());
        }
    }

    function handleItemSearchResultsClick(event) {
        if (event.target.classList.contains('select-item-btn')) {
            const itemName = event.target.dataset.itemName;
            const itemCode = event.target.dataset.itemCode;
            selectItem(itemName, itemCode);
        }
    }


    // --- 사원 검색 관련 이벤트 핸들러 함수들 ---
    function handleEmployeeSearchInputKeyup(event) {
        clearTimeout(employeeSearchDebounceTimer);
        const query = event.target.value.trim();
        employeeSearchDebounceTimer = setTimeout(() => {
            searchEmployees(query);
        }, EMPLOYEE_SEARCH_DEBOUNCE_DELAY);
    }

    function handleExecuteEmployeeSearchClick() {
        const employeeSearchInput = document.getElementById('employeeSearchInput');
        if (employeeSearchInput) {
            searchEmployees(employeeSearchInput.value.trim());
        }
    }

    function handleEmployeeSearchResultsClick(event) {
        if (event.target.classList.contains('select-employee-btn')) {
            const empName = event.target.dataset.empName;
            const empNo = event.target.dataset.empNo;
            selectEmployee(empName, empNo);
        }
    }


    // 비고 모달 관련 스크립트 (기존과 동일)
    var remarkModal = document.getElementById('remarkModal');
    if (remarkModal) {
        remarkModal.addEventListener('show.bs.modal', function (event) {
            var button = event.relatedTarget;
            var remarkContent = button.getAttribute('data-remark');
            var modalBodyP = remarkModal.querySelector('#modalRemarkContent');

            if (remarkContent === null || remarkContent.trim() === '') {
                modalBodyP.textContent = '등록된 비고 내용이 없습니다.';
            } else {
                modalBodyP.textContent = remarkContent;
            }
        });
    }

}); // <--- DOMContentLoaded 이벤트 리스너 끝