// Sidebar toggle
     const sidebar = document.getElementById("sidebar");
     document.getElementById("sidebarOpenBtn").onclick = () =>
       sidebar.classList.remove("closed");
     document.getElementById("sidebarCloseBtn").onclick = () =>
       sidebar.classList.add("closed");
     // PC에서는 항상 열려있게
     function handleSidebar() {
       if (window.innerWidth >= 992) sidebar.classList.remove("closed");
       else sidebar.classList.add("closed");
     }
     window.addEventListener("resize", handleSidebar);
     window.addEventListener("DOMContentLoaded", handleSidebar);

     // 거래처 검색 모달
     const clientModal = new bootstrap.Modal(
       document.getElementById("clientSearchModal")
     );
     document.getElementById("searchClientBtn").onclick = () => {
       document.getElementById("clientSearchInput").value = "";
       filterClientRows("");
       clientModal.show();
     };
     document.getElementById("clientSearchInput").oninput = function () {
       filterClientRows(this.value);
     };
     function filterClientRows(keyword) {
       const rows = document.querySelectorAll("#clientSearchResult tr");
       rows.forEach((row) => {
         const name = row.dataset.name || "";
         row.style.display = name.includes(keyword) ? "" : "none";
       });
     }
     document.querySelectorAll("#clientSearchResult tr").forEach((row) => {
       row.onclick = function () {
         document.getElementById("bpName").value = this.dataset.name;
         document.getElementById("bpCode").value = this.dataset.code;
         document.getElementById("Tel").value = this.dataset.phone;
         clientModal.hide();
       };
     });

     // 폼 검증
     document.getElementById("productForm").onsubmit = function (e) {
       e.preventDefault();
       let error = "";
       // 필수값 체크
       const requiredFields = [
         { id: "productType", label: "제품구분" },
         { id: "mainCategory", label: "대분류" },
         { id: "subCategory", label: "소분류" },
         { id: "productName", label: "제품명" },
         { id: "spec", label: "규격" },
         { id: "unit", label: "단위" },
         { id: "price", label: "단가" },
         { id: "clientName", label: "거래처" },
         { id: "clientCode", label: "거래처 코드" },
         { id: "clientPhone", label: "거래처 전화번호" },
       ];
       for (const f of requiredFields) {
         const el = document.getElementById(f.id);
         if (!el.value || (el.type === "number" && el.value === "")) {
           error = `${f.label} 항목을 입력해 주세요.`;
           break;
         }
       }
       const errorDiv = document.getElementById("formError");
       if (error) {
         errorDiv.textContent = error;
         errorDiv.style.display = "";
         return false;
       } else {
         errorDiv.style.display = "none";
         alert("저장되었습니다.");
         // 실제 저장 로직 추가 필요
         return true;
       }
     };
	 
	 
	 
	 // 거래처 행 클릭 시 값 전달 + 모달 닫기
	 /*
	 document.querySelectorAll("#clientSearchResult tr").forEach(row => {
	   row.addEventListener("click", function () {
	     const name = this.dataset.name;
	     const code = this.dataset.code;
	     const phone = this.dataset.phone;

	     document.getElementById("bpCode").value = name;
	     document.getElementById("clientCode").value = code;
	     document.getElementById("Tel").value = phone;
	     // 모달 닫기 로직
	     const modalEl = document.getElementById("clientSearchModal");
	     const modalInstance = bootstrap.Modal.getInstance(modalEl);
	     if (modalInstance) {
	       modalInstance.hide();
	     } else {
	       // fallback: 강제로 닫기 (에러 방지용)
	       bootstrap.Modal.getOrCreateInstance(modalEl).hide();
	     }
	   });
	 });
	 */
