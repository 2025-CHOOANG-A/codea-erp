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

     // 샘플 BOM 데이터 (제품별로 여러 자재)
	 /*
     const bomData = [
		
       {
         id: 1,
         bomCode: "BOM001",
         productCode: "P001",
         productName: "제품A",
         materials: [
           {
             materialCode: "M001",
             materialName: "자재X",
             spec: "100x200",
             unit: "EA",
             price: 1000,
             qty: 10,
             note: "",
           },
           {
             materialCode: "M002",
             materialName: "자재Y",
             spec: "50x100",
             unit: "EA",
             price: 500,
             qty: 5,
             note: "",
           },
         ],
       },
       {
         id: 2,
         bomCode: "BOM002",
         productCode: "P002",
         productName: "제품B",
         materials: [
           {
             materialCode: "M003",
             materialName: "자재Z",
             spec: "200x300",
             unit: "EA",
             price: 1200,
             qty: 7,
             note: "",
           },
         ],
       },
       {
         id: 3,
         bomCode: "BOM003",
         productCode: "P003",
         productName: "제품C",
         materials: [
           {
             materialCode: "M004",
             materialName: "자재W",
             spec: "80x120",
             unit: "EA",
             price: 800,
             qty: 8,
             note: "",
           },
           {
             materialCode: "M005",
             materialName: "자재V",
             spec: "60x90",
             unit: "EA",
             price: 600,
             qty: 12,
             note: "",
           },
         ],
       },
       {
         id: 4,
         bomCode: "BOM004",
         productCode: "P004",
         productName: "제품D",
         materials: [
           {
             materialCode: "M006",
             materialName: "자재U",
             spec: "150x250",
             unit: "EA",
             price: 1500,
             qty: 20,
             note: "",
           },
         ],
       },	   
     ];
*/
     let currentPage = 1;
     const rowsPerPage = 10;
     let filteredData = bomData.slice();
     let sortAsc = true;

     function applyFilter() {
       const code = document.getElementById("bomCodeInput").value.trim();
       filteredData = bomData.filter((item) => {
         const matchCode = !code || item.bomCode.includes(code);
         return matchCode;
       });
       sortData();
       currentPage = 1;
       renderTable();
       renderPagination();
     }

     function sortData() {
       filteredData.sort((a, b) => {
         if (sortAsc) return a.bomCode.localeCompare(b.bomCode, "ko");
         else return b.bomCode.localeCompare(a.bomCode, "ko") * -1;
       });
     }

     function renderTable() {
       const tbody = document.querySelector("#bomTable tbody");
       tbody.innerHTML = "";
       const start = (currentPage - 1) * rowsPerPage;
       const end = start + rowsPerPage;
       const pageData = filteredData.slice(start, end);

       pageData.forEach((item) => {
         item.materials.forEach((mat, idx) => {
           const tr = document.createElement("tr");
           tr.innerHTML = `
                           ${
                             idx === 0
                               ? `<td rowspan="${item.materials.length}"><input type="checkbox" data-id="${item.id}"></td>`
                               : ""
                           }
                           ${
                             idx === 0
                               ? `<td rowspan="${item.materials.length}">${item.bomCode}</td>`
                               : ""
                           }
                           ${
                             idx === 0
                               ? `<td rowspan="${item.materials.length}"><a href="#" class="name-link" data-id="${item.id}" data-type="product">${item.productCode}</a></td>`
                               : ""
                           }
                           ${
                             idx === 0
                               ? `<td rowspan="${item.materials.length}"><a href="#" class="name-link" data-id="${item.id}" data-type="productName">${item.productName}</a></td>`
                               : ""
                           }
                           <td><a href="#" class="name-link" data-id="${
                             item.id
                           }" data-type="material">${mat.materialCode}</a></td>
                           <td><a href="#" class="name-link" data-id="${
                             item.id
                           }" data-type="materialName">${
             mat.materialName
           }</a></td>
                           <td>${mat.spec}</td>
                           <td>${mat.unit}</td>
                           <td>${mat.price.toLocaleString()}</td>
                           <td>${mat.qty}</td>
                           <td>${mat.note || "-"}</td>
                           ${
                             idx === 0
                               ? `<td rowspan="${item.materials.length}"><button class="btn btn-outline-secondary btn-sm detail-btn" data-id="${item.id}">확인</button></td>`
                               : ""
                           }
                       `;
           tbody.appendChild(tr);
         });
       });
       document.getElementById("selectAll").checked = false;
     }

     function renderPagination() {
       const total = filteredData.length;
       const totalPages = Math.ceil(total / rowsPerPage);
       const pagination = document.getElementById("pagination");
       pagination.innerHTML = "";

       if (totalPages <= 1) return;

       if (currentPage > 3) {
         pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="1">&laquo;</a></li>`;
       }
       if (currentPage > 1) {
         pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${
           currentPage - 1
         }">&lt;</a></li>`;
       }
       let start = Math.max(1, currentPage - 2);
       let end = Math.min(totalPages, currentPage + 2);
       if (currentPage <= 3) end = Math.min(5, totalPages);
       if (currentPage >= totalPages - 2) start = Math.max(1, totalPages - 4);
       for (let i = start; i <= end; i++) {
         pagination.innerHTML += `<li class="page-item${
           i === currentPage ? " active" : ""
         }"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
       }
       if (currentPage < totalPages) {
         pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${
           currentPage + 1
         }">&gt;</a></li>`;
       }
       if (currentPage < totalPages - 2) {
         pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${totalPages}">&raquo;</a></li>`;
       }
     }

     function showDetail(id) {
       const item = bomData.find((w) => w.id == id);
       if (!item) return;
       const modalBody = document.getElementById("modalDetailBody");
       let html = `
                   <tr><th>BOM 코드</th><td>${item.bomCode}</td></tr>
                   <tr><th>제품 코드</th><td>${item.productCode}</td></tr>
                   <tr><th>제품 명</th><td>${item.productName}</td></tr>
                   <tr>
                       <th>자재 목록</th>
                       <td>
                           <table class="table table-bordered mb-0">
                               <thead>
                                   <tr>
                                       <th>자재 코드</th>
                                       <th>자재 명</th>
                                       <th>규격</th>
                                       <th>단위(EA)</th>
                                       <th>단가</th>
                                       <th>자재소요량</th>
                                       <th>비고</th>
                                   </tr>
                               </thead>
                               <tbody>
                                   ${item.materials
                                     .map(
                                       (mat) => `
                                       <tr>
                                           <td>${mat.materialCode}</td>
                                           <td>${mat.materialName}</td>
                                           <td>${mat.spec}</td>
                                           <td>${mat.unit}</td>
                                           <td>${mat.price.toLocaleString()}</td>
                                           <td>${mat.qty}</td>
                                           <td>${mat.note || "-"}</td>
                                       </tr>
                                   `
                                     )
                                     .join("")}
                               </tbody>
                           </table>
                       </td>
                   </tr>
               `;
       modalBody.innerHTML = html;
       const modal = new bootstrap.Modal(
         document.getElementById("detailModal")
       );
       modal.show();
     }

     function handleNameClick(id, type) {
       let msg = "";
       switch (type) {
         case "product":
           msg = "제품 코드 페이지로 이동: ";
           break;
         case "productName":
           msg = "제품명 페이지로 이동: ";
           break;
         case "material":
           msg = "자재 코드 페이지로 이동: ";
           break;
         case "materialName":
           msg = "자재명 페이지로 이동: ";
           break;
         default:
           msg = "페이지로 이동: ";
       }
       alert(msg + id);
     }

     document.addEventListener("DOMContentLoaded", () => {
       renderTable();
       renderPagination();

       document
         .getElementById("filterForm")
         .addEventListener("submit", function (e) {
           e.preventDefault();
           applyFilter();
         });

       document
         .getElementById("sortBtn")
         .addEventListener("click", function () {
           sortAsc = !sortAsc;
           this.textContent = sortAsc
             ? "BOM코드 오름차순"
             : "BOM코드 내림차순";
           sortData();
           renderTable();
         });

       document
         .getElementById("pagination")
         .addEventListener("click", function (e) {
           if (e.target.tagName === "A") {
             e.preventDefault();
             const page = parseInt(e.target.dataset.page, 10);
             if (!isNaN(page)) {
               currentPage = page;
               renderTable();
               renderPagination();
             }
           }
         });

       document
         .querySelector("#bomTable tbody")
         .addEventListener("click", function (e) {
           if (e.target.classList.contains("detail-btn")) {
             const id = e.target.dataset.id;
             showDetail(id);
           }
           if (e.target.classList.contains("name-link")) {
             const id = e.target.dataset.id;
             const type = e.target.dataset.type;
             handleNameClick(id, type);
           }
         });

       document
         .getElementById("selectAll")
         .addEventListener("change", function (e) {
           const checkboxes = document.querySelectorAll(
             '#bomTable tbody input[type="checkbox"]'
           );
           checkboxes.forEach((cb) => (cb.checked = e.target.checked));
         });

       document
         .querySelector(".add-btn")
         .addEventListener("click", function (e) {
           e.preventDefault();
           alert("BOM 등록페이지로 이동합니다.");
           location.href="./bom_write"
         });

       document
         .querySelector(".delete-btn")
         .addEventListener("click", function () {
           const checked = Array.from(
             document.querySelectorAll(
               '#bomTable tbody input[type="checkbox"]:checked'
             )
           );
           if (checked.length === 0) {
             alert("삭제할 항목을 선택하세요.");
             return;
           }
           if (confirm("정말 삭제하시겠습니까?")) {
             const ids = checked.map((cb) => parseInt(cb.dataset.id, 10));
             for (let id of ids) {
               const idx = bomData.findIndex((w) => w.id === id);
               if (idx > -1) bomData.splice(idx, 1);
             }
             applyFilter();
           }
         });
     });
	 
	 
	 /*
	 document.addEventListener("DOMContentLoaded", function () {
	    const sortBtn = document.getElementById("sortBtn");
	    const sortOrderInput = document.getElementById("sortOrderInput");

	    if (sortBtn && sortOrderInput) {
	      sortBtn.addEventListener("click", function () {
	        // 1) 현재 sortOrder 값을 읽어온다 (ASC 아니면 DESC로 간주)
	        let current = sortOrderInput.value === "ASC" ? "ASC" : "DESC";

	        // 2) 토글: ASC → DESC, DESC → ASC
	        let next = current === "ASC" ? "DESC" : "ASC";
	        sortOrderInput.value = next;

	        // 3) 버튼 레이블도 토글 후 바꿔준다
	        //    (현재 설정된 next 값이 'ASC'라면, 버튼에는 'BOM코드 내림차순'을 표시
	        //     next 값이 'DESC'라면, 버튼에는 'BOM코드 오름차순'을 표시)
	        if (next === "ASC") {
	          sortBtn.innerText = "BOM코드 내림차순";
	        } else {
	          sortBtn.innerText = "BOM코드 오름차순";
	        }

	        // 4) (선택) 정렬 기준이 바뀌었으니 항상 1페이지로 돌아가고 싶다면:
	        // document.querySelector("input[name='page']").value = 1;

	        // 5) 폼을 submit() 해서 서버에 GET 요청 보내기
	        document.getElementById("filterForm").submit();
	      });
	    }
	  });
	  */