// productplan_list.js - 완전한 버전

// 전역 변수
let employeeSearchDebounceTimer;
let itemSearchDebounceTimer;
const EMPLOYEE_SEARCH_DEBOUNCE_DELAY = 300;
const ITEM_SEARCH_DEBOUNCE_DELAY = 300;

document.addEventListener('DOMContentLoaded', function() {
    // --- 1. 주요 DOM 요소 참조 ---
    const productionPlanRegisterModalElement = document.getElementById('productionPlanRegisterModal');
    const productionPlanRegisterModal = new bootstrap.Modal(productionPlanRegisterModalElement);
    const productionPlanForm = document.getElementById('productionPlanForm');

    // 모달 제목과 제출 버튼 참조
    const modalTitle = document.getElementById('productionPlanRegisterModalLabel');
    const modalSubmitButton = document.getElementById('modalSubmitButton');

    // 검색 관련 요소들
    const itemSearchInput = document.getElementById('itemSearchInput');
    const itemSearchDropdown = document.getElementById('itemSearchDropdown');
    const itemCodeInput = document.getElementById('itemCode');
    
    const employeeSearchInput = document.getElementById('employeeSearchInput');
    const employeeSearchDropdown = document.getElementById('employeeSearchDropdown');
    const modalEmpNoInput = document.getElementById('modalEmpNo');

    // 모달 폼 필드들
    const formFields = productionPlanForm.querySelectorAll('input:not([type="hidden"]), select, textarea');

    // 모달 내 개별 필드 참조
    const modalPlanQtyInput = document.getElementById('modalPlanQty');
    const modalStartDateInput = document.getElementById('modalStartDate');
    const modalDueDateInput = document.getElementById('modalDueDate');
    const modalCompletionDateInput = document.getElementById('modalCompletionDate');
    const modalActualQtyInput = document.getElementById('modalActualQty');
    const modalStatusSelect = document.getElementById('modalStatus');
    const remarkInput = document.getElementById('remark');

    const completionDateDiv = modalCompletionDateInput.closest('.mb-3');
    const actualQtyDiv = modalActualQtyInput.closest('.mb-3');
    
    // 버튼들
    const createWorkOrderButton = document.getElementById('createWorkOrderButton');
    const cancelWorkOrderButton = document.getElementById('cancelWorkOrderButton');
    const mrpCalcBtn = document.querySelector('.mrp-calc-btn');

    let currentModalMode = 'register';

    // --- 2. 유틸리티 함수들 ---
    
    // 상태별 알림 함수
    function showStatusAlert(type, message) {
        // 기존 알림 제거
        const existing = document.querySelector('.status-alert');
        if (existing) existing.remove();
        
        const alertClass = type === 'success' ? 'alert-success' : 
                          type === 'warning' ? 'alert-warning' : 'alert-danger';
        const icon = type === 'success' ? 'bi-check-circle' : 
                    type === 'warning' ? 'bi-exclamation-triangle' : 'bi-x-circle';
        
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert ${alertClass} alert-dismissible fade show status-alert`;
        alertDiv.style.cssText = 'position: fixed; top: 20px; left: 50%; transform: translateX(-50%); z-index: 9999; min-width: 500px; max-width: 700px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);';
        alertDiv.innerHTML = `
            <div class="d-flex align-items-start">
                <i class="bi ${icon} me-3" style="font-size: 1.1rem; margin-top: 2px;"></i>
                <div style="white-space: pre-line; flex: 1;">${message}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        
        document.body.appendChild(alertDiv);
        
        if (type === 'success') {
            setTimeout(() => alertDiv.remove(), 4000);
        }
    }

    // 버튼별 상태 검사 함수
    function validateSelection(requiredStatuses, actionName) {
        const selectedCheckboxes = document.querySelectorAll('.row-checkbox:checked');
        
        if (selectedCheckboxes.length === 0) {
            showStatusAlert('warning', `${actionName}을 할 생산계획을 선택해주세요.`);
            return { valid: false };
        }
        
        const validPlans = [];
        const invalidPlans = [];
        
        selectedCheckboxes.forEach(checkbox => {
            const planId = checkbox.getAttribute('data-plan-id');
            const status = checkbox.getAttribute('data-status');
            const itemName = checkbox.getAttribute('data-item-name');
            
            if (requiredStatuses.includes(status)) {
                validPlans.push({ planId, itemName, status });
            } else {
                invalidPlans.push({ planId, itemName, status });
            }
        });
        
        return { valid: invalidPlans.length === 0, validPlans, invalidPlans, requiredStatuses };
    }

    // --- 3. 드롭다운 관련 함수들 ---
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

    function resetSearchSections() {
        hideAllDropdowns();
        if (itemSearchInput) itemSearchInput.value = '';
        if (itemCodeInput) itemCodeInput.value = '';
        if (employeeSearchInput) employeeSearchInput.value = '';
        if (modalEmpNoInput) modalEmpNoInput.value = '';
    }

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

    function setSearchButtonsDisabled(disabled) {
        if (itemSearchInput) itemSearchInput.disabled = disabled;
        if (employeeSearchInput) employeeSearchInput.disabled = disabled;
        
        if (disabled) {
            hideAllDropdowns();
        }
    }

    // --- 4. 검색 API 함수들 ---
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

    function selectItem(itemName, itemCode) {
        if (itemSearchInput) itemSearchInput.value = itemName;
        if (itemCodeInput) itemCodeInput.value = itemCode;
        hideDropdown(itemSearchDropdown);
    }

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

    function selectEmployee(empName, empNo) {
        if (employeeSearchInput) employeeSearchInput.value = empName;
        if (modalEmpNoInput) modalEmpNoInput.value = empNo;
        hideDropdown(employeeSearchDropdown);
    }

    // --- 5. AJAX 데이터 전송 함수들 ---
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

    // --- 6. 모달 이벤트 핸들러들 ---
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

    // --- 7. 메인 버튼 이벤트 리스너들 ---

    // 자재소요량 계산 버튼
    if (mrpCalcBtn) {
        mrpCalcBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            const result = validateSelection(['계획'], '자재소요량 계산');
            
            if (!result.valid) {
                if (result.invalidPlans && result.invalidPlans.length > 0) {
                    const invalidList = result.invalidPlans.map(p => 
                        `• ${p.itemName} (${p.planId}) - 현재: ${p.status}`
                    ).join('\n');
                    
                    showStatusAlert('error', 
                        `다음 생산계획들은 '계획' 상태가 아니어서 자재소요량 계산을 할 수 없습니다:\n\n${invalidList}\n\n💡 '계획' 상태인 항목만 선택해주세요.`
                    );
                }
                return;
            }
            
            // 모든 검증 통과
            const planIds = result.validPlans.map(p => p.planId);
            const planIdsParam = planIds.join(',');
            
            showStatusAlert('success', 
                `✅ ${result.validPlans.length}개 계획의 자재소요량을 계산합니다.\n잠시 후 MRP 페이지로 이동합니다.`
            );
            
            setTimeout(() => {
                window.location.href = `/mrp?selectedPlans=${planIdsParam}`;
            }, 2000);
        });
    }

    // 작업지시 생성 버튼
    if (createWorkOrderButton) {
        createWorkOrderButton.addEventListener('click', function(e) {
            e.preventDefault();
            
            const result = validateSelection(['자재계획완료'], '작업지시 생성');
            
            if (!result.valid) {
                if (result.invalidPlans && result.invalidPlans.length > 0) {
                    const invalidList = result.invalidPlans.map(p => 
                        `• ${p.itemName} (${p.planId}) - 현재: ${p.status}`
                    ).join('\n');
                    
                    showStatusAlert('error', 
                        `다음 생산계획들은 '자재계획완료' 상태가 아니어서 작업지시를 생성할 수 없습니다:\n\n${invalidList}\n\n💡 먼저 자재소요량 계산을 완료해주세요.`
                    );
                }
                return;
            }
            
            // 확인 후 진행
            if (!confirm(`${result.validPlans.length}개의 생산 계획에 대해 작업 지시를 생성하시겠습니까?`)) {
                return;
            }

            const planIds = result.validPlans.map(p => p.planId);
            const apiUrl = '/productplan/api/issue-work-orders';

            fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ planIds: planIds })
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { 
                        throw new Error(err.message || '작업지시 생성 중 오류가 발생했습니다.'); 
                    });
                }
                return response.json();
            })
            .then(data => {
                showStatusAlert('success', 
                    `🔧 ${planIds.length}개 계획의 작업지시가 생성되었습니다!\n\n${data.message || '작업지시가 성공적으로 생성되었습니다.'}`
                );
                setTimeout(() => location.reload(), 2000);
            })
            .catch(error => {
                console.error('Error issuing work orders:', error);
                showStatusAlert('error', '작업지시 생성 중 오류가 발생했습니다: ' + error.message);
            });
        });
    }

    // 작업지시 취소 버튼
    if (cancelWorkOrderButton) {
        cancelWorkOrderButton.addEventListener('click', function(e) {
            e.preventDefault();
            
            const result = validateSelection(['작업지시'], '작업지시 취소');
            
            if (!result.valid) {
                if (result.invalidPlans && result.invalidPlans.length > 0) {
                    const invalidList = result.invalidPlans.map(p => 
                        `• ${p.itemName} (${p.planId}) - 현재: ${p.status}`
                    ).join('\n');
                    
                    showStatusAlert('error', 
                        `다음 생산계획들은 '작업지시' 상태가 아니어서 취소할 수 없습니다:\n\n${invalidList}\n\n💡 '작업지시' 상태인 항목만 선택해주세요.`
                    );
                }
                return;
            }
            
            // 확인 후 진행
            if (!confirm(`${result.validPlans.length}개의 생산 계획에 대해 작업 지시를 취소하시겠습니까?\n취소된 계획은 "자재계획완료" 상태로 되돌아갑니다.`)) {
                return;
            }

            const planIds = result.validPlans.map(p => p.planId);
            const apiUrl = '/productplan/api/cancel-work-orders';

            fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ planIds: planIds })
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
                showStatusAlert('success', 
                    `✅ ${planIds.length}개 계획의 작업지시가 취소되었습니다!\n\n${data.message || '작업지시가 성공적으로 취소되었습니다.'}`
                );
                setTimeout(() => location.reload(), 2000);
            })
            .catch(error => {
                console.error('Error canceling work orders:', error);
                showStatusAlert('error', '작업지시 취소 중 오류가 발생했습니다: ' + error.message);
            });
        });
    }

    // --- 8. 검색 이벤트 리스너들 ---
    
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

        itemSearchInput.addEventListener('blur', function() {
            setTimeout(() => {
                hideDropdown(itemSearchDropdown);
            }, 200);
        });

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

        employeeSearchInput.addEventListener('blur', function() {
            setTimeout(() => {
                hideDropdown(employeeSearchDropdown);
            }, 200);
        });

        employeeSearchInput.addEventListener('focus', function() {
            if (this.value.trim().length >= 2 && employeeSearchDropdown.innerHTML.trim() !== '') {
                showDropdown(employeeSearchDropdown);
            }
        });
    }

    // 드롭다운 클릭 이벤트들
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

    // --- 9. 모달 이벤트 리스너들 ---
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

    // --- 10. 비고 모달 스크립트 ---
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

    // --- 11. 상태 가이드 기능 ---
    document.addEventListener('keydown', function(e) {
        if (e.key === 'F1') {
            e.preventDefault();
            showStatusGuide();
        }
    });

    function showStatusGuide() {
        const existing = document.querySelector('.status-guide');
        if (existing) {
            existing.remove();
            return;
        }

        const guideDiv = document.createElement('div');
        guideDiv.className = 'alert alert-info alert-dismissible fade show status-guide';
        guideDiv.style.cssText = 'position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 9999; min-width: 500px; max-width: 700px; box-shadow: 0 8px 24px rgba(0,0,0,0.15);';
        guideDiv.innerHTML = `
            <div class="text-center mb-3">
                <h5 class="mb-0">📋 생산계획 상태별 작업 가이드</h5>
            </div>
            <div class="row text-center">
                <div class="col-md-6 mb-3">
                    <div class="border rounded p-3 h-100">
                        <span class="badge bg-primary mb-2" style="font-size: 1rem;">📝 계획</span>
                        <p class="mb-1"><strong>가능한 작업:</strong></p>
                        <p class="text-muted mb-0">• 자재소요량 계산<br>• 생산계획 수정/삭제</p>
                    </div>
                </div>
                <div class="col-md-6 mb-3">
                    <div class="border rounded p-3 h-100">
                        <span class="badge bg-success mb-2" style="font-size: 1rem;">✅ 자재계획완료</span>
                        <p class="mb-1"><strong>가능한 작업:</strong></p>
                        <p class="text-muted mb-0">• 작업지시 생성<br>• 생산계획 수정</p>
                    </div>
                </div>
                <div class="col-md-6 mb-3">
                    <div class="border rounded p-3 h-100">
                        <span class="badge bg-warning text-dark mb-2" style="font-size: 1rem;">🔧 작업지시</span>
                        <p class="mb-1"><strong>가능한 작업:</strong></p>
                        <p class="text-muted mb-0">• 작업지시 취소<br>• 생산 진행 관리</p>
                    </div>
                </div>
                <div class="col-md-6 mb-3">
                    <div class="border rounded p-3 h-100">
                        <span class="badge bg-info text-dark mb-2" style="font-size: 1rem;">🎉 완료</span>
                        <p class="mb-1"><strong>가능한 작업:</strong></p>
                        <p class="text-muted mb-0">• 완료된 작업<br>• 이력 조회만 가능</p>
                    </div>
                </div>
            </div>
            <div class="text-center mt-3">
                <small class="text-muted">💡 각 상태에서는 해당하는 작업만 수행할 수 있습니다.</small>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(guideDiv);
        
        // 5초 후 자동 닫기
        setTimeout(() => {
            if (guideDiv.parentNode) {
                guideDiv.remove();
            }
        }, 8000);
    }

}); // DOMContentLoaded 이벤트 리스너 끝

// --- 12. 추가 유틸리티 함수들 (전역) ---

// 페이지 로드 후 상태별 행 색상 업데이트
window.addEventListener('load', function() {
    updateRowColors();
});

function updateRowColors() {
    const rows = document.querySelectorAll('#productionPlanTable tbody tr');
    
    rows.forEach(row => {
        const statusCell = row.querySelector('td:nth-child(11)'); // 상태 컬럼
        if (statusCell) {
            const statusText = statusCell.textContent.trim();
            
            // 기존 클래스 제거
            row.classList.remove('table-light', 'table-success', 'table-warning', 'table-info', 'table-danger');
            
            // 상태별 색상 적용
            switch(statusText) {
                case '계획':
                    row.classList.add('table-light');
                    break;
                case '자재계획완료':
                    row.classList.add('table-success');
                    break;
                case '작업지시':
                    row.classList.add('table-warning');
                    break;
                case '완료':
                    row.classList.add('table-info');
                    break;
                case '취소':
                    row.classList.add('table-danger');
                    break;
            }
        }
    });
}

// CSS 스타일을 동적으로 추가
const style = document.createElement('style');
style.textContent = `
    .status-alert, .status-guide {
        border-radius: 8px;
        border-width: 2px;
    }
    
    .table-light { background-color: #f8f9fa !important; }
    .table-success { background-color: #d1f2d1 !important; }
    .table-warning { background-color: #fff3cd !important; }
    .table-info { background-color: #d1ecf1 !important; }
    .table-danger { background-color: #f8d7da !important; }
    
    .search-dropdown {
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        background: white;
        border: 1px solid #ddd;
        border-radius: 4px;
        max-height: 200px;
        overflow-y: auto;
        z-index: 1000;
        display: none;
    }
    
    .search-dropdown.show {
        display: block;
    }
    
    .search-item {
        padding: 10px;
        cursor: pointer;
        border-bottom: 1px solid #eee;
    }
    
    .search-item:hover {
        background-color: #f5f5f5;
    }
    
    .search-item:last-child {
        border-bottom: none;
    }
    
    .search-loading, .search-no-results {
        padding: 10px;
        text-align: center;
        color: #666;
        font-style: italic;
    }
    
    .item-info, .employee-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    
    .item-name, .emp-name {
        font-weight: 500;
    }
    
    .item-code, .emp-no {
        color: #666;
        font-size: 0.9em;
    }
    
    .search-input-container {
        position: relative;
    }
    
    /* F1 키 안내 */
    body::after {
        content: "💡 F1키를 눌러 상태별 가이드를 확인하세요";
        position: fixed;
        bottom: 10px;
        right: 10px;
        background: rgba(0,0,0,0.7);
        color: white;
        padding: 5px 10px;
        border-radius: 4px;
        font-size: 0.75rem;
        z-index: 1000;
        pointer-events: none;
    }
`;
document.head.appendChild(style);