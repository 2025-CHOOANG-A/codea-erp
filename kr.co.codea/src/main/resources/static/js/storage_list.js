// storage_list.js - 간소화된 버전 (기존 스타일 유지)

const searchBtn = document.getElementById("searchBtn");
const filterForm = document.getElementById("filterForm");

// 검색 버튼 클릭 이벤트 리스너
if (searchBtn) {
    searchBtn.addEventListener("click", function(e) {
        e.preventDefault();
        submitSearchForm();
    });
}

// 검색 제출 함수
function submitSearchForm() {
    if (filterForm) {
        // 페이지를 1로 리셋
        const pageInput = filterForm.querySelector('input[name="page"]');
        if (pageInput) {
            pageInput.value = 1;
        }
        
        filterForm.action = "/storage";
        filterForm.method = "GET";
        filterForm.submit();
    } else {
        console.error("Error: filterForm not found.");
    }
}

// 상세보기 모달 표시
async function showDetail(whId) {
    if (!whId) {
        console.error("Error: whId is missing for detail view.");
        return;
    }

    try {
        const response = await fetch(`/storage/api/${whId}`);

        if (!response.ok) {
            if (response.status === 404) {
                alert("창고 상세 정보를 찾을 수 없습니다.");
            } else {
                alert("창고 상세 정보를 가져오는 데 실패했습니다: " + response.statusText);
            }
            return;
        }

        const item = await response.json();

        // 모달 내용 설정
        const modalBody = document.getElementById("modalDetailBody");
        modalBody.innerHTML = `
            <tr><th>창고 ID</th><td>${item.whId || '-'}</td></tr>
            <tr><th>창고 코드</th><td><span class="badge bg-secondary">${item.whCode || '-'}</span></td></tr>
            <tr><th>창고명</th><td class="fw-bold">${item.whName || '-'}</td></tr>
            <tr><th>주소</th><td>${item.address || '-'}</td></tr>
            <tr><th>상세 주소</th><td>${item.addressDetail || '-'}</td></tr>
            <tr><th>우편번호</th><td>${item.postCode || '-'}</td></tr>
            <tr><th>담당자</th><td>${item.empName || '-'} (${item.empNo || '-'})</td></tr>
            <tr><th>담당자 연락처</th><td>${item.empTel || '-'}</td></tr>
            <tr><th>비고</th><td>${item.remark || '-'}</td></tr>
        `;

        // 재고 현황 정보 설정
        const inventoryInfo = document.getElementById("modalInventoryInfo");
        if (item.totalItems && item.totalItems > 0) {
            inventoryInfo.innerHTML = `
                <div class="mb-2">
                    <small class="text-muted">총 품목 수</small>
                    <div class="h5 text-primary">${formatNumber(item.totalItems)}</div>
                </div>
                <div class="mb-2">
                    <small class="text-muted">현재 재고</small>
                    <div class="text-success">${formatNumber(item.currentStock || 0)}</div>
                </div>
                <div class="mb-2">
                    <small class="text-muted">가입고 예정</small>
                    <div class="text-warning">+${formatNumber(item.expectedStock || 0)}</div>
                </div>
                <div class="mb-2">
                    <small class="text-muted">가출고 예정</small>
                    <div class="text-danger">-${formatNumber(item.allocatedStock || 0)}</div>
                </div>
                <hr>
                <div class="mb-0">
                    <small class="text-muted">가용 재고</small>
                    <div class="h5 text-success">${formatNumber(item.availableStock || 0)}</div>
                </div>
            `;
        } else {
            inventoryInfo.innerHTML = `
                <div class="text-center text-muted">
                    <div class="mb-2">📦</div>
                    <small>재고 정보 없음</small>
                </div>
            `;
        }

        // 모달 하단 버튼 설정
        const editBtn = document.getElementById("editStorageBtn");
        const inventoryBtn = document.getElementById("manageInventoryBtn");
        
        if (editBtn) {
            editBtn.href = `/storage/${item.whId}/edit`;
        }
        if (inventoryBtn) {
            inventoryBtn.href = `/inventory?whId=${item.whId}`;
        }

        // Bootstrap 모달 인스턴스 생성 및 표시
        const detailModal = new bootstrap.Modal(document.getElementById("detailModal"));
        detailModal.show();
        
    } catch (error) {
        console.error("Failed to fetch warehouse detail:", error);
        alert("창고 상세 정보를 가져오는 중 오류가 발생했습니다.");
    }
}

// 숫자 포맷팅 함수
function formatNumber(num) {
    if (num === null || num === undefined) return '0';
    return new Intl.NumberFormat('ko-KR').format(num);
}

// DOMContentLoaded 이벤트 리스너
document.addEventListener("DOMContentLoaded", () => {
    // 폼 제출 시 기본 동작 방지
    if (filterForm) {
        filterForm.addEventListener("submit", function (e) {
            e.preventDefault();
            submitSearchForm();
        });
    }
    
    // 페이지 크기 변경 이벤트
    const sizeSelect = document.querySelector('select[name="size"]');
    if (sizeSelect) {
        sizeSelect.addEventListener("change", function() {
            const pageInput = filterForm.querySelector('input[name="page"]');
            if (pageInput) {
                pageInput.value = 1;
            }
            submitSearchForm();
        });
    }
    
    // "상세 보기" 버튼 클릭 이벤트
    document.querySelector("#warehouseTable tbody")?.addEventListener("click", function (e) {
        if (e.target.closest(".detail-btn")) {
            const btn = e.target.closest(".detail-btn");
            const whId = btn.dataset.whId;
            showDetail(whId);
        }
    });

    // Enter 키로 검색
    const searchInput = document.getElementById("warehouseCodeInput");
    if (searchInput) {
        searchInput.addEventListener("keypress", function(e) {
            if (e.key === "Enter") {
                e.preventDefault();
                submitSearchForm();
            }
        });
    }
});

