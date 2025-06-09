/**
 * 생산실적관리 전용 스크립트
 * productplan_list.js와 완전히 분리된 독립형 파일
 */

// 전역 변수
window.ProductActualManager = {
    
    /**
     * 작업 시작 확인 및 처리
     */
    confirmStartWork: function(planId, itemName) {
        return new Promise((resolve, reject) => {
            const confirmMessage = `${itemName} (${planId})의 작업을 시작하시겠습니까?\n\n작업이 시작되면:
• 상태가 '진행중'으로 변경됩니다
• 실제 생산 시작일이 현재 시각으로 기록됩니다
• 생산실적을 등록할 수 있게 됩니다`;
            
            if (confirm(confirmMessage)) {
                this.showProgressMessage('작업 시작 처리 중...');
                resolve();
            } else {
                reject('사용자 취소');
            }
        });
    },
    
    /**
     * 작업 종료 확인 및 처리
     */
    confirmEndWork: function(planId, itemName, actualQty, planQty) {
        return new Promise((resolve, reject) => {
            const completionRate = planQty > 0 ? ((actualQty / planQty) * 100).toFixed(1) : 0;
            const confirmMessage = `${itemName} (${planId})의 작업을 종료하시겠습니까?\n\n현재 진행률: ${completionRate}%
• 계획수량: ${planQty?.toLocaleString() || 0} EA
• 실적수량: ${actualQty?.toLocaleString() || 0} EA\n\n작업 종료 시:
• 상태가 '완료'로 변경됩니다
• 실제 완료일이 현재 시각으로 기록됩니다
• 완제품이 자동으로 완제품창고에 입고됩니다
• 더 이상 실적을 등록할 수 없습니다`;
            
            if (confirm(confirmMessage)) {
                this.showProgressMessage('작업 종료 및 완제품 입고 처리 중...');
                resolve();
            } else {
                reject('사용자 취소');
            }
        });
    },
    
    /**
     * 진행 메시지 표시
     */
    showProgressMessage: function(message) {
        const existingProgress = document.querySelector('.progress-alert');
        if (existingProgress) {
            existingProgress.remove();
        }
        
        const progressDiv = document.createElement('div');
        progressDiv.className = 'alert alert-info progress-alert d-flex align-items-center';
        progressDiv.style.cssText = `
            position: fixed; 
            top: 50%; 
            left: 50%; 
            transform: translate(-50%, -50%); 
            z-index: 9999; 
            min-width: 400px; 
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
        `;
        progressDiv.innerHTML = `
            <div class="spinner-border spinner-border-sm me-3" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <div>${message}</div>
        `;
        
        document.body.appendChild(progressDiv);
        return progressDiv;
    },
    
    /**
     * 진행 메시지 제거
     */
    hideProgressMessage: function() {
        const progressAlert = document.querySelector('.progress-alert');
        if (progressAlert) {
            progressAlert.remove();
        }
    },
    
    /**
     * 상태별 뱃지 클래스 반환
     */
    getStatusBadgeClass: function(status) {
        const statusClasses = {
            '작업지시': 'bg-info text-dark',
            '진행중': 'bg-warning text-dark',
            '완료': 'bg-success',
            '취소': 'bg-danger'
        };
        return statusClasses[status] || 'bg-secondary';
    },
    
    /**
     * 진행률 계산 및 프로그레스 바 업데이트
     */
    updateProgressBar: function(actualQty, planQty, progressBarElement, progressTextElement) {
        const rate = planQty > 0 ? ((actualQty / planQty) * 100) : 0;
        const roundedRate = Math.round(rate * 10) / 10;
        
        if (progressBarElement) {
            progressBarElement.style.width = `${Math.min(roundedRate, 100)}%`;
            
            progressBarElement.className = 'progress-bar';
            if (roundedRate >= 100) {
                progressBarElement.classList.add('bg-success');
            } else if (roundedRate >= 80) {
                progressBarElement.classList.add('bg-info');
            } else if (roundedRate >= 50) {
                progressBarElement.classList.add('bg-warning');
            } else {
                progressBarElement.classList.add('bg-danger');
            }
        }
        
        if (progressTextElement) {
            progressTextElement.textContent = `${roundedRate}%`;
        }
        
        return roundedRate;
    },
    
    /**
     * 수량 포맷팅 (천단위 콤마)
     */
    formatQuantity: function(qty) {
        return qty ? parseInt(qty).toLocaleString() : '0';
    },
    
    /**
     * 실적 등록 폼 유효성 검사
     */
    validateActualForm: function(formData) {
        const errors = [];
        
        if (!formData.planId) {
            errors.push('생산계획을 선택해주세요.');
        }
        
        if (!formData.actualDate) {
            errors.push('실적 일자를 입력해주세요.');
        }
        
        if (!formData.actualQty || formData.actualQty < 0) {
            errors.push('생산수량을 올바르게 입력해주세요.');
        }
        
        if (formData.defectQty && formData.defectQty < 0) {
            errors.push('불량수량은 0 이상이어야 합니다.');
        }
        
        const actualDate = new Date(formData.actualDate);
        const today = new Date();
        today.setHours(23, 59, 59, 999);
        
        if (actualDate > today) {
            errors.push('실적 일자는 미래 날짜가 될 수 없습니다.');
        }
        
        return {
            isValid: errors.length === 0,
            errors: errors
        };
    },
    
    /**
     * 실적 등록 후 UI 업데이트
     */
    updateUIAfterActualRegistration: function(planId, newActualQty, planQty) {
        const selectedRadio = document.querySelector(`input[name="selectedPlan"][data-plan-id="${planId}"]`);
        if (selectedRadio) {
            selectedRadio.dataset.actualQty = newActualQty.toString();
            
            const row = selectedRadio.closest('tr');
            const actualQtyCell = row.querySelector('td:nth-child(10)');
            if (actualQtyCell) {
                actualQtyCell.textContent = this.formatQuantity(newActualQty);
            }
        }
        
        const progressBar = document.getElementById('progressBar');
        const progressText = document.getElementById('displayProgressRate');
        this.updateProgressBar(newActualQty, planQty, progressBar, progressText);
        
        const remainingQtyElement = document.getElementById('displayRemainingQty');
        if (remainingQtyElement) {
            const remaining = Math.max(0, planQty - newActualQty);
            remainingQtyElement.textContent = this.formatQuantity(remaining);
        }
        
        const currentActualElement = document.getElementById('displayCurrentActualQty');
        if (currentActualElement) {
            currentActualElement.textContent = this.formatQuantity(newActualQty);
        }
    },
    
    /**
     * 알림 메시지 표시
     */
    showAlert: function(type, title, message, autoHide = true) {
        const existingAlert = document.querySelector('.custom-alert');
        if (existingAlert) {
            existingAlert.remove();
        }
        
        const alertClass = {
            'success': 'alert-success',
            'error': 'alert-danger',
            'warning': 'alert-warning',
            'info': 'alert-info'
        }[type] || 'alert-info';
        
        const iconClass = {
            'success': 'bi-check-circle-fill',
            'error': 'bi-x-circle-fill',
            'warning': 'bi-exclamation-triangle-fill',
            'info': 'bi-info-circle-fill'
        }[type] || 'bi-info-circle-fill';
        
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert ${alertClass} alert-dismissible fade show custom-alert`;
        alertDiv.style.cssText = `
            position: fixed; 
            top: 20px; 
            left: 50%; 
            transform: translateX(-50%); 
            z-index: 9999; 
            min-width: 500px; 
            max-width: 700px; 
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        `;
        
        alertDiv.innerHTML = `
            <div class="d-flex align-items-start">
                <i class="bi ${iconClass} me-3" style="font-size: 1.2rem; margin-top: 2px;"></i>
                <div style="flex: 1;">
                    <strong>${title}</strong>
                    <div style="margin-top: 5px; white-space: pre-line;">${message}</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        
        document.body.appendChild(alertDiv);
        
        if (autoHide && type === 'success') {
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.remove();
                }
            }, 5000);
        }
        
        return alertDiv;
    }
};

