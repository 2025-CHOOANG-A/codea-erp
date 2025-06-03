/**
 * 생산실적관리 추가 기능 스크립트
 * 기존 HTML에서 이미 작업시작/종료 버튼 이벤트는 구현되어 있으므로,
 * 여기서는 추가적인 유틸리티 함수들과 개선사항을 제공합니다.
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
 * 기존 HTML의 작업시작/종료 버튼 이벤트를 개선
 * (기존 이벤트 리스너를 대체하거나 보완)
 */
document.addEventListener('DOMContentLoaded', function() {
    
    // 작업시작 버튼 개선
    const workStartButton = document.getElementById('workStartButton');
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

            if (status !== '작업지시') {
                ProductActualManager.showAlert('error', '상태 오류', '작업지시 상태인 계획만 작업을 시작할 수 있습니다.');
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

    // 작업종료 버튼 개선
    const workEndButton = document.getElementById('workEndButton');
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

            if (status !== '진행중') {
                ProductActualManager.showAlert('error', '상태 오류', '진행중 상태인 계획만 작업을 종료할 수 있습니다.');
                return;
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
    
    // 실적 등록 폼 개선
    const actualRegisterForm = document.getElementById('actualRegisterForm');
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
            // (HTML에서 이미 구현된 로직을 그대로 사용)
        });
    }

});

// CSS 스타일 추가
const additionalStyle = document.createElement('style');
additionalStyle.textContent = `
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
    
    /* 진행률에 따른 프로그레스 바 애니메이션 */
    .progress-bar {
        transition: width 0.5s ease-in-out, background-color 0.3s ease;
    }
    
    /* 상태 배지 개선 */
    .badge {
        font-size: 0.85em;
        padding: 0.35em 0.65em;
    }
    
    /* 테이블 행 호버 효과 개선 */
    .table-hover tbody tr:hover {
        background-color: rgba(0, 123, 255, 0.075);
    }
    
    /* 선택된 행 강조 */
    .table tbody tr.selected {
        background-color: rgba(0, 123, 255, 0.1);
        border-left: 4px solid #007bff;
    }
`;
document.head.appendChild(additionalStyle);