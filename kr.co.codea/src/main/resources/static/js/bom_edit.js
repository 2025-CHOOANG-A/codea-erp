// ================================
// DOM 로드 후 실행되는 메인 코드
// ================================
document.addEventListener("DOMContentLoaded", function() {
  console.log("DOM 로드 완료");
  
  // ================================
  // 체크박스 전체 선택/해제 기능 (기존 유지)
  // ================================
  const selectAllCheckbox = document.getElementById("selectAllMaterial");
  
  if (!selectAllCheckbox) {
    console.error("selectAllMaterial 체크박스를 찾을 수 없습니다.");
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
  // 자재 관련 버튼 이벤트 (기존 유지)
  // ================================
  
  // 자재 추가 버튼
  console.log("자재 추가 버튼 찾는 중...");
  const addMaterialBtn = document.querySelector(".btn-add-material");
  console.log("자재 추가 버튼:", addMaterialBtn);
  
  if (addMaterialBtn) {
    console.log("자재 추가 버튼 발견! 이벤트 추가");
    addMaterialBtn.addEventListener("click", function(e) {
      e.preventDefault();
      e.stopPropagation();
      console.log("자재 추가 버튼 클릭됨! (preventDefault 적용)");
      openMaterialAddModal();
    });
  } else {
    console.error("자재 추가 버튼을 찾을 수 없습니다!");
  }
  
  // 자재 검색 버튼 이벤트
  const materialSearchBtn = document.getElementById("materialSearchBtn");
  if (materialSearchBtn) {
    materialSearchBtn.addEventListener("click", function(e) {
      e.preventDefault();
      e.stopPropagation();
      console.log("자재 검색 버튼 클릭 (preventDefault 적용)");
      openMaterialSearchModal();
    });
  }

  // 자재 추가 폼 제출 이벤트 (JSON으로 수정)
  const materialAddForm = document.getElementById("materialAddForm");
  if (materialAddForm) {
    materialAddForm.addEventListener("submit", function(e) {
      e.preventDefault();
      e.stopPropagation();
      console.log("자재 폼 제출 (JSON 방식)");
      submitMaterialForm();
    });
  }
  
  // 자재 삭제 버튼 (기존 FormData 유지)
  const deleteMaterialBtn = document.querySelector(".btn-delete-material");
  if (deleteMaterialBtn) {
    deleteMaterialBtn.addEventListener("click", function(e) {
      e.preventDefault();
      e.stopPropagation();
      
      const checkedBoxes = document.querySelectorAll('#materialTable tbody input[type="checkbox"]:checked');
      
      if (checkedBoxes.length === 0) {
        alert("삭제할 자재를 선택하세요.");
        return;
      }
      
      if (!confirm("정말 삭제하시겠습니까?")) {
        return;
      }
      
      // 선택된 자재 코드 수집
      const materialCodes = Array.from(checkedBoxes).map(cb => cb.value);
      // BOM 코드는 폼에서 가져오기
      const bomCodeInput = document.querySelector('#bomForm input[name="bomCode"]');
      const bomCode = bomCodeInput ? bomCodeInput.value : '';
      
      console.log("삭제 요청:", { bomCode, materialCodes });
      
      // FormData 방식으로 서버에 요청 (기존 유지)
      const formData = new FormData();
      formData.append('bomCode', bomCode);
      materialCodes.forEach(code => {
        formData.append('materialCodes', code);
      });
      
      fetch('/bom/deleteMaterials', {
        method: 'POST',
        body: formData
      })
      .then(response => response.json())
      .then(data => {
        console.log("서버 응답:", data);
        if (data.success) {
          alert("선택된 자재가 삭제되었습니다.");
          window.location.reload();
        } else {
          alert('삭제 실패: ' + data.message);
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('삭제 중 오류가 발생했습니다.');
      });
    });
  }
  
  // ================================
  // 기타 버튼 이벤트 (기존 유지)
  // ================================
  
  // 목록 버튼
  const btnList = document.getElementById("btnList");
  if (btnList) {
    btnList.addEventListener("click", function() {
      window.location.href = "/bom/bom_list";
    });
  }
  
  // BOM 저장 폼
  const bomForm = document.getElementById("bomForm");
  if (bomForm) {
    bomForm.addEventListener("submit", function(e) {
      e.preventDefault();
      
      // 자료 개수 확인
      const materialRows = document.querySelectorAll('#materialTable tbody tr');
      const emptyMessage = document.querySelector('#materialTable tbody tr td[colspan]');
      
      if (materialRows.length === 0 || emptyMessage) {
        alert("자재를 1개 이상 추가하세요.");
        return;
      }
      
      // 실제 폼 제출
      if (confirm("BOM 수정을 완료하시겠습니까?")) {
        alert("BOM 수정이 완료되었습니다.");
        window.location.href = "/bom/bom_list";
      }
    });
  }
  
  console.log("모든 이벤트 리스너 등록 완료");
}); // DOMContentLoaded 끝

// ================================
// 자재 추가 모달 열기 (기존 유지)
// ================================
function openMaterialAddModal() {
  console.log("자재 추가 모달 열기 함수 실행");
  
  // 모달 찾기
  const modal = document.getElementById("materialAddModal");
  console.log("모달 엘리먼트:", modal);
  
  if (!modal) {
    console.error("materialAddModal을 찾을 수 없습니다!");
    alert("자재 추가 모달을 찾을 수 없습니다.");
    return;
  }
  
  // 폼 초기화
  const form = document.getElementById("materialAddForm");
  console.log("폼 엘리먼트:", form);
  
  if (form) {
    console.log("폼 초기화 중...");
    form.reset();
    
    // 모달 타이틀 변경
    const modalTitle = modal.querySelector('.modal-title');
    if (modalTitle) {
      modalTitle.textContent = '자재 추가';
      console.log("모달 타이틀 설정 완료");
    }
    
    // BOM 코드 설정
    const bomCodeInput = document.querySelector('#bomForm input[name="bomCode"]');
    console.log("BOM 코드 입력 필드:", bomCodeInput);
    
    if (form.bomCode && bomCodeInput) {
      form.bomCode.value = bomCodeInput.value.trim();
      console.log("BOM 코드 설정:", bomCodeInput.value);
    }
    
    // editIdx 초기화 (추가 모드)
    if (form.editIdx) {
      form.editIdx.value = '';
    }
  }
  
  try {
    console.log("Bootstrap 모달 생성 중...");
    // Bootstrap 모달 열기
    const bootstrapModal = new bootstrap.Modal(modal, {
      backdrop: 'static',
      keyboard: false
    });
    
    console.log("모달 표시 중...");
    bootstrapModal.show();
    console.log("모달 열기 성공!");
    
    // 모달이 닫힐 때 backdrop 정리 (한 번만 실행)
    modal.addEventListener('hidden.bs.modal', function() {
      console.log("모달 닫힘 - cleanup");
      // backdrop 제거
      document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
        backdrop.remove();
      });
      
      // body 스타일 리셋
      document.body.classList.remove('modal-open');
      document.body.style.overflow = '';
      document.body.style.paddingRight = '';
    }, { once: true });
    
  } catch (error) {
    console.error("모달 열기 중 오류:", error);
    alert("모달을 열 수 없습니다: " + error.message);
  }
}

