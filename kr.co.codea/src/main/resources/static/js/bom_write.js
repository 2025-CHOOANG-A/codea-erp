
     // Sidebar toggle
     const sidebar = document.getElementById("sidebar");
     const sidebarToggle = document.getElementById("sidebarToggle");
     sidebarToggle.addEventListener("click", () => {
       sidebar.classList.toggle("collapsed");
     });
     function handleResize() {
       if (window.innerWidth < 992) {
         sidebar.classList.add("collapsed");
       } else {
         sidebar.classList.remove("collapsed");
       }
     }
     window.addEventListener("resize", handleResize);
     handleResize();

     // 샘플 완제품 데이터 (검색용)
     const products = [
       { code: "P001", name: "제품A", spec: "100x200", unit: "EA" },
       { code: "P002", name: "제품B", spec: "50x100", unit: "SET" },
       { code: "P003", name: "제품C", spec: "200x300", unit: "BOX" },
       { code: "P004", name: "제품D", spec: "150x150", unit: "EA" },
     ];

     // BOM 등록 데이터
     let bomDetail = {
       bomCode: "BOM2024-01",
       productCode: "P001",
       productName: "제품A",
       spec: "100x200",
       unit: "EA",
       note: "",
       materials: [
         {
           bomCode: "BOM2024-01-01",
           materialCode: "M001",
           materialName: "철판",
           spec: "1T",
           unit: "EA",
           price: 1000,
           qty: 2,
         },
         {
           bomCode: "BOM2024-01-02",
           materialCode: "M002",
           materialName: "볼트",
           spec: "M8",
           unit: "EA",
           price: 100,
           qty: 8,
         },
       ],
     };

     // 완제품 검색
     const productSearchInput = document.getElementById("productSearchInput");
     const productSearchResult = document.getElementById(
       "productSearchResult"
     );
     const productNameInput = document.getElementById("productNameInput");
     const specInput = document.getElementById("specInput");
     const unitInput = document.getElementById("unitInput");

     function showProductSearchResult(list) {
       if (!list.length) {
         productSearchResult.classList.add("d-none");
         productSearchResult.innerHTML = "";
         return;
       }
       productSearchResult.innerHTML = list
         .map(
           (p) =>
             `<div class="search-result-item" data-code="${p.code}" data-name="${p.name}" data-spec="${p.spec}" data-unit="${p.unit}">
                                                               <span class="text-primary">${p.code}</span> - ${p.name} <span class="text-muted small">[${p.spec}, ${p.unit}]</span>
                                                       </div>`
         )
         .join("");
       productSearchResult.classList.remove("d-none");
     }

     function handleProductSearch() {
       const q = productSearchInput.value.trim().toLowerCase();
       if (!q) {
         showProductSearchResult([]);
         return;
       }
       const filtered = products.filter(
         (p) =>
           p.code.toLowerCase().includes(q) || p.name.toLowerCase().includes(q)
       );
       showProductSearchResult(filtered);
     }

     productSearchInput.addEventListener("input", handleProductSearch);
     document
       .getElementById("productSearchBtn")
       .addEventListener("click", handleProductSearch);

     productSearchResult.addEventListener("mousedown", function (e) {
       if (e.target.closest(".search-result-item")) {
         const item = e.target.closest(".search-result-item");
         productSearchInput.value = item.dataset.code;
         productNameInput.value = item.dataset.name;
         specInput.value = item.dataset.spec;
         unitInput.value = item.dataset.unit;
         bomDetail.productCode = item.dataset.code;
         bomDetail.productName = item.dataset.name;
         bomDetail.spec = item.dataset.spec;
         bomDetail.unit = item.dataset.unit;
         productSearchResult.classList.add("d-none");
       }
     });

     document.addEventListener("click", function (e) {
       if (
         !productSearchInput.contains(e.target) &&
         !productSearchResult.contains(e.target)
       ) {
         productSearchResult.classList.add("d-none");
       }
     });

     // 자재 목록 렌더링
     function renderMaterialTable() {
       const tbody = document.querySelector("#materialTable tbody");
       tbody.innerHTML = "";
       bomDetail.materials.forEach((mat, idx) => {
         const tr = document.createElement("tr");
         tr.innerHTML = `
                                                                                               <td><input type="checkbox" data-idx="${idx}" /></td>
                                                                                               <td>${
                                                                                                 mat.bomCode
                                                                                               }</td>
                                                                                               <td>${
                                                                                                 mat.materialCode
                                                                                               }</td>
                                                                                               <td>${
                                                                                                 mat.materialName
                                                                                               }</td>
                                                                                               <td>${
                                                                                                 mat.spec
                                                                                               }</td>
                                                                                               <td>${
                                                                                                 mat.unit
                                                                                               }</td>
                                                                                               <td>${mat.price.toLocaleString()}</td>
                                                                                               <td>${
                                                                                                 mat.qty
                                                                                               }</td>
                                                                               `;
         tbody.appendChild(tr);
       });
       document.getElementById("selectAllMaterial").checked = false;
     }

     // 자재 추가 모달 열기
     function openMaterialAddModal() {
       const modal = new bootstrap.Modal(
         document.getElementById("materialAddModal")
       );
       // BOM 코드 자동 생성 (입력값 + -0X)
       let baseBomCode =
         document.getElementById("bomCodeInput").value.trim() || "BOM";
       let nextIdx = bomDetail.materials.length + 1;
       let bomCode = baseBomCode + "-" + String(nextIdx).padStart(2, "0");
       document.getElementById("modalBomCode").value = bomCode;
       document.getElementById("materialAddForm").reset();
       modal.show();
     }

     // 자재 추가 폼 제출
     document
       .getElementById("materialAddForm")
       .addEventListener("submit", function (e) {
         e.preventDefault();
         const form = e.target;
         const newMat = {
           bomCode: form.bomCode.value,
           materialCode: form.materialCode.value.trim(),
           materialName: form.materialName.value.trim(),
           spec: form.spec.value.trim(),
           unit: form.unit.value,
           price: Number(form.price.value),
           qty: Number(form.qty.value),
         };
         if (!newMat.materialCode || !newMat.materialName || !newMat.spec) {
           alert("필수 항목을 입력하세요.");
           return;
         }
         bomDetail.materials.push(newMat);
         renderMaterialTable();
         bootstrap.Modal.getInstance(
           document.getElementById("materialAddModal")
         ).hide();
       });

     // 자재 선택 삭제
     document
       .querySelector(".btn-delete-material")
       .addEventListener("click", function () {
         const checked = Array.from(
           document.querySelectorAll(
             '#materialTable tbody input[type="checkbox"]:checked'
           )
         );
         if (checked.length === 0) {
           alert("삭제할 자재를 선택하세요.");
           return;
         }
         if (!confirm("정말 삭제하시겠습니까?")) return;
         // 인덱스 내림차순으로 삭제
         const idxs = checked
           .map((cb) => Number(cb.dataset.idx))
           .sort((a, b) => b - a);
         for (let idx of idxs) {
           bomDetail.materials.splice(idx, 1);
         }
         renderMaterialTable();
       });

     // 자재 전체 선택
     document
       .getElementById("selectAllMaterial")
       .addEventListener("change", function (e) {
         const checkboxes = document.querySelectorAll(
           '#materialTable tbody input[type="checkbox"]'
         );
         checkboxes.forEach((cb) => (cb.checked = e.target.checked));
       });

     // 자재 추가 버튼
     document
       .querySelector(".btn-add-material")
       .addEventListener("click", openMaterialAddModal);

     // 하단 버튼 이벤트
     document.getElementById("btnList").addEventListener("click", function () {
       alert("목록 페이지로 이동");
     });

     // BOM 등록 폼 제출
     document
       .getElementById("bomForm")
       .addEventListener("submit", function (e) {
         e.preventDefault();
         // 필수값 체크
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
         // 저장 처리 (여기서는 alert)
         alert("BOM이 등록되었습니다.");
       });

     // 초기 렌더링
     document.addEventListener("DOMContentLoaded", () => {
       renderMaterialTable();
     });

	 
	 const productModal = new bootstrap.Modal(document.getElementById('productSearchModal'));

	 document.getElementById("productSearchBtn").onclick = () => {
	   productModal.show();
	   document.getElementById("productKeyword").focus();
	 };

	 document.getElementById("productKeyword").addEventListener("keydown", function(e) {
	   if (e.key === "Enter") {
	     e.preventDefault();
	     const keyword = this.value;
	     fetch(`/item/search?keyword=${keyword}`)
	       .then(res => res.text())
	       .then(html => {
	         document.getElementById("modalProductResult").innerHTML = html;
	       });
	   }
	 });

	 function selectProduct(row) {
	   const code = row.getAttribute("data-code");
	   const name = row.getAttribute("data-name");
	   const spec = row.getAttribute("data-spec");
	   const unit = row.getAttribute("data-unit");

	   document.getElementById("productSearchInput").value = code;
	   document.getElementById("productNameInput").value = name;
	   document.getElementById("specInput").value = spec;
	   document.getElementById("unitInput").value = unit;

	   productModal.hide();
	 }