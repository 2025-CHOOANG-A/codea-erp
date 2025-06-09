
// ================================
// 샘플 데이터
// ================================
const products = [
  { code: "P001", name: "제품A", spec: "100x200", unit: "EA" },
  { code: "P002", name: "제품B", spec: "50x100", unit: "SET" },
  { code: "P003", name: "제품C", spec: "200x300", unit: "BOX" },
  { code: "P004", name: "제품D", spec: "150x150", unit: "EA" },
];

// ================================
// DOM 로드 후 실행되는 메인 코드
// ================================
document.addEventListener("DOMContentLoaded", function() {
  
  // ================================
  // 체크박스 전체 선택/해제 기능
  // ================================
  const selectAllCheckbox = document.getElementById("selectAllMaterial");
  
  if (!selectAllCheckbox) {
    console.error("selectAllMaterial 체크박스를 찾을 수 없습니다. HTML에 id='selectAllMaterial' 추가 필요");
  } else {
    // 전체 선택/해제 이벤트
    selectAllCheckbox.addEventListener("change", function(e) {
      const materialCheckboxes = document.querySelectorAll('#materialTable tbody input[type="checkbox"]');
      materialCheckboxes.forEach(function(checkbox) {
        checkbox.checked = e.target.checked;
      });
    });
    
    // 개별 체크박스 변경 시 전체 선택 체크박스 상태 업데이트
    document.getElementById('materialTable').addEventListener('change', function(e) {
      if (e.target.type === 'checkbox' && e.target !== selectAllCheckbox) {
        updateSelectAllCheckbox();
      }
    });
    
    // 전체 선택 체크박스 상태 업데이트 함수
    function updateSelectAllCheckbox() {
      const materialCheckboxes = document.querySelectorAll('#materialTable tbody input[type="checkbox"]');
      const checkedBoxes = document.querySelectorAll('#materialTable tbody input[type="checkbox"]:checked');
      
      if (materialCheckboxes.length === 0) {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = false;
      } else if (checkedBoxes.length === materialCheckboxes.length) {
        selectAllCheckbox.checked = true;
        selectAllCheckbox.indeterminate = false;
      } else if (checkedBoxes.length === 0) {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = false;
      } else {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = true;
      }
    }
    
    // 페이지 로드 시 초기 상태 설정
    updateSelectAllCheckbox();
  }
  
  // ================================
  // 자재 관련 버튼 이벤트
  // ================================
  
  // 자재 추가 버튼
  const addMaterialBtn = document.querySelector(".btn-add-material");
  if (addMaterialBtn) {
    addMaterialBtn.addEventListener("click", function() {
      console.log("자재 추가 버튼 클릭");
      if (typeof openMaterialAddModal === 'function') {
        openMaterialAddModal(); // 추가 모드
      } else {
        alert("자재 추가 모달이 준비되지 않았습니다.");
      }
    });
  }
  
  // 자재 삭제 버튼
  const deleteMaterialBtn = document.querySelector(".btn-delete-material");
  if (deleteMaterialBtn) {
    deleteMaterialBtn.addEventListener("click", function() {
      const checkedBoxes = document.querySelectorAll('#materialTable tbody input[type="checkbox"]:checked');
      
      if (checkedBoxes.length === 0) {
        alert("삭제할 자재를 선택하세요.");
        return;
      }
      
      if (!confirm("정말 삭제하시겠습니까?")) {
        return;
      }
      
      // 선택된 자재 처리
      const selectedMaterials = Array.from(checkedBoxes).map(cb => cb.value);
      console.log("삭제할 자재들:", selectedMaterials);
      
      // Thymeleaf 환경에서는 서버로 삭제 요청을 보내야 함
      // 임시로 DOM에서 제거 (실제로는 페이지 새로고침 필요)
      checkedBoxes.forEach(cb => {
        const row = cb.closest('tr');
        if (row) {
          row.remove();
        }
      });
      
      // 전체 선택 체크박스 상태 업데이트
      if (selectAllCheckbox) {
        updateSelectAllCheckbox();
      }
    });
  }
  
  // 자재 수정 버튼 (이벤트 위임 사용)
  document.querySelector("#materialTable tbody").addEventListener("click", function(e) {
    if (e.target.classList.contains("btn-edit-material")) {
      // Thymeleaf에서는 data-index 사용
      const idx = Number(e.target.dataset.index || e.target.dataset.idx);
      console.log("수정 버튼 클릭:", idx);
      
      if (typeof openMaterialAddModal === 'function') {
        openMaterialAddModal(idx);
      } else {
        alert("자재 수정 모달이 준비되지 않았습니다.");
      }
    }
  });
  
  // ================================
  // 기타 버튼 이벤트
  // ================================
  
  // 목록 버튼
  const btnList = document.getElementById("btnList");
  if (btnList) {
    btnList.addEventListener("click", function() {
      // 실제로는 페이지 이동
      window.location.href = "/bom/list"; // 또는 적절한 URL
    });
  }
  
  // BOM 저장 폼
  const bomForm = document.getElementById("bomForm");
  if (bomForm) {
    bomForm.addEventListener("submit", function(e) {
      e.preventDefault();
      
      // Thymeleaf 환경에서는 실제 자료 개수 확인
      const materialRows = document.querySelectorAll('#materialTable tbody tr');
      const emptyMessage = document.querySelector('#materialTable tbody tr td[colspan]');
      
      if (materialRows.length === 0 || emptyMessage) {
        alert("자재를 1개 이상 추가하세요.");
        return;
      }
      
      // 실제 폼 제출
      if (confirm("BOM을 저장하시겠습니까?")) {
        this.submit(); // 실제 제출
      }
    });
  }
  
});

