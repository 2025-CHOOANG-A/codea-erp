// storage_list.js - 창고 목록 페이지 전체 기능

// DOM 요소 참조
const searchBtn = document.getElementById("searchBtn");
const filterForm = document.getElementById("filterForm");
let targetWhIdForModal = null;

// 숫자 포맷팅 함수
function formatNumber(num) {
    if (num === null || num === undefined) return '0';
    return num.toLocaleString('ko-KR');
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

// 창고 상세보기 모달 표시
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

        // 모달 기본 정보 설정
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
            // 재고 관리 버튼을 클릭하면 재고 이동 모달로 이동
            inventoryBtn.onclick = function(e) {
                e.preventDefault();
                // 상세 모달 닫기
                bootstrap.Modal.getInstance(document.getElementById("detailModal")).hide();
                // 재고 이동 모달 열기 (해당 창고를 출발 창고로 미리 선택)
                targetWhIdForModal = item.whId;
                const transferModal = new bootstrap.Modal(document.getElementById('inventoryTransferModal'));
                transferModal.show();
                
                // 모달이 완전히 열린 후 창고 선택 및 재고 로드
                setTimeout(() => {
                    const fromSelect = document.getElementById('fromWarehouse');
                    fromSelect.value = item.whId;
                    loadFromWarehouseItems();
                }, 300);
            };
        }

        // 모달 표시
        const detailModal = new bootstrap.Modal(document.getElementById("detailModal"));
        detailModal.show();
        
    } catch (error) {
        console.error("Failed to fetch warehouse detail:", error);
        alert("창고 상세 정보를 가져오는 중 오류가 발생했습니다.");
    }
}