/**
 * 버튼 상태 설정 헬퍼 함수
 */
function setButtonState(button, isActive, style) {
    if (!button) return;
    
    if (isActive) {
        button.disabled = false;
        if (style === 'solid') {
            button.classList.remove('btn-outline-primary', 'btn-secondary');
            button.classList.add('btn-primary');
        } else {
            button.classList.remove('btn-primary', 'btn-secondary');
            button.classList.add('btn-outline-primary');
        }
    } else {
        button.disabled = true;
        button.classList.remove('btn-primary', 'btn-outline-primary');
        button.classList.add('btn-secondary');
    }
}

/**
 * 상태별 버튼 활성화/비활성화 함수
 */
function updateButtonStates(selectedRadio) {
    const workStartButton = document.getElementById('workStartButton');
    const workEndButton = document.getElementById('workEndButton');
    const actualRegisterButton = document.getElementById('actualRegisterButton');
    const saveActualButton = document.getElementById('saveActualButton');

    if (!selectedRadio) {
        setButtonState(workStartButton, false, 'outline');
        setButtonState(workEndButton, false, 'outline');
        setButtonState(actualRegisterButton, false, 'outline');
        if (saveActualButton) saveActualButton.disabled = true;
        return;
    }
    
    const planStatus = selectedRadio.dataset.status;
    
    if (planStatus === '작업지시') {
        setButtonState(workStartButton, true, 'solid');
    } else {
        setButtonState(workStartButton, false, 'outline');
    }

    if (planStatus === '진행중') {
        setButtonState(workEndButton, true, 'solid');
    } else {
        setButtonState(workEndButton, false, 'outline');
    }

    if (planStatus === '작업지시' || planStatus === '진행중') {
        setButtonState(actualRegisterButton, true, 'solid');
        if (saveActualButton) saveActualButton.disabled = false;
    } else {
        setButtonState(actualRegisterButton, false, 'outline');
        if (saveActualButton) saveActualButton.disabled = true;
    }

    const formInputs = document.querySelectorAll('#actualRegisterForm input, #actualRegisterForm textarea');
    formInputs.forEach(input => {
        if (planStatus === '완료' || planStatus === '취소') {
            input.readOnly = true;
            input.style.backgroundColor = '#e9ecef';
        } else {
            input.readOnly = false;
            input.style.backgroundColor = '';
        }
    });

    showStatusMessage(planStatus);
}