// 재고 이동 관련 JavaScript
let warehouseList = [];

// 재고 이동 모달이 열릴 때 창고 목록 로드
document.addEventListener('DOMContentLoaded', function() {
    const transferModal = document.getElementById('inventoryTransferModal');
    if (transferModal) {
        transferModal.addEventListener('show.bs.modal', function() {
            loadWarehouses();
        });
    }
});

// 창고 목록 로드
function loadWarehouses() {
    const fromSelect = document.getElementById('fromWarehouse');
    const toSelect = document.getElementById('toWarehouse');
    
    // 기존 옵션 초기화
    fromSelect.innerHTML = '<option value="">창고를 선택하세요</option>';
    toSelect.innerHTML = '<option value="">창고를 선택하세요</option>';
    
    // 현재 페이지의 창고 데이터에서 옵션 생성
    document.querySelectorAll('#warehouseTable tbody tr').forEach(row => {
        const whId = row.querySelector('.detail-btn')?.dataset.whId;
        const whName = row.cells[2]?.textContent?.trim();
        const whCode = row.cells[1]?.querySelector('.badge')?.textContent?.trim();
        
        if (whId && whName && whCode) {
            const option = `<option value="${whId}">${whCode} - ${whName}</option>`;
            fromSelect.innerHTML += option;
            toSelect.innerHTML += option;
        }
    });
}

// 품목 검색
async function searchItems() {
    const fromWhId = document.getElementById('fromWarehouse').value;
    const keyword = document.getElementById('itemSearch').value.trim();
    
    if (!fromWhId) {
        alert('먼저 출발 창고를 선택하세요.');
        return;
    }
    
    if (!keyword) {
        alert('검색할 품목명 또는 품목코드를 입력하세요.');
        return;
    }
    
    try {
        const response = await fetch(`/inventory-transfer/api/warehouse/${fromWhId}/items?keyword=${encodeURIComponent(keyword)}`);
        
        if (!response.ok) {
            throw new Error('품목 검색 실패');
        }
        
        const items = await response.json();
        displaySearchResults(items);
        
    } catch (error) {
        console.error('품목 검색 실패:', error);
        alert('품목 검색 중 오류가 발생했습니다.');
    }
}

// 검색 결과 표시
function displaySearchResults(items) {
    const tableBody = document.getElementById('itemSearchTableBody');
    const resultsDiv = document.getElementById('itemSearchResults');
    
    tableBody.innerHTML = '';
    
    if (items.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">검색 결과가 없습니다.</td></tr>';
    } else {
        items.forEach(item => {
            const row = `
                <tr>
                    <td>${item.itemCode || '-'}</td>
                    <td>${item.itemName || '-'}</td>
                    <td class="text-end">${formatNumber(item.availableStock)} ${item.itemUnit || ''}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="selectItem(${item.itemId}, '${item.itemCode}', '${item.itemName}', ${item.availableStock})">
                            선택
                        </button>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });
    }
    
    resultsDiv.style.display = 'block';
}

// 품목 선택
function selectItem(itemId, itemCode, itemName, availableStock) {
    document.getElementById('selectedItemId').value = itemId;
    document.getElementById('itemSearch').value = `${itemCode} - ${itemName}`;
    document.getElementById('availableStock').textContent = formatNumber(availableStock);
    document.getElementById('transferQuantity').max = availableStock;
    document.getElementById('itemSearchResults').style.display = 'none';
}

// 재고 이동 실행
async function executeTransfer() {
    const formData = {
        itemId: parseInt(document.getElementById('selectedItemId').value),
        fromWhId: parseInt(document.getElementById('fromWarehouse').value),
        toWhId: parseInt(document.getElementById('toWarehouse').value),
        quantity: parseInt(document.getElementById('transferQuantity').value),
        remark: document.getElementById('transferRemark').value,
        empId: 1 // 임시값 - 실제로는 세션에서 가져와야 함
    };
    
    // 유효성 검사
    if (!formData.itemId || !formData.fromWhId || !formData.toWhId || !formData.quantity) {
        alert('모든 필수 항목을 입력하세요.');
        return;
    }
    
    if (formData.fromWhId === formData.toWhId) {
        alert('출발 창고와 목적지 창고가 같을 수 없습니다.');
        return;
    }
    
    try {
        // 유효성 검사 API 호출
        const validateResponse = await fetch('/inventory-transfer/api/validate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });
        
        const validateResult = await validateResponse.json();
        
        if (!validateResult.success) {
            alert(validateResult.message);
            return;
        }
        
        // 이동 실행 API 호출
        const executeResponse = await fetch('/inventory-transfer/api/execute', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });
        
        const executeResult = await executeResponse.json();
        
        if (executeResult.success) {
            alert(`재고 이동이 완료되었습니다.${executeResult.transferNo ? '\n이동번호: ' + executeResult.transferNo : ''}`);
            
            // 모달 닫기 및 페이지 새로고침
            bootstrap.Modal.getInstance(document.getElementById('inventoryTransferModal')).hide();
            location.reload();
        } else {
            alert(executeResult.message);
        }
        
    } catch (error) {
        console.error('재고 이동 실행 실패:', error);
        alert('재고 이동 중 오류가 발생했습니다.');
    }
}

// 재고 이동 모달 열기 함수
function openTransferModal(fromWhId = null) {
    const modal = new bootstrap.Modal(document.getElementById('inventoryTransferModal'));
    
    // 출발 창고가 지정된 경우 미리 선택
    if (fromWhId) {
        setTimeout(() => {
            document.getElementById('fromWarehouse').value = fromWhId;
        }, 100);
    }
    
    modal.show();
}