// ================================
// 자재 검색 모달 열기 (기존 유지)
// ================================
function openMaterialSearchModal() {
  console.log("자재 검색 모달 열기");
  
  // HTML에 있는 모달만 사용
  const existingModal = document.getElementById('materialSearchModal');
  if (!existingModal) {
    console.error("materialSearchModal을 HTML에서 찾을 수 없습니다.");
    alert("자재 검색 모달을 찾을 수 없습니다.");
    return;
  }
  
  console.log("HTML 모달 사용");
  
  // 기존 모달 열기
  const modal = new bootstrap.Modal(existingModal);
  modal.show();
  
  // 기존 테이블의 행들에 클릭 이벤트 확인/추가 (Thymeleaf로 렌더링된 데이터)
  const existingRows = existingModal.querySelectorAll('#modalMaterialResult tr[data-code]');
  console.log("HTML에서 찾은 자재 행 수:", existingRows.length);
  
  existingRows.forEach((row, index) => {
    if (!row.hasAttribute('data-click-added')) {
      row.addEventListener('click', function() {
        selectMaterial(this);
      });
      row.setAttribute('data-click-added', 'true');
      console.log(`행 ${index} 클릭 이벤트 추가`);
    }
  });
  
  // 검색 기능 추가
  const searchInput = existingModal.querySelector('#materialKeyword');
  if (searchInput) {
    // 기존 이벤트 리스너 제거 후 새로 추가
    const newSearchInput = searchInput.cloneNode(true);
    searchInput.parentNode.replaceChild(newSearchInput, searchInput);
    
    newSearchInput.addEventListener('input', function(e) {
      filterMaterials(e.target.value);
    });
    
    console.log("검색 기능 추가 완료");
  }
}

