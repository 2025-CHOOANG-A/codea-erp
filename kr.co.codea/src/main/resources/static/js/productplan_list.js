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
    const formFields = productionPlanForm ? productionPlanForm.querySelectorAll('input:not([type="hidden"]), select, textarea') : [];

    // 모달 내 개별 필드 참조
    const modalPlanQtyInput = document.getElementById('modalPlanQty');
    const modalStartDateInput = document.getElementById('modalStartDate');
    const modalDueDateInput = document.getElementById('modalDueDate');
    const modalCompletionDateInput = document.getElementById('modalCompletionDate');
    const modalActualQtyInput = document.getElementById('modalActualQty');
    const modalStatusSelect = document.getElementById('modalStatus');
    const remarkInput = document.getElementById('remark');

    const completionDateDiv = modalCompletionDateInput ? modalCompletionDateInput.closest('.mb-3') : null;
    const actualQtyDiv = modalActualQtyInput ? modalActualQtyInput.closest('.mb-3') : null;
    
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
                          type === 'warning' ? 'alert-warning' : 
                          type === 'info' ? 'alert-info' : 'alert-danger';
        const icon = type === 'success' ? 'bi-check-circle' : 
                    type === 'warning' ? 'bi-exclamation-triangle' : 
                    type === 'info' ? 'bi-info-circle' : 'bi-x-circle';
        
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
        
        return { valid: invalidPlans.length === 0, validPlans, invalidPlans };
    }

    // --- 3. 자재 소요량 확인 및 작업지시 생성 ---
    
    /**
     * 자재 소요량 확인 후 작업지시 생성
     */
    function checkMaterialRequirementsAndIssueWorkOrder(planIds) {
        showStatusAlert('info', '자재 소요량을 확인하고 있습니다...');
		console.log('=== 자재 소요량 확인 시작 ===');
		 console.log('전달된 planIds:', planIds);
		 console.log('planIds 타입:', typeof planIds);
		 console.log('planIds 배열 여부:', Array.isArray(planIds));
		 

		 const requestData = { planIds: planIds };
		 console.log('서버로 전송할 데이터:', JSON.stringify(requestData, null, 2));
        
        fetch('/productplan/api/check-material-requirements', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ planIds: planIds })
        })
        .then(response => {
			console.log('=== 서버 응답 정보 ===');
			  console.log('Response status:', response.status);
			  console.log('Response statusText:', response.statusText);
			  console.log('Response headers:', response.headers);
			  console.log('Response ok:', response.ok);
			  
            if (!response.ok) {
                return response.json().then(err => { 
                    throw new Error(err.message || '자재 소요량 확인 중 오류가 발생했습니다.'); 
                });
            }
            return response.json();
        })
        .then(data => {
			console.log('서버 응답:', data);

			
            if (data.success) {
				// plans 배열이 없거나 비어있는 경우 처리
				    if (!data.plans || !Array.isArray(data.plans) || data.plans.length === 0) {
				        console.warn('자재 소요량 데이터가 없습니다. 바로 작업지시를 생성합니다.');
				        
				        // 사용자에게 확인받고 바로 작업지시 생성
				        if (confirm(`선택한 생산계획(${planIds.join(', ')})의 자재 소요량 정보를 찾을 수 없습니다.\n\n그래도 작업지시를 생성하시겠습니까?`)) {
				            proceedWithWorkOrder(planIds);
				        }
				        return;
						}
				
				
				
				
                if (data.hasAnyShortage) {
                    // 자재 부족이 있으면 모달로 상세 정보 표시
                    showMaterialRequirementModal(data.plans, false);
                } else {
                    // 자재가 충분하면 바로 작업지시 생성
                    showMaterialRequirementModal(data.plans, true);
                }
            } else {
                showStatusAlert('error', data.message || '자재 소요량 확인에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error checking material requirements:', error);
            showStatusAlert('error', '자재 소요량 확인 중 오류가 발생했습니다: ' + error.message);
        });
    }

    /**
     * 자재 소요량 모달 표시
     */
    function showMaterialRequirementModal(plans, canProceed) {
        // 기존 모달이 있으면 제거
        const existingModal = document.getElementById('materialRequirementModal');
        if (existingModal) {
            existingModal.remove();
        }
        
        // 모달 HTML 생성
        const modalHtml = `
            <div class="modal fade" id="materialRequirementModal" tabindex="-1" aria-labelledby="materialRequirementModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-xl">
                    <div class="modal-content">
                        <div class="modal-header ${canProceed ? 'bg-success' : 'bg-warning'} text-white">
                            <h5 class="modal-title" id="materialRequirementModalLabel">
                                <i class="bi ${canProceed ? 'bi-check-circle' : 'bi-exclamation-triangle'}"></i>
                                자재 소요량 확인
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            ${generateMaterialRequirementContent(plans, canProceed)}
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                            ${canProceed ? 
                                '<button type="button" class="btn btn-success" id="proceedWorkOrderBtn"><i class="bi bi-gear"></i> 작업지시 생성</button>' :
                                '<button type="button" class="btn btn-warning" disabled><i class="bi bi-exclamation-triangle"></i> 재고 부족으로 작업지시 불가</button>'
                            }
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // 모달을 body에 추가
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        
        // 모달 표시
        const modal = new bootstrap.Modal(document.getElementById('materialRequirementModal'));
        modal.show();
        
        // 작업지시 생성 버튼 이벤트
        if (canProceed) {
            document.getElementById('proceedWorkOrderBtn').addEventListener('click', function() {
                modal.hide();
                const planIds = plans.map(p => p.planId);
                proceedWithWorkOrder(planIds);
            });
        }
        
        // 모달이 닫힐 때 DOM에서 제거
        document.getElementById('materialRequirementModal').addEventListener('hidden.bs.modal', function() {
            this.remove();
        });
    }

    /**
     * 자재 소요량 모달 내용 생성
     */
    function generateMaterialRequirementContent(plans, canProceed) {
        let content = `
            <div class="alert ${canProceed ? 'alert-success' : 'alert-warning'} mb-4">
                <h6 class="alert-heading mb-2">
                    <i class="bi ${canProceed ? 'bi-check-circle' : 'bi-exclamation-triangle'}"></i>
                    ${canProceed ? '자재 준비 완료' : '자재 재고 부족'}
                </h6>
                <p class="mb-0">
                    ${canProceed ? 
                        '모든 필요 자재의 재고가 충분합니다. 작업지시를 생성하면 해당 자재들이 생산공장으로 가출고됩니다.' :
                        '일부 자재의 재고가 부족합니다. 자재 입고 후 작업지시를 생성해주세요.'
                    }
                </p>
            </div>
        `;
        
        plans.forEach((plan, index) => {
            const badgeClass = plan.hasShortage ? 'bg-danger' : 'bg-success';
            const statusIcon = plan.hasShortage ? 'bi-x-circle' : 'bi-check-circle';
            
            content += `
                <div class="card mb-3">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h6 class="mb-0">
                            <i class="bi bi-box"></i>
                            ${plan.itemName} (${plan.planId})
                            <span class="badge ${badgeClass} ms-2">
                                <i class="bi ${statusIcon}"></i>
                                ${plan.hasShortage ? '재고부족' : '준비완료'}
                            </span>
                        </h6>
                        <small class="text-muted">계획수량: ${plan.planQty.toLocaleString()}</small>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-sm mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>자재코드</th>
                                        <th>자재명</th>
                                        <th class="text-end">필요수량</th>
                                        <th class="text-end">가용재고</th>
                                        <th class="text-end">부족수량</th>
                                        <th>단위</th>
                                        <th class="text-center">상태</th>
                                    </tr>
                                </thead>
                                <tbody>
            `;
            
            plan.materials.forEach(material => {
                const statusBadge = material.sufficient ? 
                    '<span class="badge bg-success"><i class="bi bi-check"></i> 충분</span>' :
                    '<span class="badge bg-danger"><i class="bi bi-x"></i> 부족</span>';
                    
                content += `
                    <tr class="${material.sufficient ? '' : 'table-warning'}">
                        <td><code>${material.itemCode}</code></td>
                        <td>${material.itemName}</td>
                        <td class="text-end fw-bold">${material.requiredQty.toLocaleString()}</td>
                        <td class="text-end">${material.availableQty.toLocaleString()}</td>
                        <td class="text-end ${material.shortage > 0 ? 'text-danger fw-bold' : ''}">${material.shortage.toLocaleString()}</td>
                        <td>${material.unit}</td>
                        <td class="text-center">${statusBadge}</td>
                    </tr>
                `;
            });
            
            content += `
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            `;
        });
        
        return content;
    }

    /**
     * 작업지시 생성 진행 (자재 가출고 포함)
     */
    function proceedWithWorkOrder(planIds) {
        showStatusAlert('info', '작업지시를 생성하고 자재를 가출고 처리하고 있습니다...');
        
        fetch('/productplan/api/issue-work-orders', {
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
            if (data.success) {
                showStatusAlert('success', 
                    `🔧 ${data.updatedCount || planIds.length}개 계획의 작업지시가 생성되었습니다!\n\n자재가 생산공장으로 가출고 처리되었습니다.`
                );
                setTimeout(() => location.reload(), 2000);
            } else {
                showStatusAlert('error', data.message || '작업지시 생성에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error issuing work orders:', error);
            showStatusAlert('error', '작업지시 생성 중 오류가 발생했습니다: ' + error.message);
        });
    }

    // --- 4. 드롭다운 관련 함수들 ---
    function showDropdown(dropdown) {
        if (dropdown) dropdown.classList.add('show');
    }

    function hideDropdown(dropdown) {
        if (dropdown) dropdown.classList.remove('show');
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

    // --- 5. 검색 API 함수들 ---
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

    // --- 6. AJAX 데이터 전송 함수들 ---
    async function sendProductPlanData(url, method, data) {
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
                if (productionPlanRegisterModal) productionPlanRegisterModal.hide();
                location.reload();
            } else {
                console.error(`${method === 'POST' ? '등록' : '수정'} 실패: ` + result.message);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('오류 발생: ' + error.message);
        }
    }

    // --- 7. 모달 이벤트 핸들러들 ---
    const registerModeSubmitHandler = async function() {
        if (!productionPlanForm) return;
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
        if (!productionPlanForm) return;
        const formData = new FormData(productionPlanForm);
        const data = Object.fromEntries(formData.entries());
        data.planId = planId;
        const url = `/productplan/${planId}`;
        await sendProductPlanData(url, 'PUT', data);
    };

    const switchToEditModeHandler = function() {
        if (!modalSubmitButton) return;
        modalSubmitButton.removeEventListener('click', switchToEditModeHandler);
        setFormFieldsReadOnly(false);
        setSearchButtonsDisabled(false);
        if (modalTitle) modalTitle.textContent = '생산 계획 수정';
        modalSubmitButton.textContent = '저장';
        modalSubmitButton.classList.remove('btn-warning');
        modalSubmitButton.classList.add('btn-success');
        currentModalMode = 'edit';
        modalSubmitButton.addEventListener('click', saveModeSubmitHandler);
    };

    // --- 8. 메인 버튼 이벤트 리스너들 ---

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

    // 작업지시 생성 버튼 (자재 확인 포함)
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
            
            const planIds = result.validPlans.map(p => p.planId);
            
            // 자재 소요량 확인 후 작업지시 생성
            checkMaterialRequirementsAndIssueWorkOrder(planIds);
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
            
            if (!confirm(`${result.validPlans.length}개의 생산 계획에 대해 작업 지시를 취소하시겠습니까?\n취소된 계획은 "자재계획완료" 상태로 되돌아갑니다.`)) {
                return;
            }

            const planIds = result.validPlans.map(p => p.planId);

            fetch('/productplan/api/cancel-work-orders', {
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
                    `✅ ${data.canceledCount || planIds.length}개 계획의 작업지시가 취소되었습니다!\n\n자재 할당이 해제되고 재고가 복구되었습니다.`
                );
                setTimeout(() => location.reload(), 2000);
            })
            .catch(error => {
                console.error('Error canceling work orders:', error);
                showStatusAlert('error', '작업지시 취소 중 오류가 발생했습니다: ' + error.message);
            });
        });
    }

    // --- 9. 단일 계획 자재소요량 조회 (상태 클릭시) ---
    window.showMaterialRequirementsForPlan = function(planId) {
		console.log('=== 자재 소요량 조회 시작 ===');
		console.log('Plan ID:', planId);
		
		  fetch(`/productplan/api/material-requirements/${planId}`)
		  .then(response => {
		      console.log('응답 상태:', response.status);
		      console.log('응답 헤더:', response.headers);
		      return response.json();
		  })
        .then(data => {
			console.log('=== 응답 데이터 전체 ===');
			       console.log(JSON.stringify(data, null, 2));
			       
			       console.log('success 값:', data.success);
			       console.log('materials 배열:', data.materials);
			       console.log('materials 타입:', typeof data.materials);
			       console.log('materials 길이:', data.materials ? data.materials.length : 'undefined');
				   
            if (data.success) {
                showSinglePlanMaterialModal(planId, data.materials);
            } else {
                showStatusAlert('error', data.message || '자재 소요량 조회에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error fetching material requirements:', error);
            showStatusAlert('error', '자재 소요량 조회 중 오류가 발생했습니다.');
        });
    };

    /**
     * 단일 계획의 자재소요량 모달
     */
    function showSinglePlanMaterialModal(planId, materials) {
        const existingModal = document.getElementById('singleMaterialModal');
        if (existingModal) {
            existingModal.remove();
        }
        
        const hasShortage = materials.some(m => !m.sufficient);
        
        const modalHtml = `
            <div class="modal fade" id="singleMaterialModal" tabindex="-1" aria-labelledby="singleMaterialModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header bg-info text-white">
                            <h5 class="modal-title" id="singleMaterialModalLabel">
                                <i class="bi bi-list-check"></i>
                                자재 소요량 - ${planId}
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead class="table-light">
                                        <tr>
                                            <th>자재코드</th>
                                            <th>자재명</th>
                                            <th class="text-end">필요수량</th>
                                            <th class="text-end">가용재고</th>
                                            <th class="text-end">부족수량</th>
                                            <th>단위</th>
                                            <th class="text-center">상태</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        ${materials.map(material => `
                                            <tr class="${material.sufficient ? '' : 'table-warning'}">
                                                <td><code>${material.itemCode}</code></td>
                                                <td>${material.itemName}</td>
                                                <td class="text-end fw-bold">${material.requiredQty.toLocaleString()}</td>
                                                <td class="text-end">${material.availableQty.toLocaleString()}</td>
                                                <td class="text-end ${material.shortage > 0 ? 'text-danger fw-bold' : ''}">${material.shortage.toLocaleString()}</td>
                                                <td>${material.unit}</td>
                                                <td class="text-center">
                                                    ${material.sufficient ? 
                                                        '<span class="badge bg-success"><i class="bi bi-check"></i> 충분</span>' :
                                                        '<span class="badge bg-danger"><i class="bi bi-x"></i> 부족</span>'
                                                    }
                                                </td>
                                            </tr>
                                        `).join('')}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        
        const modal = new bootstrap.Modal(document.getElementById('singleMaterialModal'));
        modal.show();
        
        document.getElementById('singleMaterialModal').addEventListener('hidden.bs.modal', function() {
            this.remove();
        });
    }

    // --- 10. 검색 이벤트 리스너들 ---
    
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

}); // DOMContentLoaded 이벤트 리스너 끝

// --- CSS 스타일을 동적으로 추가 ---
const style = document.createElement('style');
style.textContent = `
    .status-alert {
        border-radius: 8px;
        border-width: 2px;
    }
    
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
`;
document.head.appendChild(style);