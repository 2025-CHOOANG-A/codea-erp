
// 생산계획에서 선택된 항목들을 자동으로 계산
document.addEventListener("DOMContentLoaded", function () {
    console.log('MRP 자동 계산 스크립트 실행');
    
    // URL에서 selectedPlans 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const selectedPlans = urlParams.get('selectedPlans');
    
    if (selectedPlans) {
        const planIds = selectedPlans.split(',');
        console.log('자동 계산할 계획 IDs:', planIds);
        
        // 페이지 로드 후 바로 계산 실행
        setTimeout(() => {
            // 로딩 표시
            showCalculatingMessage();
            
            // 바로 Ajax 계산 실행
            fetch('/mrp/calculate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(planIds)
            })
            .then(res => {
                if (!res.ok) {
                    throw new Error(`서버 오류: ${res.status}`);
                }
                return res.json();
            })
            .then(data => {
                console.log('자동 계산 결과:', data);
                
                // 계산 결과를 바로 표시
                if (data && data.length > 0) {
                    renderRequirementResult(data);
                    showSuccessMessage(data.length);
                    
                    // 해당 계획들을 체크박스에도 표시 (시각적 효과)
                    markSelectedPlans(planIds);
                } else {
                    showNoDataMessage(planIds);
                }
            })
            .catch(error => {
                console.error('자동 계산 오류:', error);
                showErrorMessage(error.message);
                
                // 오류 시에도 해당 계획들을 체크박스에 표시
                markSelectedPlans(planIds);
            })
            .finally(() => {
                hideCalculatingMessage();
            });
            
        }, 500); // 0.5초 후 실행
    }
});

// 계산 중 메시지 표시
function showCalculatingMessage() {
    const messageDiv = document.createElement('div');
    messageDiv.id = 'calculating-message';
    messageDiv.className = 'alert alert-info mb-3';
    messageDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <div class="spinner-border spinner-border-sm me-2" role="status"></div>
            <span>생산계획에서 선택하신 항목들의 자재소요량을 계산 중입니다...</span>
        </div>
    `;
    
    const title = document.querySelector('h2.mb-4');
    if (title) {
        title.parentNode.insertBefore(messageDiv, title.nextSibling);
    }
}

// 계산 중 메시지 숨기기
function hideCalculatingMessage() {
    const messageDiv = document.getElementById('calculating-message');
    if (messageDiv) {
        messageDiv.remove();
    }
}

// 성공 메시지 표시
function showSuccessMessage(count) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-success alert-dismissible fade show mb-3';
    alertDiv.innerHTML = `
        <i class="bi bi-check-circle"></i> 
        선택하신 생산계획에 필요한 자재소요량이 계산되었습니다. 
        자재소요량 결과를 저장하세요.
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const title = document.querySelector('h2.mb-4');
    if (title) {
        title.parentNode.insertBefore(alertDiv, title.nextSibling);
    }
    
    // 저장 버튼 강조
    const saveBtn = document.getElementById('requirementCheckSelectedBtn');
    if (saveBtn) {
        saveBtn.classList.remove('btn-success');
        saveBtn.classList.add('btn-warning');
        saveBtn.innerHTML = '선택항목 저장 (계산완료)';
    }
}

// 데이터 없음 메시지 표시
function showNoDataMessage(planIds) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-warning alert-dismissible fade show mb-3';
    alertDiv.innerHTML = `
        <i class="bi bi-exclamation-triangle"></i> 
        선택하신 생산계획에 대한 자재소요량을 계산할 수 없습니다. 
        (BOM 정보가 없거나 이미 계산된 항목일 수 있습니다)
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const title = document.querySelector('h2.mb-4');
    if (title) {
        title.parentNode.insertBefore(alertDiv, title.nextSibling);
    }
}

// 오류 메시지 표시
function showErrorMessage(errorMsg) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger alert-dismissible fade show mb-3';
    alertDiv.innerHTML = `
        <i class="bi bi-exclamation-circle"></i> 
        자재소요량 계산 중 오류가 발생했습니다: ${errorMsg}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const title = document.querySelector('h2.mb-4');
    if (title) {
        title.parentNode.insertBefore(alertDiv, title.nextSibling);
    }
}

// 선택된 계획들을 체크박스에 표시 (시각적 효과)
function markSelectedPlans(planIds) {
    planIds.forEach(planId => {
        const checkbox = document.querySelector(`.row-check[value="${planId.trim()}"]`);
        if (checkbox && !checkbox.disabled) {
            checkbox.checked = true;
            
            // 행 하이라이트
            const row = checkbox.closest('tr');
            if (row) {
                row.style.backgroundColor = '#d1ecf1'; // 연한 파란색
                row.style.border = '2px solid #bee5eb';
            }
        }
    });
    
    // 전체 선택 체크박스 상태 업데이트
    updateCheckAllStatus();
}

// 전체 선택 체크박스 상태 업데이트
function updateCheckAllStatus() {
    const checkAll = document.getElementById("checkAll");
    const allCheckboxes = document.querySelectorAll(".row-check:not(:disabled)");
    const checkedCheckboxes = document.querySelectorAll(".row-check:checked:not(:disabled)");
    
    if (checkAll && allCheckboxes.length === checkedCheckboxes.length) {
        checkAll.checked = true;
    }
}