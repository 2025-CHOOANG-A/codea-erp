/**
 * 생산실적관리 완전 통합 스크립트
 * 모든 기능을 하나의 파일로 통합
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
        // 기존 progress 알림이 있으면 제거
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
        const roundedRate = Math.round(rate * 10) / 10; // 소수점 첫째자리까지
        
        if (progressBarElement) {
            progressBarElement.style.width = `${Math.min(roundedRate, 100)}%`;
            
            // 진행률에 따른 색상 변경
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
     * 날짜 포맷팅
     */
    formatDate: function(dateStr) {
        if (!dateStr) return '-';
        
        try {
            const date = new Date(dateStr);
            return date.toLocaleDateString('ko-KR');
        } catch (e) {
            return dateStr;
        }
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
        
        // 미래 날짜 체크
        const actualDate = new Date(formData.actualDate);
        const today = new Date();
        today.setHours(23, 59, 59, 999); // 오늘 끝까지
        
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
        // 선택된 라디오 버튼의 데이터 업데이트
        const selectedRadio = document.querySelector(`input[name="selectedPlan"][data-plan-id="${planId}"]`);
        if (selectedRadio) {
            selectedRadio.dataset.actualQty = newActualQty.toString();
            
            // 테이블 행의 실제수량 셀 업데이트
            const row = selectedRadio.closest('tr');
            const actualQtyCell = row.querySelector('td:nth-child(10)'); // 실제수량 컬럼
            if (actualQtyCell) {
                actualQtyCell.textContent = this.formatQuantity(newActualQty);
            }
        }
        
        // 진행률 업데이트
        const progressBar = document.getElementById('progressBar');
        const progressText = document.getElementById('displayProgressRate');
        this.updateProgressBar(newActualQty, planQty, progressBar, progressText);
        
        // 잔여수량 업데이트
        const remainingQtyElement = document.getElementById('displayRemainingQty');
        if (remainingQtyElement) {
            const remaining = Math.max(0, planQty - newActualQty);
            remainingQtyElement.textContent = this.formatQuantity(remaining);
        }
        
        // 현재 실적수량 표시 업데이트
        const currentActualElement = document.getElementById('displayCurrentActualQty');
        if (currentActualElement) {
            currentActualElement.textContent = this.formatQuantity(newActualQty);
        }
    },
    
    /**
     * 알림 메시지 개선된 표시
     */
    showAlert: function(type, title, message, autoHide = true) {
        // 기존 알림 제거
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
 * 상태별 버튼 활성화/비활성화 함수
 */
function updateButtonStates(status) {
    const workStartButton = document.getElementById('workStartButton');
    const workEndButton = document.getElementById('workEndButton');
    const actualRegisterButton = document.getElementById('actualRegisterButton');
    const saveActualButton = document.getElementById('saveActualButton');

    // 작업시작 버튼
    if (status === '작업지시') {
        workStartButton.disabled = false;
        workStartButton.classList.remove('btn-secondary');
        workStartButton.classList.add('btn-primary');
    } else {
        workStartButton.disabled = true;
        workStartButton.classList.remove('btn-primary');
        workStartButton.classList.add('btn-secondary');
    }

    // 작업종료 버튼
    if (status === '진행중') {
        workEndButton.disabled = false;
        workEndButton.classList.remove('btn-secondary');
        workEndButton.classList.add('btn-primary');
    } else {
        workEndButton.disabled = true;
        workEndButton.classList.remove('btn-primary');
        workEndButton.classList.add('btn-secondary');
    }

    // 실적등록 버튼 (상단 버튼)
    if (status === '작업지시' || status === '진행중') {
        actualRegisterButton.disabled = false;
        actualRegisterButton.classList.remove('btn-secondary');
        actualRegisterButton.classList.add('btn-primary');
    } else {
        actualRegisterButton.disabled = true;
        actualRegisterButton.classList.remove('btn-primary');
        actualRegisterButton.classList.add('btn-secondary');
    }

    // 실적등록 폼 내부 저장 버튼
    if (status === '작업지시' || status === '진행중') {
        saveActualButton.disabled = false;
    } else {
        saveActualButton.disabled = true;
    }

    // 실적등록 폼 필드들 readonly 설정
    const formInputs = document.querySelectorAll('#actualRegisterForm input, #actualRegisterForm textarea');
    formInputs.forEach(input => {
        if (status === '완료' || status === '취소') {
            input.readOnly = true;
            input.style.backgroundColor = '#e9ecef';
        } else {
            input.readOnly = false;
            input.style.backgroundColor = '';
        }
    });

    // 상태별 안내 메시지 표시
    showStatusMessage(status);
}

/**
 * 상태별 안내 메시지 함수
 */
function showStatusMessage(status) {
    // 기존 메시지 제거
    const existingMessage = document.querySelector('.status-message');
    if (existingMessage) {
        existingMessage.remove();
    }

    let messageText = '';
    let messageClass = '';

    switch(status) {
        case '작업지시':
            messageText = '📋 작업지시 상태: 작업시작 버튼을 클릭하거나 바로 실적을 등록할 수 있습니다.';
            messageClass = 'alert-info';
            break;
        case '진행중':
            messageText = '⚡ 진행중 상태: 실적 등록 및 작업종료가 가능합니다.';
            messageClass = 'alert-warning';
            break;
        case '완료':
            messageText = '✅ 완료 상태: 작업이 완료되어 더 이상 수정할 수 없습니다. 실적은 조회만 가능합니다.';
            messageClass = 'alert-success';
            break;
        case '취소':
            messageText = '❌ 취소 상태: 취소된 작업입니다.';
            messageClass = 'alert-danger';
            break;
        default:
            return;
    }

    const messageDiv = document.createElement('div');
    messageDiv.className = `alert ${messageClass} status-message mt-3 mb-3`;
    messageDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="bi bi-info-circle me-2"></i>
            <span>${messageText}</span>
        </div>
    `;

    // 선택된 계획 카드의 상단에 메시지 추가
    const selectedPlanCard = document.getElementById('selectedPlanCard');
    const cardBody = selectedPlanCard.querySelector('.card-body');
    if (cardBody) {
        cardBody.insertBefore(messageDiv, cardBody.firstChild);
    }
}

/**
 * 폼 초기화 함수
 */
function clearActualForm() {
    const actualPlanId = document.getElementById('actualPlanId');
    const currentActualQtyHidden = document.getElementById('currentActualQtyHidden');
    const currentPlanQtyHidden = document.getElementById('currentPlanQtyHidden');
    const actualQtyInput = document.getElementById('actualQtyInput');
    const defectQtyInput = document.getElementById('defectQtyInput');
    const actualRemark = document.getElementById('actualRemark');
    const actualDateInput = document.getElementById('actualDate');
    
    const workStartButton = document.getElementById('workStartButton');
    const workEndButton = document.getElementById('workEndButton');
    const actualRegisterButton = document.getElementById('actualRegisterButton');
    const saveActualButton = document.getElementById('saveActualButton');
    
    const displayPlanId = document.getElementById('displayPlanId');
    const displayItemName = document.getElementById('displayItemName');
    const displayPlanQty = document.getElementById('displayPlanQty');
    const displayCurrentActualQty = document.getElementById('displayCurrentActualQty');
    const displayStartDate = document.getElementById('displayStartDate');
    const displayDueDate = document.getElementById('displayDueDate');
    const displayStatus = document.getElementById('displayStatus');
    const displayProgressRate = document.getElementById('displayProgressRate');
    const displayRemainingQty = document.getElementById('displayRemainingQty');
    const progressBar = document.getElementById('progressBar');
    const selectedPlanCard = document.getElementById('selectedPlanCard');
    const dailyActualsTableBody = document.querySelector('#dailyActualsTable tbody');

    // 오늘 날짜 설정
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');

    if (actualPlanId) actualPlanId.value = '';
    if (currentActualQtyHidden) currentActualQtyHidden.value = '';
    if (currentPlanQtyHidden) currentPlanQtyHidden.value = '';
    if (actualQtyInput) actualQtyInput.value = '';
    if (defectQtyInput) defectQtyInput.value = '0';
    if (actualRemark) actualRemark.value = '';
    if (actualDateInput) actualDateInput.value = `${year}-${month}-${day}`;
    
    // 모든 버튼 비활성화
    if (workStartButton) {
        workStartButton.disabled = true;
        workStartButton.classList.remove('btn-primary');
        workStartButton.classList.add('btn-secondary');
    }
    
    if (workEndButton) {
        workEndButton.disabled = true;
        workEndButton.classList.remove('btn-primary');
        workEndButton.classList.add('btn-secondary');
    }
    
    if (actualRegisterButton) {
        actualRegisterButton.disabled = true;
        actualRegisterButton.classList.remove('btn-primary');
        actualRegisterButton.classList.add('btn-secondary');
    }
    
    if (saveActualButton) {
        saveActualButton.disabled = true;
    }

    // 폼 필드 readonly 해제
    const formInputs = document.querySelectorAll('#actualRegisterForm input, #actualRegisterForm textarea');
    formInputs.forEach(input => {
        input.readOnly = false;
        input.style.backgroundColor = '';
    });

    // 선택된 계획 정보 초기화
    if (displayPlanId) displayPlanId.textContent = '-';
    if (displayItemName) displayItemName.textContent = '-';
    if (displayPlanQty) displayPlanQty.textContent = '-';
    if (displayCurrentActualQty) displayCurrentActualQty.textContent = '-';
    if (displayStartDate) displayStartDate.textContent = '-';
    if (displayDueDate) displayDueDate.textContent = '-';
    if (displayStatus) {
        displayStatus.textContent = '-';
        displayStatus.className = 'badge bg-secondary';
    }
    if (displayProgressRate) displayProgressRate.textContent = '0%';
    if (displayRemainingQty) displayRemainingQty.textContent = '-';
    if (progressBar) progressBar.style.width = '0%';

    // 라디오 버튼 선택 해제
    const checkedRadio = document.querySelector('input[name="selectedPlan"]:checked');
    if (checkedRadio) {
        checkedRadio.checked = false;
    }

    // 선택된 계획 카드 숨김
    if (selectedPlanCard) {
        selectedPlanCard.style.display = 'none';
    }

    // 일별 실적 목록 초기화
    if (dailyActualsTableBody) {
        dailyActualsTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4"><i class="bi bi-inbox"></i><br>계획을 선택하면 실적 목록이 표시됩니다.</td></tr>';
    }
    
    // 상태 메시지 제거
    const existingMessage = document.querySelector('.status-message');
    if (existingMessage) {
        existingMessage.remove();
    }
}

/**
 * DOMContentLoaded 이벤트 리스너
 */
document.addEventListener('DOMContentLoaded', function() {
    
    // 기존 HTML 요소들 참조
    const productionPlanTable = document.getElementById('productionPlanTable');
    const selectedPlanCard = document.getElementById('selectedPlanCard');
    const workStartButton = document.getElementById('workStartButton');
    const workEndButton = document.getElementById('workEndButton');
    const actualRegisterButton = document.getElementById('actualRegisterButton');
    const actualRegisterForm = document.getElementById('actualRegisterForm');
    const clearActualFormButton = document.getElementById('clearActualFormButton');

    // 상태별 배지 클래스 반환 함수 (전역으로 사용)
    window.getStatusBadgeClass = ProductActualManager.getStatusBadgeClass;

    // 작업시작 버튼 이벤트
    if (workStartButton) {
        // 기존 이벤트 리스너 제거 후 새로운 리스너 추가
        const newStartButton = workStartButton.cloneNode(true);
        workStartButton.parentNode.replaceChild(newStartButton, workStartButton);
        
        newStartButton.addEventListener('click', async function() {
            const selectedRadio = document.querySelector('input[name="selectedPlan"]:checked');
            if (!selectedRadio) {
                ProductActualManager.showAlert('warning', '선택 필요', '작업을 시작할 생산 계획을 선택해주세요.');
                return;
            }

            const planId = selectedRadio.dataset.planId;
            const itemName = selectedRadio.dataset.itemName;
            const status = selectedRadio.dataset.status;

            // 상태 검증 강화
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
                        message = `현재 상태(${status})에서는 작업을 시작할 수 없습니다. '작업지시' 상태인 계획만 작업을 시작할 수 있습니다.`;
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

                const data = await response.json();
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
    if (workEndButton) {
        // 기존 이벤트 리스너 제거 후 새로운 리스너 추가
        const newEndButton = workEndButton.cloneNode(true);
        workEndButton.parentNode.replaceChild(newEndButton, workEndButton);
        
        newEndButton.addEventListener('click', async function() {
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

            // 상태 검증 강화
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
                        message = `현재 상태(${status})에서는 작업을 종료할 수 없습니다. '진행중' 상태인 계획만 작업을 종료할 수 있습니다.`;
                }
                ProductActualManager.showAlert('error', '상태 오류', message);
                return;
            }

            // 실적이 없는 경우 경고
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

                const data = await response.json();
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
    if (actualRegisterButton) {
        // 기존 이벤트 리스너 제거 후 새로운 리스너 추가
        const newRegisterButton = actualRegisterButton.cloneNode(true);
        actualRegisterButton.parentNode.replaceChild(newRegisterButton, actualRegisterButton);
        
        newRegisterButton.addEventListener('click', function() {
            const selectedRadio = document.querySelector('input[name="selectedPlan"]:checked');
            if (!selectedRadio) {
                ProductActualManager.showAlert('warning', '선택 필요', '실적을 등록할 생산 계획을 선택해주세요.');
                return;
            }

            const status = selectedRadio.dataset.status;
            
            // 상태 검증
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

            // 실적 등록 카드로 스크롤
            selectedPlanCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
            
            // 생산 수량 입력 필드에 포커스
            setTimeout(() => {
                const actualQtyInput = document.getElementById('actualQtyInput');
                if (actualQtyInput) {
                    actualQtyInput.focus();
                }
            }, 500);
        });
    }

    // 테이블에서 라디오 버튼 클릭 시 이벤트 핸들러 (통합된 버전)
    if (productionPlanTable) {
        productionPlanTable.addEventListener('change', function(event) {
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

                // 선택된 계획 카드 표시
                selectedPlanCard.style.display = 'block';

                // 선택된 계획 상세 정보 표시
                const displayPlanId = document.getElementById('displayPlanId');
                const displayItemName = document.getElementById('displayItemName');
                const displayPlanQty = document.getElementById('displayPlanQty');
                const displayCurrentActualQty = document.getElementById('displayCurrentActualQty');
                const displayStartDate = document.getElementById('displayStartDate');
                const displayDueDate = document.getElementById('displayDueDate');
                const displayStatus = document.getElementById('displayStatus');
                const displayProgressRate = document.getElementById('displayProgressRate');
                const displayRemainingQty = document.getElementById('displayRemainingQty');
                const progressBar = document.getElementById('progressBar');

                if (displayPlanId) displayPlanId.textContent = planId;
                if (displayItemName) displayItemName.textContent = itemName;
                if (displayPlanQty) displayPlanQty.textContent = planQty;
                if (displayCurrentActualQty) displayCurrentActualQty.textContent = actualQty;
                if (displayStartDate) displayStartDate.textContent = startDate;
                if (displayDueDate) displayDueDate.textContent = dueDate;
                
                // 상태 배지 적용
                if (displayStatus) {
                    displayStatus.textContent = status;
                    displayStatus.className = `badge ${ProductActualManager.getStatusBadgeClass(status)}`;
                }

                // 진행률 계산 및 표시
                const progressRate = (planQty > 0) ? ((actualQty / planQty) * 100).toFixed(1) : 0;
                if (displayProgressRate) displayProgressRate.textContent = `${progressRate}%`;
                if (progressBar) progressBar.style.width = `${Math.min(progressRate, 100)}%`;
                
                // 잔여 수량 계산
                const remainingQty = Math.max(0, planQty - actualQty);
                if (displayRemainingQty) displayRemainingQty.textContent = remainingQty;

                // 실적 등록 폼에 숨겨진 필드 세팅
                const actualPlanId = document.getElementById('actualPlanId');
                const currentActualQtyHidden = document.getElementById('currentActualQtyHidden');
                const currentPlanQtyHidden = document.getElementById('currentPlanQtyHidden');
                
                if (actualPlanId) actualPlanId.value = planId;
                if (currentActualQtyHidden) currentActualQtyHidden.value = actualQty;
                if (currentPlanQtyHidden) currentPlanQtyHidden.value = planQty;

                // 상태별 버튼 활성화/비활성화
                updateButtonStates(status);

                // 선택된 계획의 일별 실적 목록 로드
                loadDailyActuals(planId);

                // 카드가 보이도록 스크롤
                selectedPlanCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    }

    // 실적 등록 폼 제출 이벤트 (기존 로직과 통합)
    if (actualRegisterForm) {
        actualRegisterForm.addEventListener('submit', function(event) {
            event.preventDefault();
            
            const formData = new FormData(actualRegisterForm);
            const data = Object.fromEntries(formData.entries());
            
            // 유효성 검사
            const validation = ProductActualManager.validateActualForm(data);
            if (!validation.isValid) {
                ProductActualManager.showAlert('error', '입력 오류', validation.errors.join('\n'));
                return;
            }
            
            // 기존 실적 등록 로직 실행
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

            // 총 양품 누적 수량 = 기존 누적 양품 + 오늘 양품
            const newAccumulatedActualQty = currentAccumulatedActualQty + actualQty;

            // 계획 수량을 초과하지 않도록 검증
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

            // AJAX 요청으로 실적 등록
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

                // 현재 선택된 라디오 버튼의 data-actual-qty 업데이트
                const selectedRadio = document.querySelector('input[name="selectedPlan"]:checked');
                if (selectedRadio) {
                    selectedRadio.dataset.actualQty = newAccumulatedActualQty.toString();
                }

                // UI 업데이트
                ProductActualManager.updateUIAfterActualRegistration(planId, newAccumulatedActualQty, totalPlanQty);

                // 메인 테이블의 "실제 수량" 셀 업데이트
                if (selectedRadio) {
                    const selectedRow = selectedRadio.closest('tr');
                    const actualQtyCellInTable = selectedRow.cells[9];
                    if (actualQtyCellInTable) {
                        actualQtyCellInTable.textContent = newAccumulatedActualQty;
                    }
                }

                // 일별 실적 목록 새로고침
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
    if (clearActualFormButton) {
        clearActualFormButton.addEventListener('click', function() {
            clearActualForm();
        });
    }

    // 일별 실적 목록 로드 함수
    window.loadDailyActuals = function(planId) {
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
    };

});

/*// CSS 스타일 추가
const additionalStyle = document.createElement('style');
additionalStyle.textContent = `
     비활성화된 버튼 스타일 개선 
    .btn.disabled, .btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
        pointer-events: none;
    }

     상태별 배지 개선 
    .badge.bg-info {
        background-color: #0dcaf0 !important;
        color: #000 !important;
    }

    .badge.bg-warning {
        background-color: #ffc107 !important;
        color: #000 !important;
    }

    .badge.bg-success {
        background-color: #198754 !important;
    }

    .badge.bg-danger {
        background-color: #dc3545 !important;
    }

     상태 메시지 스타일 
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

     읽기 전용 폼 필드 스타일 
    input[readonly], textarea[readonly] {
        background-color: #e9ecef !important;
        cursor: not-allowed;
    }

     버튼 그룹 간격 
    .work-order-btn {
        margin-right: 0.5rem;
    }

    .work-order-btn:last-child {
        margin-right: 0;
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
    
     진행률에 따른 프로그레스 바 애니메이션 
    .progress-bar {
        transition: width 0.5s ease-in-out, background-color 0.3s ease;
    }
    
     상태 배지 개선 
    .badge {
        font-size: 0.85em;
        padding: 0.35em 0.65em;
    }
    
     테이블 행 호버 효과 개선 
    .table-hover tbody tr:hover {
        background-color: rgba(0, 123, 255, 0.075);
    }
    
     선택된 행 강조 
    .table tbody tr.selected {
        background-color: rgba(0, 123, 255, 0.1);
        border-left: 4px solid #007bff;
    }
`;
document.head.appendChild(additionalStyle);*/