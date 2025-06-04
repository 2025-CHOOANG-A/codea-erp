document.addEventListener("DOMContentLoaded", function () {
     // ── 1. 사이드바 토글 초기화 ──
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

     if (sidebarOpenBtn) sidebarOpenBtn.addEventListener("click", openSidebar);
     if (sidebarCloseBtn) sidebarCloseBtn.addEventListener("click", closeSidebar);

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

     // ── 2. “추가” 버튼 클릭 리스너 ──
     const addBtn = document.getElementById("addBtn");
     if (addBtn) {
       addBtn.addEventListener("click", function (e) {
         e.preventDefault();
         alert("제품 추가 페이지로 이동합니다.");
         window.location.href = "/item/item_write";
       });
     }

     // ── 3. 비고(remark) 모달 열기 함수 정의 ──
     window.showRemarkModal = function (button) {
       const remark = button.getAttribute("data-remark") || "내용 없음";
       const modalBody = document.getElementById("remarkModalBody");
       if (modalBody) {
         modalBody.textContent = remark;
       }
       const remarkModal = new bootstrap.Modal(
         document.getElementById("remarkModal")
       );
       remarkModal.show();
     };

     // ── 4. 테이블 렌더링/페이징 관련 (필요 시) ──
     //    * 여기에 filterAndRender(), renderTable(), renderPagination() 등을 넣을 수 있습니다.
     //    * 현재는 예시이므로, 호출 부분을 주석 처리합니다.
     /*
     let filteredData = sampleData.slice();
     function filterAndRender() {
       // … 필터 로직 …
       renderTable();
       renderPagination();
     }
     filterAndRender();
     */
   });