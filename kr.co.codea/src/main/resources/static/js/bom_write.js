
	 // BOM 등록 데이터 - 빈 자재 리스트로 시작
	 let bomDetail = {
	   bomCode: "",
	   productCode: "",
	   productName: "",
	   spec: "",
	   unit: "",
	   note: "",
	   materials: []
	 };

	 // 완제품 검색 관련 - 샘플 데이터 제거하고 서버 데이터 사용
	 const productSearchInput = document.getElementById("productSearchInput");
	 const productSearchResult = document.getElementById("productSearchResult");
	 const productNameInput = document.getElementById("productNameInput");
	 const specInput = document.getElementById("specInput");
	 const unitInput = document.getElementById("unitInput");

	 // ============ 자재 목록 테이블 렌더링 ============
	 function renderMaterialTable() {
	   const tbody = document.querySelector("#materialTable tbody");
	   
	   if (!tbody) {
	     console.error("자재 테이블을 찾을 수 없습니다.");
	     return;
	   }
	   
	   tbody.innerHTML = "";
	   
	   bomDetail.materials.forEach((mat, idx) => {
	     const tr = document.createElement("tr");
	     tr.innerHTML = `
	       <td><input type="checkbox" class="rowCheckbox1" data-idx="${idx}" /></td>
	       <td>${mat.bomCode}</td>
	       <td>${mat.materialCode}</td>
	       <td>${mat.materialName}</td>
	       <td>${mat.spec}</td>
	       <td>${mat.j_unitName}</td>
	       <td>${mat.price.toLocaleString()}</td>
	       <td>${mat.quantity}</td>
	     `;
	     tbody.appendChild(tr);
	   });
	   
	   const selectAllCheckbox = document.getElementById("selectAllMaterial");
	   if (selectAllCheckbox) {
	     selectAllCheckbox.checked = false;
	   }
	 }

	 
	 // ============ 체크박스 전체 선택 기능 ============
	 const selectAllMaterial = document.getElementById("selectAllMaterial");
	 if (selectAllMaterial) {
	   selectAllMaterial.addEventListener("change", function(e) {
	     const table = selectAllMaterial.closest("table"); 
	     if (!table) return;
	     
	     const checkboxes1 = table.querySelectorAll("tbody input.rowCheckbox1[type='checkbox']");
	     checkboxes1.forEach(cb => {
	       cb.checked = selectAllMaterial.checked;
	     });
	   });
	 }

	 // ============ 완제품 검색 모달 ============
	 const productModal = new bootstrap.Modal(document.getElementById('productSearchModal'));

	 // 완제품 검색 모달 열기
	 const productSearchBtn = document.getElementById("productSearchBtn");
	 if (productSearchBtn) {
	   productSearchBtn.addEventListener("click", () => {
	     productModal.show();
	     const productKeyword = document.getElementById("productKeyword");
	     if (productKeyword) {
	       productKeyword.focus();
	     }
	     
	     // 모달이 열릴 때 기존 Thymeleaf 행들에 클릭 이벤트 연결
	     setTimeout(() => {
	       const rows = document.querySelectorAll('#modalProductResult tr[data-code]');
	       rows.forEach(row => {
	         row.style.cursor = 'pointer';
	         row.addEventListener('click', function() {
	           selectProduct(this);
	         });
	       });
	     }, 100);
	   });
	 }

	 // 완제품 검색 - 기존 Thymeleaf 데이터 필터링만
	 const productKeywordInput = document.getElementById("productKeyword");
	 if (productKeywordInput) {
	   productKeywordInput.addEventListener("keydown", function(e) {
	     if (e.key === "Enter") {
	       e.preventDefault();
	       const keyword = this.value.toLowerCase().trim();
	       const modalResult = document.getElementById("modalProductResult");
	       
	       if (!modalResult) return;
	       
	       // Thymeleaf로 렌더링된 행들을 필터링
	       const allRows = modalResult.querySelectorAll('tr[data-code]');
	       
	       allRows.forEach(row => {
	         const code = row.getAttribute('data-code').toLowerCase();
	         const name = row.getAttribute('data-name').toLowerCase();
	         
	         if (!keyword || code.includes(keyword) || name.includes(keyword)) {
	           row.style.display = '';  // 보이기
	         } else {
	           row.style.display = 'none';  // 숨기기
	         }
	       });
	     }
	   });
	 }

	 // 테이블 자체에 이벤트 위임 사용 (더 안정적인 방법)
	 const modalProductResult = document.getElementById('modalProductResult');
	 if (modalProductResult) {
	   modalProductResult.addEventListener('click', function(e) {
	     const row = e.target.closest('tr');
	     if (row && row.hasAttribute('data-code')) {
	       selectProduct(row);
	     }
	   });
	 }

	 function selectProduct(row) {
	   try {
	     const code = row.getAttribute("data-code");
	     const name = row.getAttribute("data-name");
	     const spec = row.getAttribute("data-spec");
	     const unit = row.getAttribute("data-unit");

	     console.log("선택된 제품:", code, name, spec, unit); // 디버깅용

	     const productSearchInput = document.getElementById("productSearchInput");
	     const productNameInput = document.getElementById("productNameInput");
	     const specInput = document.getElementById("specInput");
	     const unitInput = document.getElementById("unitInput");

	     if (productSearchInput) productSearchInput.value = code || '';
	     if (productNameInput) productNameInput.value = name || '';
	     if (specInput) specInput.value = spec || '';
	     if (unitInput) unitInput.value = unit || '';

	     // bomDetail 업데이트 (안전하게)
	     if (typeof bomDetail !== 'undefined' && bomDetail) {
	       bomDetail.productCode = code || '';
	       bomDetail.productName = name || '';
	       bomDetail.spec = spec || '';
	       bomDetail.unit = unit || '';
	       console.log("업데이트된 bomDetail:", bomDetail); // 디버깅용
	     }

	     // 모달 닫기
	     try {
	       const modalElement = document.getElementById('productSearchModal');
	       const modalInstance = bootstrap.Modal.getInstance(modalElement);
	       if (modalInstance) {
	         modalInstance.hide();
	       } else {
	         const newModalInstance = new bootstrap.Modal(modalElement);
	         newModalInstance.hide();
	       }
	       
	       // 강제로 backdrop 제거 (안전장치)
	       setTimeout(() => {
	         const backdrop = document.querySelector('.modal-backdrop');
	         if (backdrop) {
	           backdrop.remove();
	         }
	         if (modalElement) {
	           modalElement.classList.remove('show');
	           modalElement.style.display = 'none';
	           modalElement.setAttribute('aria-hidden', 'true');
	           modalElement.removeAttribute('aria-modal');
	         }
	         document.body.classList.remove('modal-open');
	         document.body.style.overflow = '';
	         document.body.style.paddingRight = '';
	       }, 100);
	       
	     } catch (modalError) {
	       console.error("모달 닫기 오류:", modalError);
	       
	       // 강제 모달 닫기
	       const modalElement = document.getElementById('productSearchModal');
	       const backdrop = document.querySelector('.modal-backdrop');
	       
	       if (modalElement) {
	         modalElement.classList.remove('show');
	         modalElement.style.display = 'none';
	         modalElement.setAttribute('aria-hidden', 'true');
	         modalElement.removeAttribute('aria-modal');
	       }
	       
	       if (backdrop) {
	         backdrop.remove();
	       }
	       
	       document.body.classList.remove('modal-open');
	       document.body.style.overflow = '';
	       document.body.style.paddingRight = '';
	     }
	     
	   } catch (error) {
	     console.error("제품 선택 중 오류:", error);
	   }
	 }

	 // ============ 자재 검색 모달 ============
	 const materialModal = new bootstrap.Modal(document.getElementById('materialSearchModal'));

	 // 자재 검색 버튼 클릭 시 모달 열기
	 document.getElementById("materialSearchBtn").addEventListener("click", () => {
	   materialModal.show();
	   document.getElementById("materialKeyword").focus();
	   
	   // 모달이 열릴 때 기존 Thymeleaf 행들에 클릭 이벤트 연결
	   setTimeout(() => {
	     const rows = document.querySelectorAll('#modalMaterialResult tr[data-code]');
	     console.log("자재 검색 모달 - 찾은 행 개수:", rows.length); // 디버깅
	     
	     rows.forEach((row, index) => {
	       console.log(`행 ${index}:`, {
	         code: row.getAttribute('data-code'),
	         name: row.getAttribute('data-name'),
	         spec: row.getAttribute('data-spec'),
	         unit: row.getAttribute('data-unit'),
	         price: row.getAttribute('data-price')
	       }); // 디버깅
	       
	       row.style.cursor = 'pointer';
	       
	       // 기존 onclick 속성 제거하고 새로 이벤트 연결
	       row.removeAttribute('onclick');
	       row.addEventListener('click', function() {
	         console.log("자재 행 클릭됨:", this.getAttribute('data-code')); // 디버깅
	         selectMaterial(this);
	       });
	     });
	     
	     // 전체 테이블에도 이벤트 위임 추가
	     const modalResult = document.getElementById('modalMaterialResult');
	     if (modalResult) {
	       modalResult.addEventListener('click', function(e) {
	         const row = e.target.closest('tr[data-code]');
	         if (row) {
	           console.log("이벤트 위임으로 자재 선택:", row.getAttribute('data-code')); // 디버깅
	           selectMaterial(row);
	         }
	       });
	     }
	   }, 200); // 시간을 조금 더 늘림
	 });

	 // 자재 검색 - 기존 Thymeleaf 데이터 필터링만
	 document.getElementById("materialKeyword").addEventListener("keydown", function(e) {
	   if (e.key === "Enter") {
	     e.preventDefault();
	     const keyword = this.value.toLowerCase().trim();
	     const modalResult = document.getElementById("modalMaterialResult");
	     
	     if (!modalResult) return;
	     
	     // Thymeleaf로 렌더링된 행들을 필터링
	     const allRows = modalResult.querySelectorAll('tr[data-code]');
	     
	     allRows.forEach(row => {
	       const code = row.getAttribute('data-code').toLowerCase();
	       const name = row.getAttribute('data-name').toLowerCase();
	       
	       if (!keyword || code.includes(keyword) || name.includes(keyword)) {
	         row.style.display = '';  // 보이기
	       } else {
	         row.style.display = 'none';  // 숨기기
	       }
	     });
	   }
	 });

	 // 자재 선택 함수
	 function selectMaterial(row) {
	   console.log("selectMaterial 함수 호출됨!!!"); // 디버깅
	   console.log("전달받은 row:", row); // 디버깅
	   
	   try {
	     const code = row.getAttribute("data-code");
	     const name = row.getAttribute("data-name");
	     const spec = row.getAttribute("data-spec");
	     const unit = row.getAttribute("data-unit");
	     const price = row.getAttribute("data-price");

	     console.log("추출된 데이터:", { code, name, spec, unit, price }); // 디버깅

	     // 자재 추가 모달의 입력 필드들에 값 설정
	     const materialCodeInput = document.getElementById("materialCodeInput");
	     const materialNameInput = document.getElementById("materialNameInput");
	     const materialSpecInput = document.getElementById("materialSpecInput");
	     const materialUnitInput = document.getElementById("materialUnitInput");
	     const materialPriceInput = document.getElementById("materialPriceInput");

	     console.log("입력 필드들 확인:", {
	       materialCodeInput: !!materialCodeInput,
	       materialNameInput: !!materialNameInput,
	       materialSpecInput: !!materialSpecInput,
	       materialUnitInput: !!materialUnitInput,
	       materialPriceInput: !!materialPriceInput
	     }); // 디버깅

	     if (materialCodeInput) {
	       materialCodeInput.value = code || '';
	       console.log("자재코드 설정됨:", code);
	     } else {
	       console.error("materialCodeInput을 찾을 수 없습니다!");
	     }
	     
	     if (materialNameInput) {
	       materialNameInput.value = name || '';
	       console.log("자재명 설정됨:", name);
	     } else {
	       console.error("materialNameInput을 찾을 수 없습니다!");
	     }
	     
	     if (materialSpecInput) {
	       materialSpecInput.value = spec || '';
	       console.log("규격 설정됨:", spec);
	     } else {
	       console.error("materialSpecInput을 찾을 수 없습니다!");
	     }
	     
	     if (materialUnitInput) {
	       materialUnitInput.value = unit || '';
	       console.log("단위 설정됨:", unit);
	     } else {
	       console.error("materialUnitInput을 찾을 수 없습니다!");
	     }
	     
	     if (materialPriceInput) {
	       materialPriceInput.value = price || '';
	       console.log("단가 설정됨:", price);
	     } else {
	       console.error("materialPriceInput을 찾을 수 없습니다!");
	     }
		 
	     // 자재 모달 닫기
	     try {
	       const modalElement = document.getElementById('materialSearchModal');
	       const modalInstance = bootstrap.Modal.getInstance(modalElement);
	       if (modalInstance) {
	         modalInstance.hide();
	         console.log("모달 닫기 성공");
	       } else {
	         const newModalInstance = new bootstrap.Modal(modalElement);
	         newModalInstance.hide();
	         console.log("새 모달 인스턴스로 닫기 성공");
	       }
	       
	       // 강제로 backdrop 제거 (안전장치)
	       setTimeout(() => {
	         const backdrop = document.querySelector('.modal-backdrop');
	         if (backdrop) {
	           backdrop.remove();
	         }
	         if (modalElement) {
	           modalElement.classList.remove('show');
	           modalElement.style.display = 'none';
	           modalElement.setAttribute('aria-hidden', 'true');
	           modalElement.removeAttribute('aria-modal');
	         }
	         document.body.classList.remove('modal-open');
	         document.body.style.overflow = '';
	         document.body.style.paddingRight = '';
	       }, 100);
	       
	     } catch (modalError) {
	       console.error("자재 모달 닫기 오류:", modalError);
	       
	       // 강제 모달 닫기
	       const modalElement = document.getElementById('materialSearchModal');
	       const backdrop = document.querySelector('.modal-backdrop');
	       
	       if (modalElement) {
	         modalElement.classList.remove('show');
	         modalElement.style.display = 'none';
	         modalElement.setAttribute('aria-hidden', 'true');
	         modalElement.removeAttribute('aria-modal');
	       }
	       
	       if (backdrop) {
	         backdrop.remove();
	       }
	       
	       document.body.classList.remove('modal-open');
	       document.body.style.overflow = '';
	       document.body.style.paddingRight = '';
	     }
	     
	   } catch (error) {
	     console.error("자재 선택 중 오류:", error);
	   }
	 }

	 // ============ 자재 추가 모달 관련 ============
	 function openMaterialAddModal() {
	   const modal = new bootstrap.Modal(document.getElementById("materialAddModal"));
	   
	   // BOM 코드 자동 생성 (I00029v01부터 시작)
	   let bomCode;
	   
	   // 기존 자재가 있다면 마지막 번호에서 +1
	   if (bomDetail.materials.length > 0) {
	     // 마지막 자재의 BOM 코드에서 숫자 부분 추출
	     let lastBomCode = bomDetail.materials[bomDetail.materials.length - 1].bomCode;
	     let match = lastBomCode.match(/I(\d+)v01/);
	     
	     if (match) {
	       let nextNumber = parseInt(match[1]) + 1;
	       let paddedNumber = String(nextNumber).padStart(5, '0');
	       bomCode = `I${paddedNumber}v01`;
	     } else {
	       // 패턴이 맞지 않으면 기본값 사용 (I00029v01부터 시작)
	       bomCode = "I00029v01";
	     }
	   } else {
	     // 첫 번째 자재라면 I00029v01부터 시작
	     bomCode = "I00029v01";
	   }
	   
	   // 폼 초기화
	   document.getElementById("materialAddForm").reset();
	   
	   // 1. 자재 추가 모달의 BOM 코드 설정
	   document.getElementById("modalBomCode").value = bomCode;
	   
	   // 2. 상단 메인 BOM 코드도 함께 설정
	   document.getElementById("bomCodeInput").value = bomCode;
	   
	   modal.show();
	 }

	 // ============ 자재 추가/삭제 기능 ============
	 document.addEventListener("DOMContentLoaded", function() {
	   console.log("DOM 로드 완료"); // 디버깅용
	   
	   // 여러 가능한 선택자로 자재 추가 버튼 찾기
	   const addMaterialBtn = document.querySelector(".btn-add-material") || 
	                         document.getElementById("addMaterialBtn") ||
	                         document.getElementById("materialAddBtn") ||
	                         document.querySelector("[data-bs-target='#materialAddModal']") ||
	                         document.querySelector("button:contains('자재 추가')");
	   
	   console.log("자재 추가 버튼:", addMaterialBtn); // 디버깅용
	   
	   if (addMaterialBtn) {
	     console.log("자재 추가 버튼 이벤트 연결됨"); // 디버깅용
	     addMaterialBtn.addEventListener("click", function(e) {
	       e.preventDefault(); // 기본 동작 방지
	       console.log("자재 추가 버튼 클릭됨"); // 디버깅용
	       try {
	         openMaterialAddModal();
	       } catch (error) {
	         console.error("자재 추가 모달 열기 오류:", error);
	       }
	     });
	   } else {
	     console.error("자재 추가 버튼을 찾을 수 없습니다. 다음 선택자들을 확인하세요:");
	     console.error("- .btn-add-material");
	     console.error("- #addMaterialBtn"); 
	     console.error("- #materialAddBtn");
	     console.error("- [data-bs-target='#materialAddModal']");
	   }
	   
	   // 모달 내 "추가" 버튼 클릭 이벤트 - 여러 선택자로 찾기
	   const modalAddBtn = document.getElementById("materialAddBtn") ||
	                      document.getElementById("addMaterialBtn") ||
	                      document.querySelector("#materialAddModal .btn-primary") ||
	                      document.querySelector("#materialAddModal button[type='button']:not([data-bs-dismiss])");
	   
	   console.log("모달 내 추가 버튼:", modalAddBtn); // 디버깅용
	   
	   if (modalAddBtn) {
	     console.log("모달 내 추가 버튼 이벤트 연결됨"); // 디버깅용
	     modalAddBtn.addEventListener("click", function(e) {
	       e.preventDefault();
	       console.log("모달 내 추가 버튼 클릭됨"); // 디버깅용
	       try {
	         const form = document.getElementById("materialAddForm");
	         if (!form) {
	           console.error("materialAddForm을 찾을 수 없습니다.");
	           return;
	         }
	         
	         // 폼 데이터 읽기
	         const bomCodeValue = form.elements["bomCode"] ? form.elements["bomCode"].value.trim() : '';
	         const materialCodeValue = form.elements["materialCode"] ? form.elements["materialCode"].value.trim() : '';
	         const materialNameValue = form.elements["materialName"] ? form.elements["materialName"].value.trim() : '';
	         const specValue = form.elements["spec"] ? form.elements["spec"].value.trim() : '';
	         const unitValue = document.getElementById("materialUnitInput") ? document.getElementById("materialUnitInput").value.trim() : '';
	         const priceValue = form.elements["price"] ? form.elements["price"].value : '';
	         const qtyValue = document.getElementById("materialQtyInput") ? document.getElementById("materialQtyInput").value : '';
	         
	         console.log("폼 데이터:", {
	           bomCode: bomCodeValue,
	           materialCode: materialCodeValue,
	           materialName: materialNameValue,
	           spec: specValue,
	           unit: unitValue,
	           price: priceValue,
	           qty: qtyValue
	         }); // 디버깅용
	         
	         // 유효성 검사 (필수 필드만 체크)
	         if (!materialCodeValue || !materialNameValue) {
	           alert("자재코드와 자재명은 필수입니다.");
	           return;
	         }
	         
	         // bomDetail.materials 배열에 새 자재 추가
	         const newMaterial = {
	           bomCode: bomCodeValue,
	           materialCode: materialCodeValue,
	           materialName: materialNameValue,
	           spec: specValue,
	           j_unitName: unitValue,  // unit → j_unitName으로 변경
	           price: Number(priceValue) || 0,
	           quantity: Number(qtyValue) || 1
	         };
	         
	         console.log("새 자재:", newMaterial); // 디버깅용
	         
	         // bomDetail이 정의되어 있는지 확인
	         if (typeof bomDetail !== 'undefined' && bomDetail && bomDetail.materials) {
	           bomDetail.materials.push(newMaterial);
	           console.log("bomDetail.materials:", bomDetail.materials); // 디버깅용
	           
	           // 테이블 다시 렌더링
	           renderMaterialTable();
	         } else {
	           console.error("bomDetail이 정의되지 않았습니다.");
	         }
	         
	         // 폼 초기화 및 모달 닫기
	         form.reset();
	         const modalEl = document.getElementById("materialAddModal");
	         if (modalEl && bootstrap.Modal.getInstance(modalEl)) {
	           bootstrap.Modal.getInstance(modalEl).hide();
	         }
	         
	         console.log("자재가 추가되었습니다:", newMaterial);
	       } catch (error) {
	         console.error("자재 추가 중 오류:", error);
	       }
	     });
	   } else {
	     console.error("모달 내 추가 버튼을 찾을 수 없습니다.");
	   }

	   // 자재 삭제 버튼
	   const deleteBtn = document.querySelector(".btn-delete-material");
	   if (deleteBtn) {
	     deleteBtn.addEventListener("click", function() {
	       try {
	         const checked = Array.from(
	           document.querySelectorAll('#materialTable tbody input.rowCheckbox1[type="checkbox"]:checked')
	         );
	         
	         if (checked.length === 0) {
	           alert("삭제할 자재를 선택하세요.");
	           return;
	         }
	         
	         if (!confirm("정말 삭제하시겠습니까?")) return;
	         
	         const idxs = checked
	           .map((cb) => Number(cb.dataset.idx))
	           .filter(idx => !isNaN(idx))
	           .sort((a, b) => b - a);
	           
	         if (typeof bomDetail !== 'undefined' && bomDetail && bomDetail.materials) {
	           for (let idx of idxs) {
	             if (idx >= 0 && idx < bomDetail.materials.length) {
	               bomDetail.materials.splice(idx, 1);
	             }
	           }
	           
	           renderMaterialTable();
	         }
	       } catch (error) {
	         console.error("자재 삭제 중 오류:", error);
	       }
	     });
	   }
	 });

	 // ============ 하단 버튼 이벤트 ============
	 document.getElementById("btnList").addEventListener("click", function () {
	   if (confirm("목록 페이지로 이동하시겠습니까?")) {
	     location.href="/bom/bom_list";
	   }
	 });

	 // BOM 등록 폼 제출
	 document.getElementById("bomForm").addEventListener("submit", function (e) {
	   e.preventDefault();
	   
	   const bomCode = document.getElementById("bomCodeInput").value.trim();
	   const productCode = productSearchInput.value.trim();
	   
	   if (!bomCode || !productCode) {
	     alert("BOM 코드와 완제품을 선택하세요.");
	     return;
	   }
	   
	   if (bomDetail.materials.length === 0) {
	     alert("자재를 1개 이상 추가하세요.");
	     return;
	   }
	   
	   console.log("저장할 BOM 데이터:", bomDetail);
	   
	   // 서버로 전송할 데이터 구성 (hidden 필드들도 업데이트)
	  
	   const bomData = {
	     // PK는 selectKey가 채워주므로 안 넘겨도 됩니다
	     itemId:    bomDetail.productId,              // productId 가 아이디라면
	     version:   1,                                 // 헤더 버전, 기본 1로 고정
	     description: document.querySelector("[name=description]").value,
	     materials: bomDetail.materials
	   };
	   /* const bomData = {
	    //bomCode: bomCode,
	     productCode: bomDetail.productCode,
	     productName: bomDetail.productName,
	     spec: bomDetail.spec,
	     unit: bomDetail.unit,
	     note: document.getElementById("noteInput") ? document.getElementById("noteInput").value : "",
	     materials: bomDetail.materials
	   };
	   */
	   // hidden 필드들 업데이트
	   document.getElementById("hiddenProductCode").value = bomDetail.productCode || '';
	   document.getElementById("hiddenProductName").value = bomDetail.productName || '';
	   document.getElementById("hiddenSpec").value = bomDetail.spec || '';
	   document.getElementById("hiddenUnit").value = bomDetail.unit || '';
	   document.getElementById("hiddenNote").value = bomData.note;
	   console.log("전송할 데이터:", bomData); // 디버깅용
	   
	   // 서버로 데이터 전송
	   fetch('/bom/bom_writeok', {
	     method: 'POST',
	     headers: {
	       'Content-Type': 'application/json',
	     },
	     body: JSON.stringify(bomData)
	   })
	   .then(response => {
	     console.log("서버 응답 상태:", response.status); // 디버깅용
	     if (response.ok) {
	       return response.json();
	     }
	     throw new Error(`서버 응답 오류: ${response.status} ${response.statusText}`);
	   })
	   .then(data => {
	     console.log("서버 응답 데이터:", data); // 디버깅용
	     alert("BOM이 성공적으로 등록되었습니다.");
	     // 성공 시 목록 페이지로 이동
	     location.href = "/bom/bom_list";
	   })
	   .catch(error => {
	     console.error('BOM 등록 오류:', error);
	     alert("BOM 등록 중 오류가 발생했습니다: " + error.message);
	   });
	 });

	 // ============ 초기화 ============
	 document.addEventListener("DOMContentLoaded", () => {
	   renderMaterialTable();
	 });			        	         