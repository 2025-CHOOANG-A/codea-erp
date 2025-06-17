// storage_write.js - 창고 등록/수정 페이지

// 전역 변수
let isFormSubmitting = false;

// 폼 요소들
const warehouseForm = document.getElementById('warehouseForm');
const cancelBtn = document.getElementById('cancelBtn');
const searchEmployeeBtn = document.getElementById('searchEmployeeBtn');
const clearEmployeeBtn = document.getElementById('clearEmployeeBtn');
const remarkTextarea = document.getElementById('remark');
const remarkCount = document.getElementById('remarkCount');

// 우편번호 검색
function searchPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.
            
            // 각 주소의 노출 규칙에 따라 주소를 조합한다.
            // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
            let addr = ''; // 주소 변수
            let extraAddr = ''; // 참고항목 변수

            //사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
                addr = data.roadAddress;
            } else { // 사용자가 지번 주소를 선택했을 경우(J)
                addr = data.jibunAddress;
            }

            // 사용자가 선택한 주소가 도로명 타입일때 참고항목을 조합한다.
            if(data.userSelectedType === 'R'){
                // 법정동명이 있을 경우 추가한다. (법정리는 제외)
                // 법정동의 경우 마지막 문자가 "동/로/가"로 끝난다.
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
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('postCode').value = data.zonecode;
            document.getElementById('address').value = addr + extraAddr;
            
            // 커서를 상세주소 필드로 이동한다.
            document.getElementById('addressDetail').focus();
            
            // 유효성 검사 상태 업데이트
            document.getElementById('postCode').classList.remove('is-invalid');
            document.getElementById('address').classList.remove('is-invalid');
        }
    }).open();
}

// 사원 검색 모달 열기
function openEmployeeSearchModal() {
    const modal = new bootstrap.Modal(document.getElementById('employeeSearchModal'));
    modal.show();
    
    // 모달이 열린 후 검색 입력 필드에 포커스
    document.getElementById('employeeSearchModal').addEventListener('shown.bs.modal', function() {
        document.getElementById('employeeSearchInput').focus();
    });
}