/**
 * 상태별 안내 메시지 함수
 */
function showStatusMessage(status) {
    const existingMessage = document.querySelector('.status-message');
    if (existingMessage) {
        existingMessage.remove();
    }

    let messageText = '';
    let messageClass = '';
    let iconClass = '';

    switch(status) {
        case '작업지시':
            messageText = '작업지시 상태: 작업시작 버튼을 클릭하거나 바로 실적을 등록할 수 있습니다.';
            messageClass = 'alert-info';
            iconClass = 'bi-clipboard-check';
            break;
        case '진행중':
            messageText = '진행중 상태: 실적 등록 및 작업종료가 가능합니다.';
            messageClass = 'alert-warning';
            iconClass = 'bi-gear-fill';
            break;
        case '완료':
            messageText = '완료 상태: 작업이 완료되어 더 이상 수정할 수 없습니다. 실적은 조회만 가능합니다.';
            messageClass = 'alert-success';
            iconClass = 'bi-check-circle-fill';
            break;
        case '취소':
            messageText = '취소 상태: 취소된 작업입니다.';
            messageClass = 'alert-danger';
            iconClass = 'bi-x-circle-fill';
            break;
        default:
            return;
    }

    const messageDiv = document.createElement('div');
    messageDiv.className = `alert ${messageClass} status-message mt-3 mb-3`;
    messageDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="${iconClass} me-2"></i>
            <span>${messageText}</span>
        </div>
    `;

    const selectedPlanCard = document.getElementById('selectedPlanCard');
    const cardBody = selectedPlanCard ? selectedPlanCard.querySelector('.card-body') : null;
    if (cardBody) {
        cardBody.insertBefore(messageDiv, cardBody.firstChild);
    }
}

/**
 * 폼 초기화 함수
 */
function clearActualForm() {
    const elements = {
        actualPlanId: document.getElementById('actualPlanId'),
        currentActualQtyHidden: document.getElementById('currentActualQtyHidden'),
        currentPlanQtyHidden: document.getElementById('currentPlanQtyHidden'),
        actualQtyInput: document.getElementById('actualQtyInput'),
        defectQtyInput: document.getElementById('defectQtyInput'),
        actualRemark: document.getElementById('actualRemark'),
        actualDateInput: document.getElementById('actualDate'),
        displayPlanId: document.getElementById('displayPlanId'),
        displayItemName: document.getElementById('displayItemName'),
        displayPlanQty: document.getElementById('displayPlanQty'),
        displayCurrentActualQty: document.getElementById('displayCurrentActualQty'),
        displayStartDate: document.getElementById('displayStartDate'),
        displayDueDate: document.getElementById('displayDueDate'),
        displayStatus: document.getElementById('displayStatus'),
        displayProgressRate: document.getElementById('displayProgressRate'),
        displayRemainingQty: document.getElementById('displayRemainingQty'),
        progressBar: document.getElementById('progressBar'),
        selectedPlanCard: document.getElementById('selectedPlanCard'),
        dailyActualsTableBody: document.querySelector('#dailyActualsTable tbody')
    };

    // 오늘 날짜 설정
    const today = new Date();
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

    // 폼 필드 초기화
    if (elements.actualPlanId) elements.actualPlanId.value = '';
    if (elements.currentActualQtyHidden) elements.currentActualQtyHidden.value = '';
    if (elements.currentPlanQtyHidden) elements.currentPlanQtyHidden.value = '';
    if (elements.actualQtyInput) elements.actualQtyInput.value = '';
    if (elements.defectQtyInput) elements.defectQtyInput.value = '0';
    if (elements.actualRemark) elements.actualRemark.value = '';
    if (elements.actualDateInput) elements.actualDateInput.value = todayStr;
    
    // 버튼 상태 초기화
    updateButtonStates(null);

    // 폼 필드 readonly 해제
    const formInputs = document.querySelectorAll('#actualRegisterForm input, #actualRegisterForm textarea');
    formInputs.forEach(input => {
        input.readOnly = false;
        input.style.backgroundColor = '';
    });

    // 디스플레이 요소 초기화
    if (elements.displayPlanId) elements.displayPlanId.textContent = '-';
    if (elements.displayItemName) elements.displayItemName.textContent = '-';
    if (elements.displayPlanQty) elements.displayPlanQty.textContent = '-';
    if (elements.displayCurrentActualQty) elements.displayCurrentActualQty.textContent = '-';
    if (elements.displayStartDate) elements.displayStartDate.textContent = '-';
    if (elements.displayDueDate) elements.displayDueDate.textContent = '-';
    if (elements.displayStatus) {
        elements.displayStatus.textContent = '-';
        elements.displayStatus.className = 'badge bg-secondary';
    }
    if (elements.displayProgressRate) elements.displayProgressRate.textContent = '0%';
    if (elements.displayRemainingQty) elements.displayRemainingQty.textContent = '-';
    if (elements.progressBar) elements.progressBar.style.width = '0%';

    // 라디오 버튼 선택 해제
    const checkedRadio = document.querySelector('input[name="selectedPlan"]:checked');
    if (checkedRadio) {
        checkedRadio.checked = false;
    }

    // 선택된 계획 카드 숨김
    if (elements.selectedPlanCard) {
        elements.selectedPlanCard.style.display = 'none';
    }

    // 일별 실적 목록 초기화
    if (elements.dailyActualsTableBody) {
        elements.dailyActualsTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4"><i class="bi bi-list-ul"></i><br>계획을 선택하면 실적 목록이 표시됩니다.</td></tr>';
    }
    
    // 상태 메시지 제거
    const existingMessage = document.querySelector('.status-message');
    if (existingMessage) {
        existingMessage.remove();
    }
}

/**
 * 일별 실적 목록 로드 함수
 */
function loadDailyActuals(planId) {
    const dailyActualsTableBody = document.querySelector('#dailyActualsTable tbody');
    if (!dailyActualsTableBody) return;
    
    dailyActualsTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4"><i class="bi bi-hourglass-split"></i><br>실적을 로드 중입니다...</td></tr>';
    
    fetch(`/productactual/daily?planId=${planId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('일별 실적을 불러오는 데 실패했습니다.');
            }
            return response.json();
        })
        .then(dailyActuals => {
            dailyActualsTableBody.innerHTML = '';
            if (dailyActuals.length === 0) {
                dailyActualsTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4"><i class="bi bi-clipboard-x"></i><br>등록된 실적이 없습니다.</td></tr>';
            } else {
                dailyActuals.forEach((actual, index) => {
                    const row = `
                        <tr>
                            <td class="text-center">${dailyActuals.length - index}</td>
                            <td class="text-center">${actual.actualDate}</td>
                            <td class="text-center text-success fw-bold">${actual.actualQty}</td>
                            <td class="text-center text-danger">${actual.defectQty}</td>
                            <td>${actual.remark || '-'}</td>
                            <td class="text-center small text-muted">${actual.createdAt}</td>
                        </tr>
                    `;
                    dailyActualsTableBody.insertAdjacentHTML('beforeend', row);
                });
            }
        })
        .catch(error => {
            console.error('일별 실적 로드 중 오류 발생:', error);
            dailyActualsTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4"><i class="bi bi-exclamation-triangle"></i><br>실적 로드 오류: ' + error.message + '</td></tr>';
        });
}