// 재고 목록 모달 표시
async function showInventoryListModal(whId, whName) {
    const modalEl = document.getElementById('inventoryListModal');
    if (!modalEl) return;
    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);

    document.getElementById('inventoryModalTitle').textContent = `[${whName}] 재고 목록`;
    const tableBody = document.getElementById('inventoryListTableBody');
    tableBody.innerHTML = '<tr><td colspan="5" class="text-center py-3"><div class="spinner-border spinner-border-sm"></div></td></tr>';
    
    try {
        const response = await fetch(`/inventory-transfer/api/warehouse/${whId}/items`);
        if (!response.ok) throw new Error(`Server responded with status: ${response.status}`);
        const items = await response.json();
        
        tableBody.innerHTML = '';
        if (items.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">보유 재고가 없습니다.</td></tr>';
        } else {
            items.forEach(item => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${item.itemCode || '-'}</td>
                    <td>${item.itemName || '-'}</td>
                    <td class="text-end">${formatNumber(item.currentStock)}</td>
                    <td class="text-end fw-bold">${formatNumber(item.availableStock)}</td>
                    <td>${item.itemUnit || '-'}</td>
                `;
                tableBody.appendChild(row);
            });
        }
    } catch (error) {
        console.error('Error loading inventory list:', error);
        tableBody.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-3">재고 목록을 불러오는 데 실패했습니다.</td></tr>';
    }
    modal.show();
}

// 출발 창고 재고 목록 로드
async function loadFromWarehouseItems() {
    const fromWhId = document.getElementById('fromWarehouse').value;
    const inventoryDiv = document.getElementById('fromWarehouseInventory');
    const tableBody = document.getElementById('fromWarehouseTableBody');
    
    // 선택 상태 초기화
    document.getElementById('selectedItemDisplay').value = '';
    document.getElementById('selectedItemId').value = '';
    document.getElementById('availableStock').textContent = '-';
    document.getElementById('transferQuantity').value = '';

    if (!fromWhId) {
        inventoryDiv.style.display = 'none';
        return;
    }
    
    inventoryDiv.style.display = 'block';
    tableBody.innerHTML = '<tr><td colspan="5" class="text-center py-3"><div class="spinner-border spinner-border-sm"></div></td></tr>';
    
    try {
        const response = await fetch(`/inventory-transfer/api/warehouse/${fromWhId}/items`);
        if (!response.ok) throw new Error(`Server responded with status: ${response.status}`);
        const items = await response.json();
        
        tableBody.innerHTML = '';
        if (items.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">이동 가능한 재고가 없습니다.</td></tr>';
        } else {
            items.forEach(item => {
                const row = document.createElement('tr');
                const itemJsonString = JSON.stringify(item).replace(/"/g, '&quot;');
                row.innerHTML = `
                    <td><input type="radio" name="transferItem" class="form-check-input" onchange="selectTransferItem(${itemJsonString})"></td>
                    <td>${item.itemCode || '-'}</td>
                    <td>${item.itemName || '-'}</td>
                    <td class="text-end fw-bold">${formatNumber(item.availableStock)}</td>
                    <td>${item.itemUnit || '-'}</td>
                `;
                tableBody.appendChild(row);
            });
        }
    } catch (error) {
        console.error('Error loading warehouse items:', error);
        tableBody.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-3">재고를 불러오는 데 실패했습니다.</td></tr>';
    }
}

// 이동할 품목 선택
function selectTransferItem(item) {
    document.getElementById('selectedItemId').value = item.itemId;
    document.getElementById('selectedItemDisplay').value = `[${item.itemCode}] ${item.itemName}`;
    document.getElementById('availableStock').textContent = formatNumber(item.availableStock);
    const quantityInput = document.getElementById('transferQuantity');
    quantityInput.max = item.availableStock;
    quantityInput.value = '1';
    quantityInput.focus();
}

// 재고 이동 실행
async function executeTransfer() {
    const payload = {
        itemId: parseInt(document.getElementById('selectedItemId').value) || null,
        fromWhId: parseInt(document.getElementById('fromWarehouse').value) || null,
        toWhId: parseInt(document.getElementById('toWarehouse').value) || null,
        quantity: parseInt(document.getElementById('transferQuantity').value) || null,
        remark: document.getElementById('transferRemark').value.trim(),
    };

    // 유효성 검사
    if (!payload.fromWhId) return alert('출발 창고를 선택해주세요.');
    if (!payload.toWhId) return alert('목적지 창고를 선택해주세요.');
    if (payload.fromWhId === payload.toWhId) return alert('출발 창고와 목적지 창고는 같을 수 없습니다.');
    if (!payload.itemId) return alert('이동할 품목을 선택해주세요.');
    if (!payload.quantity || payload.quantity <= 0) return alert('이동 수량을 1 이상 입력해주세요.');

    const maxQuantity = parseInt(document.getElementById('transferQuantity').max);
    if (payload.quantity > maxQuantity) {
        return alert(`요청 수량이 가용 재고(${formatNumber(maxQuantity)})를 초과할 수 없습니다.`);
    }

    if (!confirm("재고 이동을 요청하시겠습니까?")) return;

    try {
        const response = await fetch('/inventory-transfer/api/execute', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await response.json();
        
        if (response.ok && result.success) {
            alert(`재고 이동 요청이 등록되었습니다. (이동번호: ${result.transferNo})`);
            bootstrap.Modal.getInstance(document.getElementById('inventoryTransferModal')).hide();
            location.reload();
        } else {
            alert(`요청 실패: ${result.message || '알 수 없는 오류가 발생했습니다.'}`);
        }
    } catch (error) {
        console.error('Error during transfer execution:', error);
        alert('요청 처리 중 오류가 발생했습니다. 관리자에게 문의하세요.');
    }
}

// 창고 목록 로드 (재고 이동 모달용)
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

// 품목 검색 (키워드 기반)
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

// 품목 선택 (검색 결과에서)
function selectItem(itemId, itemCode, itemName, availableStock) {
    document.getElementById('selectedItemId').value = itemId;
    document.getElementById('itemSearch').value = `${itemCode} - ${itemName}`;
    document.getElementById('availableStock').textContent = formatNumber(availableStock);
    document.getElementById('transferQuantity').max = availableStock;
    document.getElementById('itemSearchResults').style.display = 'none';
}

// 재고 이동 모달 열기
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

// 페이지 로드 시 이벤트 리스너 등록
document.addEventListener("DOMContentLoaded", () => {
    // 검색 버튼 이벤트
    if (searchBtn) {
        searchBtn.addEventListener("click", function(e) {
            e.preventDefault();
            submitSearchForm();
        });
    }

    // 폼 제출 이벤트
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
    
    // 상세보기 버튼 이벤트
    document.querySelectorAll('.detail-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const whId = this.getAttribute('data-wh-id');
            if (whId) {
                showDetail(whId);
            }
        });
    });

    // 재고 목록 버튼 이벤트
    document.querySelectorAll('.inventory-list-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const row = this.closest('tr');
            const whId = row.dataset.whId;
            const whName = row.dataset.whName;
            showInventoryListModal(whId, whName);
        });
    });

    // 재고 이동 버튼 이벤트
    document.querySelectorAll('.transfer-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            targetWhIdForModal = this.closest('tr').dataset.whId;
            const transferModal = new bootstrap.Modal(document.getElementById('inventoryTransferModal'));
            transferModal.show();
        });
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

    // 재고 이동 모달 이벤트
    const transferModalEl = document.getElementById('inventoryTransferModal');
    if (transferModalEl) {
        // 모달이 열릴 때
        transferModalEl.addEventListener('show.bs.modal', function () {
            document.getElementById('transferForm').reset();
            document.getElementById('fromWarehouseInventory').style.display = 'none';

            if (targetWhIdForModal) {
                const fromSelect = document.getElementById('fromWarehouse');
                fromSelect.value = targetWhIdForModal;
                if (fromSelect.value === targetWhIdForModal) {
                    loadFromWarehouseItems();
                }
            }
        });

        // 모달이 닫힐 때
        transferModalEl.addEventListener('hidden.bs.modal', function () {
            targetWhIdForModal = null;
        });
    }
});