// 사원 검색 실행
async function searchEmployees() {
    const query = document.getElementById('employeeSearchInput').value.trim();
    const resultsDiv = document.getElementById('employeeSearchResults');
    
    if (!query) {
        showEmployeeSearchMessage('검색어를 입력하세요.', 'warning');
        return;
    }
    
    try {
        // 로딩 표시
        resultsDiv.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">검색 중...</span>
                </div>
                <div class="mt-2">검색 중...</div>
            </div>
        `;
        
        const response = await fetch(`/storage/api/employees/search?query=${encodeURIComponent(query)}`);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const employees = await response.json();
        
        if (employees.length === 0) {
            showEmployeeSearchMessage('검색 결과가 없습니다.', 'info');
            return;
        }
        
        // 검색 결과 표시
        let html = '<div class="list-group">';
        employees.forEach(emp => {
            html += `
                <button type="button" class="list-group-item list-group-item-action" 
                        onclick="selectEmployee('${emp.empNo}', '${emp.empName}')">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">${emp.empName}</h6>
                        <small class="text-muted">${emp.empNo}</small>
                    </div>
                </button>
            `;
        });
        html += '</div>';
        
        resultsDiv.innerHTML = html;
        
    } catch (error) {
        console.error('Employee search error:', error);
        showEmployeeSearchMessage('검색 중 오류가 발생했습니다.', 'danger');
    }
}

// 사원 검색 메시지 표시
function showEmployeeSearchMessage(message, type) {
    const resultsDiv = document.getElementById('employeeSearchResults');
    const iconClass = type === 'danger' ? 'exclamation-triangle' : 
                     type === 'warning' ? 'exclamation-circle' : 'info-circle';
    
    resultsDiv.innerHTML = `
        <div class="text-center py-4">
            <i class="bi bi-${iconClass} display-4 text-${type}"></i>
            <div class="mt-2">${message}</div>
        </div>
    `;
}

// 사원 선택
function selectEmployee(empNo, empName) {
    document.getElementById('empNo').value = empNo;
    document.getElementById('empName').value = empName;
    
    // 유효성 검사 상태 업데이트
    document.getElementById('empNo').classList.remove('is-invalid');
    document.getElementById('empName').classList.remove('is-invalid');
    
    // 모달 닫기
    const modal = bootstrap.Modal.getInstance(document.getElementById('employeeSearchModal'));
    modal.hide();
    
    showToast('담당자가 선택되었습니다.', 'success');
}

// 사원 정보 초기화
function clearEmployee() {
    document.getElementById('empNo').value = '';
    document.getElementById('empName').value = '';
    document.getElementById('empNo').classList.remove('is-invalid');
    document.getElementById('empName').classList.remove('is-invalid');
}

// 폼 유효성 검사
function validateForm() {
    let isValid = true;
    const requiredFields = [
        { id: 'whCode', message: '창고 코드를 입력해주세요.' },
        { id: 'whName', message: '창고명을 입력해주세요.' },
        { id: 'postCode', message: '우편번호를 검색해주세요.' },
        { id: 'address', message: '주소를 검색해주세요.' },
        { id: 'addressDetail', message: '상세주소를 입력해주세요.' },
        { id: 'empName', message: '담당자를 선택해주세요.' },
        { id: 'empNo', message: '담당자 사번이 필요합니다.' }
    ];
    
    requiredFields.forEach(field => {
        const element = document.getElementById(field.id);
        if (!element.value.trim()) {
            element.classList.add('is-invalid');
            isValid = false;
        } else {
            element.classList.remove('is-invalid');
        }
    });
    
    // 창고 코드 형식 검사 (영문+숫자)
    const whCode = document.getElementById('whCode').value.trim();
    if (whCode && !/^[A-Za-z0-9]+$/.test(whCode)) {
        document.getElementById('whCode').classList.add('is-invalid');
        showToast('창고 코드는 영문과 숫자만 사용할 수 있습니다.', 'warning');
        isValid = false;
    }
    
    return isValid;
}

// 폼 제출 처리
async function handleFormSubmit(event) {
    event.preventDefault();
    
    if (isFormSubmitting) {
        return;
    }
    
    if (!validateForm()) {
        showToast('필수 항목을 모두 입력해주세요.', 'warning');
        return;
    }
    
    isFormSubmitting = true;
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    
    try {
        // 버튼 비활성화 및 로딩 표시
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>처리 중...';
        
        // 폼 데이터 준비
        const formData = new FormData(warehouseForm);
        const data = Object.fromEntries(formData.entries());
        
        // API 호출
        const isUpdate = document.getElementById('whId') && document.getElementById('whId').value;
        const url = isUpdate ? `/storage/${data.whId}` : '/storage';
        const method = isUpdate ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        if (response.ok && result.success) {
            showToast(result.message || '저장되었습니다.', 'success');
            
            // 성공 시 목록으로 이동
            setTimeout(() => {
                window.location.href = '/storage?success=' + encodeURIComponent(result.message);
            }, 1500);
        } else {
            throw new Error(result.message || '저장 중 오류가 발생했습니다.');
        }
        
    } catch (error) {
        console.error('Form submission error:', error);
        showToast(error.message || '저장 중 오류가 발생했습니다.', 'danger');
    } finally {
        isFormSubmitting = false;
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

// 취소 버튼 처리
function handleCancel() {
    if (confirm('작성 중인 내용이 있습니다. 정말 취소하시겠습니까?')) {
        window.location.href = '/storage';
    }
}

// 비고 글자 수 카운트
function updateRemarkCount() {
    if (remarkTextarea && remarkCount) {
        const currentLength = remarkTextarea.value.length;
        remarkCount.textContent = currentLength;
        
        if (currentLength > 500) {
            remarkCount.style.color = '#dc3545';
            remarkTextarea.classList.add('is-invalid');
        } else {
            remarkCount.style.color = currentLength > 400 ? '#fd7e14' : '#6c757d';
            remarkTextarea.classList.remove('is-invalid');
        }
    }
}

// 토스트 알림 표시
function showToast(message, type = 'info') {
    // 기존 토스트 제거
    const existingToasts = document.querySelectorAll('.custom-toast');
    existingToasts.forEach(toast => toast.remove());
    
    const toastId = 'toast-' + Date.now();
    const toastHtml = `
        <div class="toast custom-toast position-fixed top-0 end-0 m-3" id="${toastId}" role="alert" style="z-index: 9999;">
            <div class="toast-header bg-${type} text-white">
                <i class="bi bi-${getToastIcon(type)} me-2"></i>
                <strong class="me-auto">${getToastTitle(type)}</strong>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', toastHtml);
    
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: type === 'success' ? 3000 : 5000
    });
    toast.show();
    
    // 토스트가 숨겨진 후 DOM에서 제거
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}