// ================================
// 자재 목록 필터링 (기존 유지)
// ================================
function filterMaterials(keyword) {
  console.log("검색 키워드:", keyword);
  
  // HTML 모달의 테이블에서 검색
  const modal = document.getElementById('materialSearchModal');
  if (!modal) {
    console.error("materialSearchModal을 찾을 수 없음");
    return;
  }
  
  // Thymeleaf로 생성된 행들 찾기
  const rows = modal.querySelectorAll('#modalMaterialResult tr[data-code]');
  let visibleCount = 0;
  
  console.log("검색 대상 행 수:", rows.length);
  
  rows.forEach((row, index) => {
    const materialCode = row.querySelector('td:nth-child(1)')?.textContent?.trim() || '';
    const materialName = row.querySelector('td:nth-child(2)')?.textContent?.trim() || '';
    
    const isVisible = keyword === '' || 
        materialCode.toLowerCase().includes(keyword.toLowerCase()) ||
        materialName.toLowerCase().includes(keyword.toLowerCase());
    
    if (isVisible) {
      row.style.display = '';
      visibleCount++;
    } else {
      row.style.display = 'none';
    }
  });
  
  console.log(`검색 결과: ${visibleCount}개 표시`);
}

// ================================
// 자재 선택 (기존 유지)
// ================================
function selectMaterial(row) {
  console.log("자재 선택:", row);
  
  // HTML data 속성에서 직접 값 가져오기
  const materialCode = row.getAttribute('data-code');
  const materialName = row.getAttribute('data-name');
  const spec = row.getAttribute('data-spec');
  const unitName = row.getAttribute('data-unit');
  const price = row.getAttribute('data-price');
  
  console.log("선택된 자재:", { materialCode, materialName, spec, unitName, price });
  
  // 자재 추가 모달의 필드에 값 설정
  const form = document.getElementById("materialAddForm");
  if (form) {
    if (form.materialCode) form.materialCode.value = materialCode || '';
    if (form.materialName) form.materialName.value = materialName || '';
    if (form.spec) form.spec.value = spec || '';
    if (form.unit) form.unit.value = unitName || 'EA';
    if (form.price) form.price.value = price || '';
    
    console.log("폼 필드 설정 완료");
  } else {
    console.error("materialAddForm을 찾을 수 없음");
  }
  
  // 검색 모달 닫기
  const searchModal = bootstrap.Modal.getInstance(document.getElementById('materialSearchModal'));
  if (searchModal) {
    searchModal.hide();
    console.log("검색 모달 닫기");
  }
}

