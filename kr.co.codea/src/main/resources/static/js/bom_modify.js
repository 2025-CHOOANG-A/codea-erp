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

   // BOM 데이터 (수정용)
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

   // 자재 목록 렌더링
   function renderMaterialTable() {
     const tbody = document.querySelector("#materialTable tbody");
     tbody.innerHTML = "";
     bomDetail.materials.forEach((mat, idx) => {
       const tr = document.createElement("tr");
       tr.innerHTML = `
                     <td><input type="checkbox" data-idx="${idx}" /></td>
                     <td>${mat.bomCode}</td>
                     <td>${mat.materialCode}</td>
                     <td>${mat.materialName}</td>
                     <td>${mat.spec}</td>
                     <td>${mat.unit}</td>
                     <td>${mat.price.toLocaleString()}</td>
                     <td>${mat.qty}</td>
                     <td>
                         <button type="button" class="btn btn-outline-secondary btn-sm btn-edit-material" data-idx="${idx}">수정</button>
                     </td>
                 `;
       tbody.appendChild(tr);
     });
     document.getElementById("selectAllMaterial").checked = false;
   }

   // 자재 추가/수정 모달 열기
   function openMaterialAddModal(editIdx = null) {
     const modal = new bootstrap.Modal(
       document.getElementById("materialAddModal")
     );
     const form = document.getElementById("materialAddForm");
     form.reset();
     document.getElementById("editIdx").value = "";
     let baseBomCode =
       document.getElementById("bomCodeInput").value.trim() || "BOM";
     if (editIdx !== null) {
       // 수정
       document.getElementById("materialAddModalLabel").textContent =
         "자재 수정";
       const mat = bomDetail.materials[editIdx];
       form.bomCode.value = mat.bomCode;
       form.materialCode.value = mat.materialCode;
       form.materialName.value = mat.materialName;
       form.spec.value = mat.spec;
       form.unit.value = mat.unit;
       form.price.value = mat.price;
       form.qty.value = mat.qty;
       document.getElementById("editIdx").value = editIdx;
     } else {
       // 추가
       document.getElementById("materialAddModalLabel").textContent =
         "자재 추가";
       let nextIdx = bomDetail.materials.length + 1;
       let bomCode = baseBomCode + "-" + String(nextIdx).padStart(2, "0");
       form.bomCode.value = bomCode;
     }
     modal.show();
   }

   // 자재 추가/수정 폼 제출
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
       const editIdx = form.editIdx.value;
       if (editIdx !== "") {
         // 수정
         bomDetail.materials[editIdx] = newMat;
       } else {
         // 추가
         bomDetail.materials.push(newMat);
       }
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
     .addEventListener("click", () => openMaterialAddModal());

   // 자재 수정 버튼
   document
     .querySelector("#materialTable tbody")
     .addEventListener("click", function (e) {
       if (e.target.classList.contains("btn-edit-material")) {
         const idx = Number(e.target.dataset.idx);
         openMaterialAddModal(idx);
       }
     });

   // 하단 버튼 이벤트
   document.getElementById("btnList").addEventListener("click", function () {
     alert("목록 페이지로 이동");
   });

   // BOM 수정 폼 제출
   document
     .getElementById("bomForm")
     .addEventListener("submit", function (e) {
       e.preventDefault();
       if (bomDetail.materials.length === 0) {
         alert("자재를 1개 이상 추가하세요.");
         return;
       }
       alert("BOM이 수정되었습니다.");
     });

   // 초기 렌더링
   document.addEventListener("DOMContentLoaded", () => {
     renderMaterialTable();
   });