// 토스트 아이콘 반환
function getToastIcon(type) {
    switch(type) {
        case 'success': return 'check-circle';
        case 'danger': return 'exclamation-triangle';
        case 'warning': return 'exclamation-circle';
        case 'info': return 'info-circle';
        default: return 'info-circle';
    }
}

// 토스트 제목 반환
function getToastTitle(type) {
    switch(type) {
        case 'success': return '성공';
        case 'danger': return '오류';
        case 'warning': return '경고';
        case 'info': return '정보';
        default: return '알림';
    }
}

// 실시간 유효성 검사
function setupRealTimeValidation() {
    const inputs = warehouseForm.querySelectorAll('input[required], textarea[required]');
    
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            if (this.value.trim()) {
                this.classList.remove('is-invalid');
            }
        });
        
        input.addEventListener('blur', function() {
            if (!this.value.trim()) {
                this.classList.add('is-invalid');
            }
        });
    });
}

// URL 파라미터에서 메시지 처리
function handleUrlMessages() {
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    
    if (error) {
        showToast(decodeURIComponent(error), 'danger');
        // URL에서 파라미터 제거
        const url = new URL(window.location);
        url.searchParams.delete('error');
        window.history.replaceState({}, '', url);
    }
}

// 브라우저 뒤로가기 방지
function preventBackNavigation() {
    let formChanged = false;
    
    // 폼 변경 감지
    const inputs = warehouseForm.querySelectorAll('input, textarea, select');
    inputs.forEach(input => {
        input.addEventListener('input', () => {
            formChanged = true;
        });
    });
    
    // 페이지 이탈 시 경고
    window.addEventListener('beforeunload', function(e) {
        if (formChanged && !isFormSubmitting) {
            e.preventDefault();
            e.returnValue = '';
            return '';
        }
    });
}

// Enter 키로 사원 검색
function handleEmployeeSearchEnter(event) {
    if (event.key === 'Enter') {
        event.preventDefault();
        searchEmployees();
    }
}

// DOMContentLoaded 이벤트 리스너
document.addEventListener('DOMContentLoaded', function() {
    console.log('Storage write page loaded');
    
    // URL 메시지 처리
    handleUrlMessages();
    
    // 폼 제출 이벤트
    if (warehouseForm) {
        warehouseForm.addEventListener('submit', handleFormSubmit);
    }
    
    // 취소 버튼 이벤트
    if (cancelBtn) {
        cancelBtn.addEventListener('click', handleCancel);
    }
    
    // 사원 검색 버튼 이벤트
    if (searchEmployeeBtn) {
        searchEmployeeBtn.addEventListener('click', openEmployeeSearchModal);
    }
    
    // 사원 정보 초기화 버튼 이벤트
    if (clearEmployeeBtn) {
        clearEmployeeBtn.addEventListener('click', clearEmployee);
    }
    
    // 비고 글자수 카운트 이벤트
    if (remarkTextarea) {
        remarkTextarea.addEventListener('input', updateRemarkCount);
        updateRemarkCount(); // 초기 카운트 설정
    }
    
    // 사원 검색 Enter 키 이벤트
    const employeeSearchInput = document.getElementById('employeeSearchInput');
    if (employeeSearchInput) {
        employeeSearchInput.addEventListener('keypress', handleEmployeeSearchEnter);
    }
    
    // 실시간 유효성 검사 설정
    setupRealTimeValidation();
    
    // 브라우저 뒤로가기 방지 설정
    preventBackNavigation();
    
    // 툴팁 초기화
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[title]'));
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
        new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    console.log('All event listeners attached successfully');
});