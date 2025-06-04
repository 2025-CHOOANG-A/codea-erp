document.addEventListener("DOMContentLoaded", function () {
      /*** 1. Sidebar 토글(생략) ***/
      const sidebar = document.getElementById("sidebar");
      document.getElementById("sidebarOpenBtn").onclick = () =>
        sidebar.classList.remove("closed");
      document.getElementById("sidebarCloseBtn").onclick = () =>
        sidebar.classList.add("closed");
      function handleSidebar() {
        if (window.innerWidth >= 992) sidebar.classList.remove("closed");
        else sidebar.classList.add("closed");
      }
      window.addEventListener("resize", handleSidebar);
      window.addEventListener("DOMContentLoaded", handleSidebar);

      /*** 2. 거래처 검색 모달 열기 및 필터링 ***/
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

      /*** 3. 거래처 검색 결과 행 클릭 시 부모 폼에 값 채우기 ***/
      // 모달 내의 <tr>들이 모두 렌더링된 뒤에 이벤트 바인딩
      document.querySelectorAll("#clientSearchResult tr").forEach((row) => {
        row.addEventListener("click", function () {
          // 클릭된 행의 data-name, data-code, data-phone 읽어서
          const selectedName  = this.dataset.name;
          const selectedCode  = this.dataset.code;
          const selectedPhone = this.dataset.phone;

          // 부모 폼 입력란 요소
          const inputBpName  = document.getElementById("bpName");
          const inputBpCode  = document.getElementById("bpCode");
          const inputBpPhone = document.getElementById("Tel");

          // 혹시 요소가 없으면 로그 남기고 중단
          if (!inputBpName || !inputBpCode || !inputBpPhone) {
            console.error("bpName, bpCode, Tel 입력란을 찾을 수 없습니다.");
            return;
          }

          // 값 채워 넣기
          inputBpName.value  = selectedName;
          inputBpCode.value  = selectedCode;
          inputBpPhone.value = selectedPhone;

          // 모달 닫기
          clientModal.hide();
        });
      });

      /*** 4. 폼 검증 및 AJAX 수정 요청 ***/
      const form = document.getElementById("productForm");
      form.addEventListener("submit", function (e) {
        e.preventDefault(); // 기본 폼 제출 막기

        // 필수값 검증 (ID가 실제 HTML과 일치해야 함)
        let error = "";
        const requiredFields = [
          { id: "itemType", label: "제품구분" },
          { id: "itemCatL", label: "대분류" },
          { id: "itemCatS", label: "소분류" },
          { id: "itemName", label: "제품명" },
          { id: "spec", label: "규격" },
          { id: "unitCode", label: "단위" },
          { id: "price", label: "단가" },
          { id: "bpName", label: "거래처" },
          { id: "bpCode", label: "거래처 코드" },
          { id: "Tel", label: "거래처 전화번호" },
        ];
        for (const f of requiredFields) {
          const el = document.getElementById(f.id);
          if (!el || !el.value || (el.type === "number" && el.value === "")) {
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
        }

        // 확인창 띄우기
        const confirmResult = confirm("정말 수정하시겠습니까?");
        if (confirmResult) {
          // 폼 ID가 productForm이라면
          document.getElementById("productForm").submit();
        }
      });
    });
        
        
        // AJAX 요청 준비
        /*
        const formData = new FormData(form);
        const urlParams = new URLSearchParams();
      */
        // Controller의 @PostMapping("/item_editeok") 경로와 정확히 일치시킵니다.
        /*
        fetch("/item/item_editeok", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: formData,
        })
          .then((res) => {
            if (!res.ok) throw new Error("서버 오류 발생");
            return res.text(); // 혹은 JSON이면 .json()
          })
          .then((result) => {
            alert("수정이 완료되었습니다.");
            // 수정 완료 후 목록으로 이동
            window.location.href = "/item/item_list";
          })
          .catch((err) => {
            console.error(err);
            document.getElementById("formError").style.display = "block";
            document.getElementById("formError").innerText =
              "저장 중 오류가 발생했습니다.";
          });
      });
    });
  */
  
  