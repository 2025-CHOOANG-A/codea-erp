// productplan_list.js - 수정된 버전

// 전역 변수 (기존 유지)
let employeeSearchDebounceTimer;
let itemSearchDebounceTimer;
const EMPLOYEE_SEARCH_DEBOUNCE_DELAY = 300;
const ITEM_SEARCH_DEBOUNCE_DELAY = 300;

document.addEventListener('DOMContentLoaded', function() {
    // --- 1. 주요 DOM 요소 참조 (수정된 부분)
    const productionPlanRegisterModalElement = document.getElementById('productionPlanRegisterModal');
    const productionPlanRegisterModal = new bootstrap.Modal(productionPlanRegisterModalElement);
    const productionPlanForm = document.getElementById('productionPlanForm');

    // 모달 제목과 제출 버튼 참조
    const modalTitle = document.getElementById('productionPlanRegisterModalLabel');
    const modalSubmitButton = document.getElementById('modalSubmitButton');

    // 새로운 검색 방식의 요소들
    const itemSearchInput = document.getElementById('itemSearchInput');
    const itemSearchDropdown = document.getElementById('itemSearchDropdown');
    const itemCodeInput = document.getElementById('itemCode');
    
    const employeeSearchInput = document.getElementById('employeeSearchInput');
    const employeeSearchDropdown = document.getElementById('employeeSearchDropdown');
    const modalEmpNoInput = document.getElementById('modalEmpNo');

    // 모달 폼 필드들 (수정된 방식)
    const formFields = productionPlanForm.querySelectorAll('input:not([type="hidden"]), select, textarea');

    // 모달 내 개별 필드 참조 (기존 유지)
    const modalPlanQtyInput = document.getElementById('modalPlanQty');
    const modalStartDateInput = document.getElementById('modalStartDate');
    const modalDueDateInput = document.getElementById('modalDueDate');
    const modalCompletionDateInput = document.getElementById('modalCompletionDate');
    const modalActualQtyInput = document.getElementById('modalActualQty');
    const modalStatusSelect = document.getElementById('modalStatus');
    const remarkInput = document.getElementById('remark');

    const completionDateDiv = modalCompletionDateInput.closest('.mb-3');
    const actualQtyDiv = modalActualQtyInput.closest('.mb-3');
    
    // 작업지시 버튼들 (기존 유지)
    const createWorkOrderButton = document.getElementById('createWorkOrderButton');
    const cancelWorkOrderButton = document.getElementById('cancelWorkOrderButton');

    let currentModalMode = 'register';

    // --- 새로운 드롭다운 관련 함수들 ---
    function showDropdown(dropdown) {
        dropdown.classList.add('show');
    }

    function hideDropdown(dropdown) {
        dropdown.classList.remove('show');
    }

    function hideAllDropdowns() {
        hideDropdown(itemSearchDropdown);
        hideDropdown(employeeSearchDropdown);
    }

    // --- 수정된 검색 섹션 초기화 함수 ---
    function resetSearchSections() {
        hideAllDropdowns();
        if (itemSearchInput) itemSearchInput.value = '';
        if (itemCodeInput) itemCodeInput.value = '';
        if (employeeSearchInput) employeeSearchInput.value = '';
        if (modalEmpNoInput) modalEmpNoInput.value = '';
    }

    // --- 수정된 폼 필드 읽기 전용 설정 함수 ---
    function setFormFieldsReadOnly(readOnly) {
        formFields.forEach(field => {
            if (field.type === 'hidden') return;

            if (field.tagName === 'INPUT' || field.tagName === 'SELECT' || field.tagName === 'TEXTAREA') {
                field.readOnly = readOnly;
                if (field.tagName === 'SELECT') {
                    field.disabled = readOnly;
                }
                if (field.type === 'date') {
                    field.style.pointerEvents = readOnly ? 'none' : 'auto';
                    field.style.backgroundColor = readOnly ? '#e9ecef' : '';
                }
            }
        });
    }

    // --- 수정된 검색 버튼 비활성화 함수 ---
    function setSearchButtonsDisabled(disabled) {
        if (itemSearchInput) itemSearchInput.disabled = disabled;
        if (employeeSearchInput) employeeSearchInput.disabled = disabled;
        
        if (disabled) {
            hideAllDropdowns();
        }
    }

    // --- 품목 검색 API 호출 (수정됨) ---
    async function searchItems(query) {
        if (!itemSearchDropdown) return;

        itemSearchDropdown.innerHTML = '<div class="search-loading">검색 중...</div>';

        if (!query || query.length < 2) {
            itemSearchDropdown.innerHTML = '<div class="search-no-results">두 글자 이상 입력해주세요.</div>';
            return;
        }

        try {
            const response = await fetch(`/productplan/api/item/search?query=${encodeURIComponent(query)}`);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const items = await response.json();
            displayItemResults(items);

        } catch (error) {
            console.error('품목 검색 오류:', error);
            itemSearchDropdown.innerHTML = '<div class="search-no-results">검색 중 오류가 발생했습니다.</div>';
        }
    }

    // --- 품목 검색 결과 표시 (새로 추가) ---
    function displayItemResults(items) {
        if (!items || items.length === 0) {
            itemSearchDropdown.innerHTML = '<div class="search-no-results">검색 결과가 없습니다.</div>';
            return;
        }

        let html = '';
        items.forEach(item => {
            html += `
                <div class="search-item" data-item-name="${item.itemName || ''}" data-item-code="${item.itemCode || ''}">
                    <div class="item-info">
                        <span class="item-name">${item.itemName || ''}</span>
                        <span class="item-code">${item.itemCode || ''}</span>
                    </div>
                </div>
            `;
        });
        itemSearchDropdown.innerHTML = html;
    }

    // --- 품목 선택 처리 (수정됨) ---
    function selectItem(itemName, itemCode) {
        if (itemSearchInput) itemSearchInput.value = itemName;
        if (itemCodeInput) itemCodeInput.value = itemCode;
        hideDropdown(itemSearchDropdown);
    }

    // --- 사원 검색 API 호출 (수정됨) ---
    async function searchEmployees(query) {
        if (!employeeSearchDropdown) return;

        employeeSearchDropdown.innerHTML = '<div class="search-loading">검색 중...</div>';

        if (!query || query.length < 2) {
            employeeSearchDropdown.innerHTML = '<div class="search-no-results">두 글자 이상 입력해주세요.</div>';
            return;
        }

        try {
            const response = await fetch(`/storage/api/employees/search?query=${encodeURIComponent(query)}`);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const employees = await response.json();
            displayEmployeeResults(employees);

        } catch (error) {
            console.error('사원 검색 오류:', error);
            employeeSearchDropdown.innerHTML = '<div class="search-no-results">검색 중 오류가 발생했습니다.</div>';
        }
    }

    // --- 사원 검색 결과 표시 (새로 추가) ---
    function displayEmployeeResults(employees) {
        if (!employees || employees.length === 0) {
            employeeSearchDropdown.innerHTML = '<div class="search-no-results">검색 결과가 없습니다.</div>';
            return;
        }

        let html = '';
        employees.forEach(emp => {
            html += `
                <div class="search-item" data-emp-name="${emp.empName || ''}" data-emp-no="${emp.empNo || ''}">
                    <div class="employee-info">
                        <span class="emp-name">${emp.empName || ''}</span>
                        <span class="emp-no">${emp.empNo || ''}</span>
                    </div>
                </div>
            `;
        });
        employeeSearchDropdown.innerHTML = html;
    }

    // --- 사원 선택 처리 (수정됨) ---
    function selectEmployee(empName, empNo) {
        if (employeeSearchInput) employeeSearchInput.value = empName;
        if (modalEmpNoInput) modalEmpNoInput.value = empNo;
        hideDropdown(employeeSearchDropdown);
    }

    // --- 기존 AJAX 함수들 (변경 없음) ---
    async function sendProductPlanData(url, method, data) {
        console.log("----- 전송 전 최종 data 객체 확인 -----");
        console.log(JSON.stringify(data, null, 2));
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
                location.reload();
            } else {
                console.error(`${method === 'POST' ? '등록' : '수정'} 실패: ` + result.message);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('오류 발생: ' + error.message);
        }
    }

    // --- 기존 이벤트 핸들러들 (변경 없음) ---
    const registerModeSubmitHandler = async function() {
        const formData = new FormData(productionPlanForm);
        const data = Object.fromEntries(formData.entries());
        const url = '/productplan/write';
        await sendProductPlanData(url, 'POST', data);
    };

    const saveModeSubmitHandler = async function() {
        const planId = document.getElementById('planId')?.value;
        if (!planId) {
            alert('수정할 계획 ID를 찾을 수 없습니다.');
            return;
        }
        const formData = new FormData(productionPlanForm);
        const data = Object.fromEntries(formData.entries());
        data.planId = planId;
        const url = `/productplan/${planId}`;
        await sendProductPlanData(url, 'PUT', data);
    };

    const switchToEditModeHandler = function() {
        modalSubmitButton.removeEventListener('click', switchToEditModeHandler);
        setFormFieldsReadOnly(false);
        setSearchButtonsDisabled(false);
        modalTitle.textContent = '생산 계획 수정';
        modalSubmitButton.textContent = '저장';
        modalSubmitButton.classList.remove('btn-warning');
        modalSubmitButton.classList.add('btn-success');
        currentModalMode = 'edit';
        modalSubmitButton.addEventListener('click', saveModeSubmitHandler);
    };

    // --- 새로운 이벤트 리스너들 ---
    
    // 품목 검색 입력 이벤트
    if (itemSearchInput) {
        itemSearchInput.addEventListener('input', function(e) {
            const query = e.target.value.trim();
            
            clearTimeout(itemSearchDebounceTimer);
            
            if (query.length < 2) {
                hideDropdown(itemSearchDropdown);
                return;
            }
            
            showDropdown(itemSearchDropdown);
            
            itemSearchDebounceTimer = setTimeout(() => {
                searchItems(query);
            }, ITEM_SEARCH_DEBOUNCE_DELAY);
        });

        // 포커스 해제 시 드롭다운 숨기기 (지연 처리로 클릭 이벤트 허용)
        itemSearchInput.addEventListener('blur', function() {
            setTimeout(() => {
                hideDropdown(itemSearchDropdown);
            }, 200);
        });

        // 포커스 시 기존 검색 결과가 있으면 드롭다운 표시
        itemSearchInput.addEventListener('focus', function() {
            if (this.value.trim().length >= 2 && itemSearchDropdown.innerHTML.trim() !== '') {
                showDropdown(itemSearchDropdown);
            }
        });
    }

    // 사원 검색 입력 이벤트
    if (employeeSearchInput) {
        employeeSearchInput.addEventListener('input', function(e) {
            const query = e.target.value.trim();
            
            clearTimeout(employeeSearchDebounceTimer);
            
            if (query.length < 2) {
                hideDropdown(employeeSearchDropdown);
                return;
            }
            
            showDropdown(employeeSearchDropdown);
            
            employeeSearchDebounceTimer = setTimeout(() => {
                searchEmployees(query);
            }, EMPLOYEE_SEARCH_DEBOUNCE_DELAY);
        });

        // 포커스 해제 시 드롭다운 숨기기 (지연 처리로 클릭 이벤트 허용)
        employeeSearchInput.addEventListener('blur', function() {
            setTimeout(() => {
                hideDropdown(employeeSearchDropdown);
            }, 200);
        });

        // 포커스 시 기존 검색 결과가 있으면 드롭다운 표시
        employeeSearchInput.addEventListener('focus', function() {
            if (this.value.trim().length >= 2 && employeeSearchDropdown.innerHTML.trim() !== '') {
                showDropdown(employeeSearchDropdown);
            }
        });
    }

    // 품목 드롭다운 클릭 이벤트
    if (itemSearchDropdown) {
        itemSearchDropdown.addEventListener('click', function(e) {
            const searchItem = e.target.closest('.search-item');
            if (searchItem) {
                const itemName = searchItem.getAttribute('data-item-name');
                const itemCode = searchItem.getAttribute('data-item-code');
                selectItem(itemName, itemCode);
            }
        });
    }

    // 사원 드롭다운 클릭 이벤트
    if (employeeSearchDropdown) {
        employeeSearchDropdown.addEventListener('click', function(e) {
            const searchItem = e.target.closest('.search-item');
            if (searchItem) {
                const empName = searchItem.getAttribute('data-emp-name');
                const empNo = searchItem.getAttribute('data-emp-no');
                selectEmployee(empName, empNo);
            }
        });
    }

    // ESC 키로 드롭다운 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            hideAllDropdowns();
        }
    });

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.search-input-container')) {
            hideAllDropdowns();
        }
    });

    // --- 2. 기존 모달 이벤트 리스너들 (수정됨) ---
    productionPlanRegisterModalElement.addEventListener('show.bs.modal', function (event) {
        const button = event.relatedTarget;
        const mode = button.getAttribute('data-mode');
        const planIdFromButton = button.getAttribute('data-plan-id');

        productionPlanForm.reset();
        resetSearchSections();

        const existingPlanIdInput = document.getElementById('planId');
        if (existingPlanIdInput) {
            existingPlanIdInput.remove();
        }

        modalSubmitButton.removeEventListener('click', registerModeSubmitHandler);
        modalSubmitButton.removeEventListener('click', saveModeSubmitHandler);
        modalSubmitButton.removeEventListener('click', switchToEditModeHandler);

        currentModalMode = mode;

        if (mode === 'register') {
            modalTitle.textContent = '생산 계획 등록';
            modalSubmitButton.textContent = '등록';
            modalSubmitButton.classList.remove('btn-warning', 'btn-success');
            modalSubmitButton.classList.add('btn-primary');
            modalSubmitButton.style.display = 'block';

            setFormFieldsReadOnly(false);
            setSearchButtonsDisabled(false);

            if (completionDateDiv) completionDateDiv.style.display = 'none';
            if (actualQtyDiv) actualQtyDiv.style.display = 'none';

            modalSubmitButton.addEventListener('click', registerModeSubmitHandler);

        } else if (mode === 'detail') {
            modalTitle.textContent = '생산 계획 상세';
            modalSubmitButton.textContent = '수정';
            modalSubmitButton.classList.remove('btn-primary', 'btn-success');
            modalSubmitButton.classList.add('btn-warning');

            setFormFieldsReadOnly(true);
            setSearchButtonsDisabled(true);
            modalSubmitButton.style.display = 'block';

            if (completionDateDiv) completionDateDiv.style.display = 'block';
            if (actualQtyDiv) actualQtyDiv.style.display = 'block';

            if (planIdFromButton) {
                console.log('상세보기 요청 planId:', planIdFromButton);

                fetch(`/productplan/${planIdFromButton}`)
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

                        // 수정된 부분: 새로운 입력 필드들에 값 설정
                        if (itemSearchInput) itemSearchInput.value = planData.itemName || '';
                        if (itemCodeInput) itemCodeInput.value = planData.itemCode || '';
                        if (modalPlanQtyInput) modalPlanQtyInput.value = planData.planQty || '';
                        if (modalStartDateInput) modalStartDateInput.value = planData.startDate || '';
                        if (modalDueDateInput) modalDueDateInput.value = planData.dueDate || '';
                        if (modalCompletionDateInput) modalCompletionDateInput.value = planData.completionDate || '';
                        if (modalActualQtyInput) modalActualQtyInput.value = planData.actualQty || '';
                        if (modalStatusSelect) modalStatusSelect.value = planData.status || '계획';
                        if (employeeSearchInput) employeeSearchInput.value = planData.empName || '';
                        if (modalEmpNoInput) modalEmpNoInput.value = planData.empNo || '';
                        if (remarkInput) remarkInput.value = planData.remark || '';

                        let planIdInput = document.getElementById('planId');
                        if (!planIdInput) {
                            planIdInput = document.createElement('input');
                            planIdInput.type = 'hidden';
                            planIdInput.id = 'planId';
                            planIdInput.name = 'planId';
                            productionPlanForm.appendChild(planIdInput);
                        }
                        planIdInput.value = planData.planId;

                        modalSubmitButton.addEventListener('click', switchToEditModeHandler);

                    })
                    .catch(error => {
                        console.error('Error fetching plan details:', error);
                        alert('생산 계획 상세 정보를 불러오는 중 오류가 발생했습니다: ' + error.message);
                        productionPlanRegisterModal.hide();
                    });
            } else {
                console.warn("planId가 없습니다. 상세 정보를 불러올 수 없습니다.");
                alert('상세 정보를 불러올 계획 번호가 없습니다.');
                productionPlanRegisterModal.hide();
            }
        }
    });

    productionPlanRegisterModalElement.addEventListener('hidden.bs.modal', function () {
        productionPlanForm.reset();
        resetSearchSections();
        const planIdInput = document.getElementById('planId');
        if (planIdInput) {
            planIdInput.remove();
        }
        modalSubmitButton.removeEventListener('click', registerModeSubmitHandler);
        modalSubmitButton.removeEventListener('click', saveModeSubmitHandler);
        modalSubmitButton.removeEventListener('click', switchToEditModeHandler);

        currentModalMode = 'register';
        setSearchButtonsDisabled(false);
        if (completionDateDiv) completionDateDiv.style.display = 'block';
        if (actualQtyDiv) actualQtyDiv.style.display = 'block';
    });

    // --- 기존 비고 모달 스크립트 (변경 없음) ---
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

    // --- 기존 작업지시 관련 코드 (변경 없음) ---
    if (createWorkOrderButton) {
        createWorkOrderButton.addEventListener('click', function() {
            console.log('[작업지시] 버튼 클릭됨');

            const selectedCheckboxes = document.querySelectorAll('#productionPlanTable .row-checkbox:checked');
            console.log('[작업지시] 선택된 체크박스 개수:', selectedCheckboxes.length);

            const planIdsToUpdate = [];
            let canProceed = true;
            let nonEligiblePlanSelected = false;

            selectedCheckboxes.forEach(checkbox => {
                const planId = checkbox.getAttribute('data-plan-id');
                const row = checkbox.closest('tr');
                const statusCell = row.cells[10];

                if (statusCell.textContent.trim() === '자재계획완료' || statusCell.textContent.trim() === '계획') {
                    planIdsToUpdate.push(planId);
                } else {
                    nonEligiblePlanSelected = true;
                }
            });

            if (planIdsToUpdate.length === 0) {
                if(nonEligiblePlanSelected || selectedCheckboxes.length > 0) {
                    alert('선택된 항목 중 "자재계획완료" 또는 "계획" 상태인 생산 계획이 없습니다.\n상태를 확인해주세요.');
                } else {
                    alert('작업 지시를 생성할 생산 계획을 선택해주세요.');
                }
                return;
            }
            
            if (nonEligiblePlanSelected) {
                if (!confirm('"자재계획완료" 또는 "계획" 상태가 아닌 항목이 선택에 포함되어 있습니다. 해당 항목을 제외하고 진행하시겠습니까?')) {
                    return;
                }
            }

            if (!confirm(planIdsToUpdate.length + '개의 생산 계획에 대해 작업 지시를 생성하시겠습니까?')) {
                return;
            }

            const apiUrl = 'productplan/api/issue-work-orders';

            fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ planIds: planIdsToUpdate })
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { 
                        throw new Error(err.message || '상태 변경 중 오류가 발생했습니다.'); 
                    });
                }
                return response.json();
            })
            .then(data => {
                alert(data.message || '선택된 생산 계획의 상태가 성공적으로 변경되었습니다.');
                location.reload();
            })
            .catch(error => {
                console.error('Error issuing work orders:', error);
                alert('오류: ' + error.message);
            });
        });
    }

    if (cancelWorkOrderButton) {
        cancelWorkOrderButton.addEventListener('click', function() {
            console.log('[작업지시 취소] 버튼 클릭됨');

            const selectedCheckboxes = document.querySelectorAll('#productionPlanTable .row-checkbox:checked');
            console.log('[작업지시 취소] 선택된 체크박스 개수:', selectedCheckboxes.length);

            const planIdsToCancel = [];
            let canProceed = true;
            let nonEligiblePlanSelected = false;

            selectedCheckboxes.forEach(checkbox => {
                const planId = checkbox.getAttribute('data-plan-id');
                const row = checkbox.closest('tr');
                const statusCell = row.cells[10];

                if (statusCell.textContent.trim() === '작업지시') {
                    planIdsToCancel.push(planId);
                } else {
                    nonEligiblePlanSelected = true;
                }
            });

            if (planIdsToCancel.length === 0) {
                if (nonEligiblePlanSelected || selectedCheckboxes.length > 0) {
                    alert('선택된 항목 중 "작업지시" 상태인 생산 계획이 없습니다.\n작업지시 상태인 계획만 취소할 수 있습니다.');
                } else {
                    alert('작업 지시를 취소할 생산 계획을 선택해주세요.');
                }
                return;
            }

            if (nonEligiblePlanSelected) {
                if (!confirm('"작업지시" 상태가 아닌 항목이 선택에 포함되어 있습니다. 해당 항목을 제외하고 진행하시겠습니까?')) {
                    return;
                }
            }

            if (!confirm(planIdsToCancel.length + '개의 생산 계획에 대해 작업 지시를 취소하시겠습니까?\n취소된 계획은 "자재계획완료" 상태로 되돌아갑니다.')) {
                return;
            }

            const apiUrl = '/productplan/api/cancel-work-orders';

            fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ planIds: planIdsToCancel })
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { 
                        throw new Error(err.message || '작업지시 취소 중 오류가 발생했습니다.'); 
                    });
                }
                return response.json();
            })
            .then(data => {
                alert(data.message || '선택된 생산 계획의 작업지시가 성공적으로 취소되었습니다.');
                location.reload();
            })
            .catch(error => {
                console.error('Error canceling work orders:', error);
                alert('오류: ' + error.message);
            });
        });
    }

}); // DOMContentLoaded 이벤트 리스너 끝