// ================================
// Thymeleaf용 자재 모달 함수
// ================================
function openMaterialAddModal(editIndex = null) {
  const modal = document.getElementById("materialAddModal");
  if (!modal) {
    console.error("materialAddModal을 찾을 수 없습니다.");
    return;
  }
  
  const form = document.getElementById("materialAddForm");
  const modalLabel = document.getElementById("materialAddModalLabel");
  
  if (form) {
    form.reset();
  }
  
  // hidden field에 편집 인덱스 저장
  const editIdxField = document.getElementById("editIdx");
  if (editIdxField) {
    editIdxField.value = editIndex !== null ? editIndex : "";
  }
  
  if (editIndex !== null) {
    // 수정 모드
    if (modalLabel) {
      modalLabel.textContent = "자재 수정";
    }
    
    // 해당 행의 데이터로 폼 채우기 (Thymeleaf 렌더링된 테이블에서)
    const rows = document.querySelectorAll('#materialTable tbody tr');
    const targetRow = rows[editIndex];
    
    if (targetRow && form) {
      const cells = targetRow.querySelectorAll('td');
      if (cells.length >= 8) {
        // 체크박스 제외하고 데이터 추출
        if (form.bomCode) form.bomCode.value = cells[1].textContent.trim();
        if (form.materialCode) form.materialCode.value = cells[2].textContent.trim();
        if (form.materialName) form.materialName.value = cells[3].textContent.trim();
        if (form.spec) form.spec.value = cells[4].textContent.trim();
        if (form.unit) form.unit.value = cells[5].textContent.trim();
        if (form.price) form.price.value = cells[6].textContent.replace(/,/g, '').trim();
        if (form.qty) form.qty.value = cells[7].textContent.trim();
      }
    }
  } else {
    // 추가 모드
    if (modalLabel) {
      modalLabel.textContent = "자재 추가";
    }
    
    // BOM 코드 자동 생성
    const bomCodeInput = document.getElementById("bomCodeInput");
    let baseBomCode = "BOM";
    if (bomCodeInput) {
      baseBomCode = bomCodeInput.value.trim() || "BOM";
    }
    
    const materialCount = document.querySelectorAll('#materialTable tbody tr').length;
    const newBomCode = baseBomCode + "-" + String(materialCount + 1).padStart(2, "0");
    
    if (form && form.bomCode) {
      form.bomCode.value = newBomCode;
    }
  }
  
  // 모달 열기
  const bootstrapModal = new bootstrap.Modal(modal);
  bootstrapModal.show();
}

// ================================
// 자재 폼 제출 처리 (모달)
// ================================
const materialAddForm = document.getElementById("materialAddForm");
if (materialAddForm) {
  materialAddForm.addEventListener("submit", function(e) {
    e.preventDefault();
    
    // 폼 데이터 검증
    const form = e.target;
    const materialCode = form.materialCode.value.trim();
    const materialName = form.materialName.value.trim();
    const spec = form.spec.value.trim();
    
    if (!materialCode || !materialName || !spec) {
      alert("필수 항목을 입력하세요.");
      return;
    }
    
    // 실제로는 서버에 데이터 전송
    console.log("자재 저장:", {
      bomCode: form.bomCode.value,
      materialCode: materialCode,
      materialName: materialName,
      spec: spec,
      unit: form.unit.value,
      price: Number(form.price.value),
      qty: Number(form.qty.value)
    });
    
    // 모달 닫기
    const modal = bootstrap.Modal.getInstance(document.getElementById("materialAddModal"));
    if (modal) {
      modal.hide();
    }
    
    // 실제로는 페이지 새로고침이나 AJAX로 테이블 업데이트
    alert("자재가 저장되었습니다. 페이지를 새로고침합니다.");
    // window.location.reload();
  });
}