/**
 * DOMContentLoaded 이벤트 리스너
 */
document.addEventListener('DOMContentLoaded', function() {
    
    const elements = {
        productionPlanTable: document.getElementById('productionPlanTable'),
        selectedPlanCard: document.getElementById('selectedPlanCard'),
        workStartButton: document.getElementById('workStartButton'),
        workEndButton: document.getElementById('workEndButton'),
        actualRegisterButton: document.getElementById('actualRegisterButton'),
        actualRegisterForm: document.getElementById('actualRegisterForm'),
        clearActualFormButton: document.getElementById('clearActualFormButton'),
        // 모달 관련 요소들 추가
        productionPlanRegisterModal: document.getElementById('productionPlanRegisterModal'),
        remarkModal: document.getElementById('remarkModal')
    };

    // 전역 함수 설정
    window.getStatusBadgeClass = ProductActualManager.getStatusBadgeClass;
    window.loadDailyActuals = loadDailyActuals;

    // 상세 버튼 클릭 이벤트 (이벤트 위임 방식)
    document.addEventListener('click', function(event) {
        // 상세 버튼 클릭 처리
        if (event.target.matches('[data-bs-target="#productionPlanRegisterModal"]') || 
            event.target.closest('[data-bs-target="#productionPlanRegisterModal"]')) {
            
            const button = event.target.closest('[data-bs-target="#productionPlanRegisterModal"]');
            const planId = button.getAttribute('data-plan-id');
            const mode = button.getAttribute('data-mode');
            
            if (mode === 'detail' && planId) {
                handleDetailModalShow(planId);
            }
        }
        
        // 비고 버튼 클릭 처리
        if (event.target.matches('[data-bs-target="#remarkModal"]') || 
            event.target.closest('[data-bs-target="#remarkModal"]')) {
            
            const button = event.target.closest('[data-bs-target="#remarkModal"]');
            const remarkContent = button.getAttribute('data-remark');
            handleRemarkModalShow(remarkContent);
        }
    });

    /**
     * 상세 모달 표시 처리
     */
    function handleDetailModalShow(planId) {
        if (!planId) {
            ProductActualManager.showAlert('error', '오류', '계획 ID를 찾을 수 없습니다.');
            return;
        }

        // 로딩 표시
        ProductActualManager.showProgressMessage('상세 정보를 불러오는 중...');

        fetch(`/productplan/${planId}`)
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
                ProductActualManager.hideProgressMessage();
                populateDetailModal(planData);
            })
            .catch(error => {
                ProductActualManager.hideProgressMessage();
                console.error('Error fetching plan details:', error);
                ProductActualManager.showAlert('error', '로드 실패', 
                    '생산 계획 상세 정보를 불러오는 중 오류가 발생했습니다: ' + error.message);
            });
    }

    /**
     * 상세 모달에 데이터 채우기
     */
    function populateDetailModal(planData) {
        const modalElements = {
            title: document.getElementById('productionPlanRegisterModalLabel'),
            itemSearchInput: document.getElementById('itemSearchInput'),
            itemCode: document.getElementById('itemCode'),
            modalPlanQty: document.getElementById('modalPlanQty'),
            modalStartDate: document.getElementById('modalStartDate'),
            modalDueDate: document.getElementById('modalDueDate'),
            modalCompletionDate: document.getElementById('modalCompletionDate'),
            modalActualQty: document.getElementById('modalActualQty'),
            modalStatus: document.getElementById('modalStatus'),
            employeeSearchInput: document.getElementById('employeeSearchInput'),
            modalEmpNo: document.getElementById('modalEmpNo'),
            remark: document.getElementById('remark'),
            submitButton: document.getElementById('modalSubmitButton')
        };

        // 모달 제목 설정
        if (modalElements.title) {
            modalElements.title.textContent = '생산 계획 상세 (조회 전용)';
        }

        // 데이터 채우기
        if (modalElements.itemSearchInput) modalElements.itemSearchInput.value = planData.itemName || '';
        if (modalElements.itemCode) modalElements.itemCode.value = planData.itemCode || '';
        if (modalElements.modalPlanQty) modalElements.modalPlanQty.value = planData.planQty || '';
        if (modalElements.modalStartDate) modalElements.modalStartDate.value = planData.startDate || '';
        if (modalElements.modalDueDate) modalElements.modalDueDate.value = planData.dueDate || '';
        if (modalElements.modalCompletionDate) modalElements.modalCompletionDate.value = planData.completionDate || '';
        if (modalElements.modalActualQty) modalElements.modalActualQty.value = planData.actualQty || '';
        if (modalElements.modalStatus) modalElements.modalStatus.value = planData.status || '계획';
        if (modalElements.employeeSearchInput) modalElements.employeeSearchInput.value = planData.empName || '';
        if (modalElements.modalEmpNo) modalElements.modalEmpNo.value = planData.empNo || '';
        if (modalElements.remark) modalElements.remark.value = planData.remark || '';

        // 모든 필드를 읽기 전용으로 설정
        const formFields = document.querySelectorAll('#productionPlanForm input, #productionPlanForm select, #productionPlanForm textarea');
        formFields.forEach(field => {
            field.readOnly = true;
            if (field.tagName === 'SELECT') {
                field.disabled = true;
            }
            field.style.backgroundColor = '#e9ecef';
        });

        // 제출 버튼 숨기기
        if (modalElements.submitButton) {
            modalElements.submitButton.style.display = 'none';
        }
    }

    /**
     * 비고 모달 표시 처리
     */
    function handleRemarkModalShow(remarkContent) {
        const modalRemarkContent = document.getElementById('modalRemarkContent');
        if (modalRemarkContent) {
            if (remarkContent === null || remarkContent.trim() === '') {
                modalRemarkContent.textContent = '등록된 비고 내용이 없습니다.';
            } else {
                modalRemarkContent.textContent = remarkContent;
            }
        }
    }

    // 모달 숨김 시 초기화
    if (elements.productionPlanRegisterModal) {
        elements.productionPlanRegisterModal.addEventListener('hidden.bs.modal', function() {
            // 폼 초기화
            const form = document.getElementById('productionPlanForm');
            if (form) {
                form.reset();
            }

            // 필드 스타일 복원
            const formFields = document.querySelectorAll('#productionPlanForm input, #productionPlanForm select, #productionPlanForm textarea');
            formFields.forEach(field => {
                field.readOnly = false;
                if (field.tagName === 'SELECT') {
                    field.disabled = false;
                }
                field.style.backgroundColor = '';
            });

            // 제출 버튼 복원
            const submitButton = document.getElementById('modalSubmitButton');
            if (submitButton) {
                submitButton.style.display = 'block';
            }
        });
    }

    // 작업시작 버튼 이벤트
    if (elements.workStartButton) {
        elements.workStartButton.addEventListener('click', async function() {
            const selectedRadio = document.querySelector('input[name="selectedPlan"]:checked');
            if (!selectedRadio) {
                ProductActualManager.showAlert('warning', '선택 필요', '작업을 시작할 생산 계획을 선택해주세요.');
                return;
            }

            const planId = selectedRadio.dataset.planId;
            const itemName = selectedRadio.dataset.itemName;
            const status = selectedRadio.dataset.status;

            if (status !== '작업지시') {
                let message = '';
                switch(status) {
                    case '진행중':
                        message = '이미 진행중인 작업입니다.';
                        break;
                    case '완료':
                        message = '이미 완료된 작업입니다.';
                        break;
                    case '취소':
                        message = '취소된 작업은 시작할 수 없습니다.';
                        break;
                    default:
                        message = `현재 상태(${status})에서는 작업을 시작할 수 없습니다.`;
                }
                ProductActualManager.showAlert('error', '상태 오류', message);
                return;
            }

            try {
                await ProductActualManager.confirmStartWork(planId, itemName);
                
                const response = await fetch(`/productactual/start-work/${planId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                ProductActualManager.hideProgressMessage();

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || '작업 시작 중 오류가 발생했습니다.');
                }

                ProductActualManager.showAlert('success', '작업 시작 완료', 
                    `${itemName}의 작업이 시작되었습니다.\n페이지를 새로고침합니다.`);
                
                setTimeout(() => location.reload(), 2000);

            } catch (error) {
                ProductActualManager.hideProgressMessage();
                if (error === '사용자 취소') return;
                
                console.error('작업 시작 중 오류:', error);
                ProductActualManager.showAlert('error', '작업 시작 실패', 
                    '작업 시작에 실패했습니다:\n' + error.message);
            }
        });
    }

    // 작업종료 버튼 이벤트
    if (elements.workEndButton) {
        elements.workEndButton.addEventListener('click', async function() {
            const selectedRadio = document.querySelector('input[name="selectedPlan"]:checked');
            if (!selectedRadio) {
                ProductActualManager.showAlert('warning', '선택 필요', '작업을 종료할 생산 계획을 선택해주세요.');
                return;
            }

            const planId = selectedRadio.dataset.planId;
            const itemName = selectedRadio.dataset.itemName;
            const status = selectedRadio.dataset.status;
            const actualQty = parseInt(selectedRadio.dataset.actualQty, 10);
            const planQty = parseInt(selectedRadio.dataset.planQty, 10);

            if (status !== '진행중') {
                let message = '';
                switch(status) {
                    case '작업지시':
                        message = '아직 작업이 시작되지 않았습니다. 먼저 작업을 시작해주세요.';
                        break;
                    case '완료':
                        message = '이미 완료된 작업입니다.';
                        break;
                    case '취소':
                        message = '취소된 작업입니다.';
                        break;
                    default:
                        message = `현재 상태(${status})에서는 작업을 종료할 수 없습니다.`;
                }
                ProductActualManager.showAlert('error', '상태 오류', message);
                return;
            }

            if (actualQty <= 0) {
                if (!confirm('아직 등록된 생산실적이 없습니다.\n그래도 작업을 종료하시겠습니까?')) {
                    return;
                }
            }

            try {
                await ProductActualManager.confirmEndWork(planId, itemName, actualQty, planQty);
                
                const response = await fetch(`/productactual/end-work/${planId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                ProductActualManager.hideProgressMessage();

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || '작업 종료 중 오류가 발생했습니다.');
                }

                ProductActualManager.showAlert('success', '작업 종료 완료', 
                    `${itemName}의 작업이 완료되었습니다.\n완제품 ${ProductActualManager.formatQuantity(actualQty)} EA가 완제품창고에 입고되었습니다.\n\n페이지를 새로고침합니다.`);
                
                setTimeout(() => location.reload(), 3000);

            } catch (error) {
                ProductActualManager.hideProgressMessage();
                if (error === '사용자 취소') return;
                
                console.error('작업 종료 중 오류:', error);
                ProductActualManager.showAlert('error', '작업 종료 실패', 
                    '작업 종료에 실패했습니다:\n' + error.message);
            }
        });
    }

    // 실적등록 버튼 (상단) 이벤트
    if (elements.actualRegisterButton) {
        elements.actualRegisterButton.addEventListener('click', function() {
            const selectedRadio = document.querySelector('input[name="selectedPlan"]:checked');
            if (!selectedRadio) {
                ProductActualManager.showAlert('warning', '선택 필요', '실적을 등록할 생산 계획을 선택해주세요.');
                return;
            }

            const status = selectedRadio.dataset.status;
            
            if (status !== '작업지시' && status !== '진행중') {
                let message = '';
                switch(status) {
                    case '완료':
                        message = '완료된 작업의 실적은 수정할 수 없습니다. 조회만 가능합니다.';
                        break;
                    case '취소':
                        message = '취소된 작업의 실적은 등록할 수 없습니다.';
                        break;
                    default:
                        message = `현재 상태(${status})에서는 실적을 등록할 수 없습니다.`;
                }
                ProductActualManager.showAlert('error', '상태 오류', message);
                return;
            }

            if (elements.selectedPlanCard) {
                elements.selectedPlanCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
            
            setTimeout(() => {
                const actualQtyInput = document.getElementById('actualQtyInput');
                if (actualQtyInput) {
                    actualQtyInput.focus();
                }
            }, 500);
        });
    }

    // 테이블에서 라디오 버튼 클릭 시 이벤트 핸들러
    if (elements.productionPlanTable) {
        elements.productionPlanTable.addEventListener('change', function(event) {
            if (event.target.classList.contains('row-radio')) {
                const selectedRadio = event.target;
                const planId = selectedRadio.dataset.planId;
                const itemCode = selectedRadio.dataset.itemCode;
                const itemName = selectedRadio.dataset.itemName;
                const planQty = parseInt(selectedRadio.dataset.planQty, 10);
                const actualQty = parseInt(selectedRadio.dataset.actualQty, 10);
                const status = selectedRadio.dataset.status;
                const startDate = selectedRadio.dataset.startDate;
                const dueDate = selectedRadio.dataset.dueDate;

                if (elements.selectedPlanCard) {
                    elements.selectedPlanCard.style.display = 'block';
                }

                const displayElements = {
                    displayPlanId: document.getElementById('displayPlanId'),
                    displayItemName: document.getElementById('displayItemName'),
                    displayPlanQty: document.getElementById('displayPlanQty'),
                    displayCurrentActualQty: document.getElementById('displayCurrentActualQty'),
                    displayStartDate: document.getElementById('displayStartDate'),
                    displayDueDate: document.getElementById('displayDueDate'),
                    displayStatus: document.getElementById('displayStatus'),
                    displayProgressRate: document.getElementById('displayProgressRate'),
                    displayRemainingQty: document.getElementById('displayRemainingQty'),
                    progressBar: document.getElementById('progressBar')
                };

                if (displayElements.displayPlanId) displayElements.displayPlanId.textContent = planId;
                if (displayElements.displayItemName) displayElements.displayItemName.textContent = itemName;
                if (displayElements.displayPlanQty) displayElements.displayPlanQty.textContent = planQty;
                if (displayElements.displayCurrentActualQty) displayElements.displayCurrentActualQty.textContent = actualQty;
                if (displayElements.displayStartDate) displayElements.displayStartDate.textContent = startDate;
                if (displayElements.displayDueDate) displayElements.displayDueDate.textContent = dueDate;
                
                if (displayElements.displayStatus) {
                    displayElements.displayStatus.textContent = status;
                    displayElements.displayStatus.className = `badge ${ProductActualManager.getStatusBadgeClass(status)}`;
                }

                const progressRate = (planQty > 0) ? ((actualQty / planQty) * 100).toFixed(1) : 0;
                if (displayElements.displayProgressRate) displayElements.displayProgressRate.textContent = `${progressRate}%`;
                if (displayElements.progressBar) displayElements.progressBar.style.width = `${Math.min(progressRate, 100)}%`;
                
                const remainingQty = Math.max(0, planQty - actualQty);
                if (displayElements.displayRemainingQty) displayElements.displayRemainingQty.textContent = remainingQty;

                const formElements = {
                    actualPlanId: document.getElementById('actualPlanId'),
                    currentActualQtyHidden: document.getElementById('currentActualQtyHidden'),
                    currentPlanQtyHidden: document.getElementById('currentPlanQtyHidden')
                };
                
                if (formElements.actualPlanId) formElements.actualPlanId.value = planId;
                if (formElements.currentActualQtyHidden) formElements.currentActualQtyHidden.value = actualQty;
                if (formElements.currentPlanQtyHidden) formElements.currentPlanQtyHidden.value = planQty;

                updateButtonStates(selectedRadio);
                loadDailyActuals(planId);

                if (elements.selectedPlanCard) {
                    elements.selectedPlanCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            }
        });
    }

    // 실적 등록 폼 제출 이벤트
    if (elements.actualRegisterForm) {
        elements.actualRegisterForm.addEventListener('submit', function(event) {
            event.preventDefault();
            
            const formData = new FormData(elements.actualRegisterForm);
            const data = Object.fromEntries(formData.entries());
            
            const validation = ProductActualManager.validateActualForm(data);
            if (!validation.isValid) {
                ProductActualManager.showAlert('error', '입력 오류', validation.errors.join('\n'));
                return;
            }
            
            const planId = data.planId;
            const actualDate = data.actualDate;
            const actualQty = parseInt(data.actualQty, 10);
            const defectQty = parseInt(data.defectQty, 10);
            const remark = data.remark;
            const currentAccumulatedActualQty = parseInt(data.currentActualQty, 10);
            const totalPlanQty = parseInt(data.currentPlanQty, 10);

            if (!planId) {
                ProductActualManager.showAlert('error', '입력 오류', '생산 계획을 먼저 선택해주세요.');
                return;
            }

            const newAccumulatedActualQty = currentAccumulatedActualQty + actualQty;

            if (newAccumulatedActualQty > totalPlanQty) {
                ProductActualManager.showAlert('error', '수량 초과', 
                    `오늘 생산 실적(양품)을 더하면 계획 수량(${totalPlanQty} EA)을 초과합니다.\n현재까지 실적(양품): ${currentAccumulatedActualQty} EA, 오늘 등록 양품: ${actualQty} EA`);
                return;
            }

            const requestData = {
                planId: planId,
                actualDate: actualDate,
                actualQty: actualQty,
                defectQty: defectQty,
                remark: remark
            };

            fetch('/productactual/insert', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData),
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { 
                        throw new Error(err.message || '서버 오류'); 
                    });
                }
                return response.json();
            })
            .then(data => {
                ProductActualManager.showAlert('success', '등록 완료', '생산 실적이 성공적으로 등록되었습니다.');

                const selectedRadio = document.querySelector('input[name="selectedPlan"]:checked');
                if (selectedRadio) {
                    selectedRadio.dataset.actualQty = newAccumulatedActualQty.toString();
                }

                ProductActualManager.updateUIAfterActualRegistration(planId, newAccumulatedActualQty, totalPlanQty);

                if (selectedRadio) {
                    const selectedRow = selectedRadio.closest('tr');
                    const actualQtyCellInTable = selectedRow.cells[9];
                    if (actualQtyCellInTable) {
                        actualQtyCellInTable.textContent = newAccumulatedActualQty;
                    }
                }

                loadDailyActuals(planId);

                // 실적 입력 폼의 특정 필드만 초기화
                const actualQtyInput = document.getElementById('actualQtyInput');
                const defectQtyInput = document.getElementById('defectQtyInput');
                const actualRemark = document.getElementById('actualRemark');
                
                if (actualQtyInput) actualQtyInput.value = '';
                if (defectQtyInput) defectQtyInput.value = '0';
                if (actualRemark) actualRemark.value = '';
            })
            .catch(error => {
                console.error('실적 등록 중 오류 발생:', error);
                ProductActualManager.showAlert('error', '등록 실패', '실적 등록에 실패했습니다: ' + error.message);
            });
        });
    }

    // 폼 초기화 버튼 클릭 이벤트
    if (elements.clearActualFormButton) {
        elements.clearActualFormButton.addEventListener('click', function() {
            clearActualForm();
        });
    }

});

