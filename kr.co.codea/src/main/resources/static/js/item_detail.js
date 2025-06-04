const sidebar = document.getElementById("sidebar");
     const sidebarToggle = document.getElementById("sidebarToggle");
     const mainContent = document.getElementById("mainContent");

     function setSidebar(show) {
       if (show) {
         sidebar.classList.add("show");
         mainContent.classList.add("with-sidebar");
       } else {
         sidebar.classList.remove("show");
         mainContent.classList.remove("with-sidebar");
       }
     }

     // 초기 상태: 데스크탑은 열림, 모바일은 닫힘
     function handleResize() {
       if (window.innerWidth >= 992) {
         setSidebar(true);
       } else {
         setSidebar(false);
       }
     }
     window.addEventListener("resize", handleResize);
     handleResize();

     sidebarToggle.addEventListener("click", () => {
       const isShown = sidebar.classList.contains("show");
       setSidebar(!isShown);
     });

     // 사이드바 바깥 클릭 시 닫기 (모바일)
     document.addEventListener("click", (e) => {
       if (window.innerWidth < 992 && sidebar.classList.contains("show")) {
         if (
           !sidebar.contains(e.target) &&
           !sidebarToggle.contains(e.target)
         ) {
           setSidebar(false);
         }
       }
     });

	 // DOMContentLoaded: 페이지의 모든 요소가 로드된 뒤 실행
	 document.addEventListener("DOMContentLoaded", function() {
	   // 1) “목록” 버튼 클릭 시 Alert
	   const btnList = document.getElementById("btnList");
	   if (btnList) {
	     btnList.addEventListener("click", function(event) {
	       // ① alert 창 띄우기
	       alert("목록 페이지로 이동합니다.");
	       // ② alert 확인 후, href대로 자동 이동됨 (event.preventDefault() 없음)
	     });
	   }

	   // 2) “수정” 버튼 클릭 시 Alert
	   const btnEdit = document.getElementById("btnEdit");
	   if (btnEdit) {
	     btnEdit.addEventListener("click", function(event) {
	       // ① alert 창 띄우기
	       alert("수정 페이지로 이동합니다.");
	       // ② alert 확인 후, href대로 자동 이동됨
	     });
	   }

	   // 3) “삭제” 버튼 클릭 시 Confirm
	 /*
	   const btnDelete = document.getElementById("btnDelete");
	   if (btnDelete) {
	     btnDelete.addEventListener("click", function(event) {
	       // confirm 창 띄워서 “확인” 누를 때만 이동
	       const ok = confirm("정말 이 항목을 삭제하시겠습니까?");
	       if (!ok) {
	         event.preventDefault();
	       }
	       // “확인” 누르면 아무 조치 없이 href대로 이동됨
	     });
	   }
	 });
	*/
	 const btnDelete = document.getElementById("btnDelete");
	  if (btnDelete) {
	    btnDelete.addEventListener("click", function(event) {
	      // confirm에서 “취소”를 누르면 함수 종료
	      if (!confirm("정말 이 항목을 삭제하시겠습니까?")) {
	        return;
	      }
		  
		  alert("삭제되었습니다.");
	      // hidden input에 이미 itemCode가 th:value로 세팅되어 있다고 가정
	      document.getElementById("deleteForm").submit();
	    });
	  }
	}); // ← 여기서 DOMContentLoaded 콜백 함수가 닫힙니다.
	// ↑ 여
	
	/*
	/*
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
		  
		  // 추가 버튼
		  		document.addEventListener("DOMContentLoaded", function () {
		  		     const addBtn = document.getElementById("addBtn");
		  		     if (!addBtn) return; // 버튼이 없다면 종료

		  		     addBtn.addEventListener("click", function (e) {
		  		       e.preventDefault();
		  		       alert("제품정보 입력페이지로 이동합니다.");
		  		       window.location.href = "/item/item_write";
		  		     });
		  		   });

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
		  /*
	      window.addEventListener("resize", () => {
	        if (window.innerWidth < 992) closeSidebar();
	        else openSidebar();
	      });
	      if (window.innerWidth < 992) closeSidebar();
	*/
	  
	      // 페이징/필터 변수
	      //let currentPage = 1;
	     // const rowsPerPage = 10;
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
		  /*
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
*/
		//Remark 모달 열기 스크립트
		/*
		      function showRemarkModal(button) {
		        const remark = button.getAttribute("data-remark") || "내용 없음";
		        document.getElementById("remarkModalBody").innerText = remark;
		        const remarkModal = new bootstrap.Modal(document.getElementById("remarkModal"));
		        remarkModal.show();
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

		  // 비고 모달 열기 함수 (비고 내용확인)
		  function showRemarkModal(button) {
		    const remark = button.getAttribute("data-remark"); // th:data-remark 사용 시 변환됨
		    const modalBody = document.getElementById("remarkModalBody");

		    if (remark && remark.trim() !== "") {
		      modalBody.textContent = remark;
		    } else {
		      modalBody.textContent = "비고 내용이 없습니다.";
		    }

		    // 부트스트랩 모달 열기
		    const remarkModal = new bootstrap.Modal(document.getElementById("remarkModal"));
		    remarkModal.show();
		  }
		   */
			/*
	      // 비고 내용 모달(초반)
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
			*/
			
	      // 최초 렌더링
	      //filterAndRender();	

		     // Sidebar toggle
			 /*
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
		  	  
		  	  // 추가 버튼
		  	  		document.addEventListener("DOMContentLoaded", function () {
		  	  		     const addBtn = document.getElementById("addBtn");
		  	  		     if (!addBtn) return; // 버튼이 없다면 종료

		  	  		     addBtn.addEventListener("click", function (e) {
		  	  		       e.preventDefault();
		  	  		       alert("제품정보 입력페이지로 이동합니다.");
		  	  		       window.location.href = "/item/item_write";
		  	  		     });
		  	  		   });

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
*/
		        // Always show sidebar toggle in header
		  	  /*
		        window.addEventListener("resize", () => {
		          if (window.innerWidth < 992) closeSidebar();
		          else openSidebar();
		        });
		        if (window.innerWidth < 992) closeSidebar();
		  */
		    
		        // 페이징/필터 변수
		       // let currentPage = 1;
		       // const rowsPerPage = 10;
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
				/*
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
*/
		  	//Remark 모달 열기 스크립트
			/*
		  	      function showRemarkModal(button) {
		  	        const remark = button.getAttribute("data-remark") || "내용 없음";
		  	        document.getElementById("remarkModalBody").innerText = remark;
		  	        const remarkModal = new bootstrap.Modal(document.getElementById("remarkModal"));
		  	        remarkModal.show();
		  	      }
				  */
		        // 페이지네이션 렌더링
				/*
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
*/
		  	  // 비고 모달 열기 함수 (비고 내용확인)
			  /*
		  	  function showRemarkModal(button) {
		  	    const remark = button.getAttribute("data-remark"); // th:data-remark 사용 시 변환됨
		  	    const modalBody = document.getElementById("remarkModalBody");

		  	    if (remark && remark.trim() !== "") {
		  	      modalBody.textContent = remark;
		  	    } else {
		  	      modalBody.textContent = "비고 내용이 없습니다.";
		  	    }

		  	    // 부트스트랩 모달 열기
		  	    const remarkModal = new bootstrap.Modal(document.getElementById("remarkModal"));
		  	    remarkModal.show();
		  	  }
			  */ 
		  		/*
		        // 비고 내용 모달(초반)
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
		  		*/
		  		
		        // 최초 렌더링
		       // filterAndRender();	
			
	
	
	
	 
	 	 