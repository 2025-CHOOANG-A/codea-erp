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