      // Sidebar toggle
      const sidebar = document.getElementById("sidebar");
      const sidebarOpenBtn = document.getElementById("sidebarOpenBtn");
      const sidebarCloseBtn = document.getElementById("sidebarCloseBtn");

      function openSidebar() {
        sidebar.classList.remove("closed");
        document.body.style.overflowY = "hidden";
      }
      
      function closeSidebar() {
        sidebar.classList.add("closed");
        document.body.style.overflowY = "";
      }
      sidebarOpenBtn.addEventListener("click", openSidebar);
      sidebarCloseBtn.addEventListener("click", closeSidebar);

      // 사이드바 외부 클릭 시 닫기 (모바일에서만)
      document.addEventListener("mousedown", function (e) {
        if (
          window.innerWidth < 992 &&
          !sidebar.classList.contains("closed") &&
          !sidebar.contains(e.target) &&
          !sidebarOpenBtn.contains(e.target)
        ) {
          closeSidebar();
        }
      });

      // Always show sidebar toggle in header
      window.addEventListener("resize", () => {
        if (window.innerWidth < 992) closeSidebar();
        else openSidebar();
      });
      if (window.innerWidth < 992) closeSidebar();

  
      // 페이징/필터 변수
      let currentPage = 1;
      const rowsPerPage = 10;
      //let filteredData = sampleData.slice();

      // 필터/검색
/*
      function filterAndRender() {
  const category = document.getElementById("categoryFilter").value;
  const keyword = document
    .getElementById("search_input") // ← 여기도 수정
    .value.trim()
    .toLowerCase();

  filteredData = sampleData.filter((item) => {
    const matchCategory =
      category === "전체" || item.category.toLowerCase() === category.toLowerCase();
    const matchKeyword =
      !keyword ||
      item.stockNo.toLowerCase().includes(keyword) ||
      item.code.toLowerCase().includes(keyword) ||
      item.name.toLowerCase().includes(keyword) ||
      item.subCategory.toLowerCase().includes(keyword) ||
      item.warehouse.toLowerCase().includes(keyword) ||
      item.warehouseCode.toLowerCase().includes(keyword);
    return matchCategory && matchKeyword;
  });

  currentPage = 1;
  renderTable();
  renderPagination();
}

// ✅ ID 정확하게 수정
document
  .getElementById("categoryFilter")
  .addEventListener("change", filterAndRender);

document
  .getElementById("searchBtn")
  .addEventListener("click", filterAndRender);

document
  .getElementById("search_input") // ← 여기 수정
  .addEventListener("keydown", function (e) {
    if (e.key === "Enter") filterAndRender();
  });
    */  
      
      
     /* 
      function filterAndRender() {
        //const category = document.getElementById("categoryFilter").value;
        const keyword = document
          .getElementById("search_input")
          .value.trim()
          .toLowerCase();
        filteredData = sampleData.filter((item) => {
          const matchCategory =
            category === "전체" || item.category === category;
          const matchKeyword =
            !keyword ||
            item.stockNo.toLowerCase().includes(keyword) ||
            item.code.toLowerCase().includes(keyword) ||
            item.name.toLowerCase().includes(keyword) ||
            item.subCategory.toLowerCase().includes(keyword) ||
            item.warehouse.toLowerCase().includes(keyword) ||
            item.warehouseCode.toLowerCase().includes(keyword);
          return matchCategory && matchKeyword;
        });
        currentPage = 1;
        renderTable();
        renderPagination();
      }

      document
        .getElementById("categoryFilter")
        .addEventListener("change", filterAndRender);
      document
        .getElementById("searchBtn")
        .addEventListener("click", filterAndRender);
      document
        .getElementById("searchInput")
        .addEventListener("keydown", function (e) {
          if (e.key === "Enter") filterAndRender();
        });
      */
      // 테이블 렌더링
      function renderTable() {
        const tbody = document.getElementById("productTableBody");
        tbody.innerHTML = "";
        const start = (currentPage - 1) * rowsPerPage;
        const end = start + rowsPerPage;
        const pageData = filteredData.slice(start, end);

        if (pageData.length === 0) {
          tbody.innerHTML = `<tr><td colspan="11" class="text-center text-secondary">데이터가 없습니다.</td></tr>`;
          return;
        }

        for (const item of pageData) {
          const remarkCell = item.remark
            ? `<button type="button" class="btn btn-outline-secondary btn-sm btn-remark" data-remark="${item.remark}">내용 확인</button>`
            : `<span>-</span>`;
          tbody.innerHTML += `
          <tr>
            <td>${item.stockNo}</td>
            <td>${item.category}</td>
            <td>${item.subCategory}</td>
            <td>${item.code}</td>
            <td><a href="#" class="name-link" data-name="${item.name}">${
            item.name
          }</a></td>
            <td>${item.spec}</td>
            <td>${item.price.toLocaleString()}</td>
            <td>${item.qty}</td>
            <td>${item.warehouse}</td>
            <td>${item.warehouseCode}</td>
            <td>${remarkCell}</td>
          </tr>
        `;
        }
      }

      // 페이지네이션 렌더링
      function renderPagination() {
        const totalPages = Math.ceil(filteredData.length / rowsPerPage);
        const pagination = document.getElementById("pagination");
        pagination.innerHTML = "";
        if (totalPages <= 1) return;

        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, startPage + 4);
        if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);

        if (currentPage > 1) {
          pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="prev">&laquo;</a></li>`;
        }
        for (let i = startPage; i <= endPage; i++) {
          pagination.innerHTML += `<li class="page-item${
            i === currentPage ? " active" : ""
          }"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
        }
        if (currentPage < totalPages) {
          pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="next">&raquo;</a></li>`;
        }
      }

      // 페이지네이션 클릭
      document
        .getElementById("pagination")
        .addEventListener("click", function (e) {
          if (e.target.tagName !== "A") return;
          e.preventDefault();
          const totalPages = Math.ceil(filteredData.length / rowsPerPage);
          let page = e.target.getAttribute("data-page");
          if (page === "prev") currentPage = Math.max(1, currentPage - 1);
          else if (page === "next")
            currentPage = Math.min(totalPages, currentPage + 1);
          else currentPage = parseInt(page);
          renderTable();
          renderPagination();
        });

      // 비고 내용 모달
      document
        .getElementById("productTableBody")
        .addEventListener("click", function (e) {
          if (e.target.classList.contains("btn-remark")) {
            const remark = e.target.getAttribute("data-remark");
            document.getElementById("remarkModalBody").textContent = remark;
            const modal = new bootstrap.Modal(
              document.getElementById("remarkModal")
            );
            modal.show();
          }
          if (e.target.classList.contains("name-link")) {
            e.preventDefault();
            // 페이지 이동 예시 (실제 이동은 필요에 따라 구현)
            alert(`${e.target.dataset.name} 상세 페이지로 이동`);
            location.href=""
          }
        });
      // 추가 버튼
      document.getElementById("addBtn").addEventListener("click", function (e) {
        e.preventDefault();
        alert("제품정보 입력페이지로 이동합니다.");
        location.href = "/item/item_write";
      });
      // 최초 렌더링
      //filterAndRender();