// CSS 스타일 추가
const productActualStyle = document.createElement('style');
productActualStyle.textContent = `
    /* 생산실적관리 전용 스타일 */
    .btn-outline-primary:hover:not(:disabled) {
        background-color: #0d6efd !important;
        border-color: #0d6efd !important;
        color: #ffffff !important;
    }
    
    .btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }
    
    .btn-outline-primary:not(:disabled) {
        border-width: 2px;
        font-weight: 500;
    }
    
    .btn-primary:not(:disabled) {
        font-weight: 600;
        box-shadow: 0 2px 4px rgba(13, 110, 253, 0.25);
    }
    
    .custom-alert {
        border-radius: 8px;
        border-width: 2px;
    }
    
    .progress-alert {
        border-radius: 8px;
        border-width: 2px;
    }
    
    .spinner-border-sm {
        width: 1rem;
        height: 1rem;
    }
    
    .progress-bar {
        transition: width 0.5s ease-in-out, background-color 0.3s ease;
    }
    
    .status-message {
        border-left: 4px solid;
        font-size: 0.9rem;
    }
    
    .status-message.alert-info {
        border-left-color: #0dcaf0;
    }
    
    .status-message.alert-warning {
        border-left-color: #ffc107;
    }
    
    .status-message.alert-success {
        border-left-color: #198754;
    }
    
    .status-message.alert-danger {
        border-left-color: #dc3545;
    }
    
    .badge {
        font-size: 0.85em;
        padding: 0.35em 0.65em;
    }
    
    .table-hover tbody tr:hover {
        background-color: rgba(0, 123, 255, 0.075);
    }
    
    input[readonly], textarea[readonly] {
        background-color: #e9ecef !important;
        cursor: not-allowed;
    }
`;
document.head.appendChild(productActualStyle);