// ================================
// 자재 폼 제출 (JSON으로 수정)
// ================================
function submitMaterialForm() {
  console.log("자재 폼 제출 시작 (JSON 방식)");
  
  const form = document.getElementById("materialAddForm");
  if (!form) {
    console.error("materialAddForm을 찾을 수 없음");
    return;
  }
  
  // 폼 데이터 검증
  const materialCode = form.materialCode.value.trim();
  const materialName = form.materialName.value.trim();
  const spec = form.spec.value.trim();
  const price = form.price.value.trim();
  const quantity = form.qty.value.trim();
  
  console.log("폼 데이터:", { materialCode, materialName, spec, price, quantity });
  
  if (!materialCode || !materialName || !spec || !price || !quantity) {
    alert("모든 필수 항목을 입력하세요.");
    return;
  }
  
  // JSON 데이터 생성
  const requestData = {
    bomCode: form.bomCode.value,
    materialCode: materialCode,
    materialName: materialName,
    spec: spec,
    unit: form.unit.value,
    price: parseInt(price),
    quantity: parseInt(quantity),
    lossRate: 0
  };
  
  console.log("서버 요청 시작 (JSON):", requestData);
  
  // JSON으로 서버에 요청
  fetch('/bom/addMaterial', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestData)
  })
  .then(response => response.json())
  .then(data => {
    console.log("서버 응답:", data);
    if (data.success) {
      alert("자재가 추가되었습니다.");
      
      // 모달 닫기
      const modal = bootstrap.Modal.getInstance(document.getElementById("materialAddModal"));
      if (modal) {
        modal.hide();
      }
      
      // backdrop 정리 후 페이지 새로고침
      setTimeout(() => {
        document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
          backdrop.remove();
        });
        document.body.classList.remove('modal-open');
        document.body.style.overflow = '';
        document.body.style.paddingRight = '';
        
        window.location.reload(); // 페이지 새로고침으로 업데이트된 자재 목록 표시
      }, 100);
      
    } else {
      alert('자재 추가 실패: ' + data.message);
    }
  })
  .catch(error => {
    console.error('Error:', error);
    alert('자재 추가 중 오류가 발생했습니다.');
  });
}

// ================================
// 자재 수정 모달 처리 (기존 유지)
// ================================
document.addEventListener('DOMContentLoaded', function() {
  const materialAddModal = document.getElementById('materialAddModal');
  if (materialAddModal) {
    materialAddModal.addEventListener('show.bs.modal', function(event) {
      const button = event.relatedTarget; // 모달을 연 버튼
      console.log("모달 show 이벤트, 버튼:", button);
      
      if (button && button.dataset.index !== undefined) {
        // 수정 모드
        const idx = Number(button.dataset.index);
        console.log("자재 수정 모드, 인덱스:", idx);
        
        // 모달 타이틀 변경
        const modalTitle = this.querySelector('.modal-title');
        if (modalTitle) {
          modalTitle.textContent = '자재 수정';
        }
        
        // 테이블에서 데이터 가져와서 폼에 설정
        const materialTable = document.getElementById('materialTable');
        if (materialTable) {
          const rows = materialTable.querySelectorAll('tbody tr:not([th\\:if])'); // Thymeleaf 조건부 행 제외
          if (rows[idx]) {
            const cells = rows[idx].querySelectorAll('td');
            const form = this.querySelector('#materialAddForm');
            
            if (cells.length >= 8 && form) {
              // 테이블 셀에서 데이터 추출 (체크박스 제외하고 1번째부터)
              form.bomCode.value = cells[1]?.textContent?.trim() || '';
              form.materialCode.value = cells[2]?.textContent?.trim() || '';
              form.materialName.value = cells[3]?.textContent?.trim() || '';
              form.spec.value = cells[4]?.textContent?.trim() || '';
              form.unit.value = cells[5]?.textContent?.trim() || 'EA';
              form.price.value = cells[6]?.textContent?.trim() || '';
              form.qty.value = cells[7]?.textContent?.trim() || '';
              form.editIdx.value = idx;
              
              console.log("수정 데이터 설정 완료");
            }
          }
        }
      } else {
        // 추가 모드
        console.log("자재 추가 모드");
        
        const modalTitle = this.querySelector('.modal-title');
        if (modalTitle) {
          modalTitle.textContent = '자재 추가';
        }
        
        const form = this.querySelector('#materialAddForm');
        if (form) {
          // BOM 코드만 설정, 나머지는 리셋
          const bomCodeInput = document.querySelector('#bomForm input[name="bomCode"]');
          if (bomCodeInput && form.bomCode) {
            form.bomCode.value = bomCodeInput.value.trim();
          }
          if (form.editIdx) {
            form.editIdx.value = '';
          }
        }
      }
    });
  }
});