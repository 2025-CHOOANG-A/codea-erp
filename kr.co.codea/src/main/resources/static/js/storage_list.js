// searchBtn과 filterForm은 전역 스코프에서 접근 가능하도록 선언
const searchBtn = document.getElementById("searchBtn");
const filterForm = document.getElementById("filterForm");

// 검색 버튼 클릭 이벤트 리스너
if (searchBtn) {
    searchBtn.addEventListener("click", function() {
        submitSearchForm();
    });
}

// 검색 제출 함수
function submitSearchForm() {
    if (filterForm) {
        filterForm.action = "/storage"; // 올바른 URL
        filterForm.method = "GET";
        filterForm.submit();
    } else {
        console.error("Error: filterForm not found.");
    }
}

// Sidebar toggle (기존 코드 유지)
const sidebar = document.getElementById("sidebar");
const sidebarToggle = document.getElementById("sidebarToggle");
if (sidebarToggle) {
    sidebarToggle.addEventListener("click", () => {
        if (sidebar) sidebar.classList.toggle("collapsed");
    });
}

function handleResize() {
    if (window.innerWidth < 992) {
        if (sidebar) sidebar.classList.add("collapsed");
    } else {
        if (sidebar) sidebar.classList.remove("collapsed");
    }
}
window.addEventListener("resize", handleResize);
handleResize();



// "상세 보기" 모달을 위한 함수 (AJAX로 상세 정보 가져오기)
async function showDetail(whId) {
    if (!whId) {
        console.error("Error: whId is missing for detail view.");
        return;
    }

    try {
        const response = await fetch(`/api/storage/${whId}`); 
        
        if (!response.ok) {
            if (response.status === 404) {
                alert("창고 상세 정보를 찾을 수 없습니다.");
            } else {
                alert("창고 상세 정보를 가져오는 데 실패했습니다: " + response.statusText);
            }
            return;
        }

        const item = await response.json(); // JSON 응답 파싱

        // 모달 내용 설정
        const modalBody = document.getElementById("modalDetailBody");
        modalBody.innerHTML = `
            <tr><th>창고 ID</th><td>${item.whId || '-'}</td></tr>
            <tr><th>창고 코드</th><td>${item.whCode || '-'}</td></tr>
            <tr><th>창고명</th><td>${item.whName || '-'}</td></tr>
            <tr><th>주소</th><td>${item.address || '-'}</td></tr>
            <tr><th>상세 주소</th><td>${item.addressDetail || '-'}</td></tr>
            <tr><th>우편번호</th><td>${item.postCode || '-'}</td></tr>
            <tr><th>전화번호</th><td>${item.tel || '-'}</td></tr>
            <tr><th>담당자 사번</th><td>${item.empNo || '-'}</td></tr>
            <tr><th>담당자 명</th><td>${item.empName || '-'}</td></tr>
            <tr><th>비고</th><td>${item.remark || '-'}</td></tr>
        `;
        
        // Bootstrap 모달 인스턴스 생성 및 표시
        const detailModal = new bootstrap.Modal(document.getElementById("detailModal"));
        detailModal.show();

    } catch (error) {
        console.error("Error fetching storage detail:", error);
        alert("창고 상세 정보를 가져오는 중 오류가 발생했습니다.");
    }
}



// DOMContentLoaded 이벤트 리스너 내부 정리
document.addEventListener("DOMContentLoaded", () => {
    // 폼 제출 시 기본 동작 방지 (submitSearchForm에서 직접 제출)
    if (filterForm) {
        filterForm.addEventListener("submit", function (e) {
            e.preventDefault(); // 기본 제출 방지
            submitSearchForm(); // 직접 제출 함수 호출
        });
    }
	
	// "상세 보기" 버튼 클릭 이벤트 (클래스 이름 'detail-btn'으로 변경됨)
	    document.querySelector("#warehouseTable tbody")
	        .addEventListener("click", function (e) {
	            // 클릭된 요소가 'detail-btn' 클래스를 가지고 있는지 확인
	            if (e.target.classList.contains("detail-btn")) {
	                const whId = e.target.dataset.whId; // data-wh-id 속성 값 가져오기
	                showDetail(whId); // <-- 여기에서 showDetail 함수를 호출합니다!
	            }

	            // 'name-link' 클릭 이벤트 (HTML에서 사용되지 않는다면 제거)
	            if (e.target.classList.contains("name-link")) {
	                const id = e.target.dataset.id;
	                const type = e.target.dataset.type;
	                // handleNameClick(id, type); // 함수 호출
	                alert((type === "name" ? "창고명" : "담당자명") + " 페이지로 이동: " + id); // 직접 alert
	            }
	        });

	
	

    // 전체 선택 체크박스
    const selectAllCheckbox = document.getElementById("selectAll");
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener("change", function (e) {
            const checkboxes = document.querySelectorAll(
                '#warehouseTable tbody input[type="checkbox"]'
            );
            checkboxes.forEach((cb) => (cb.checked = e.target.checked));
        });
    }

    // "등록" 버튼 클릭 이벤트 (페이지 이동)
    const addBtn = document.querySelector(".add-btn");
    if (addBtn) {
        addBtn.addEventListener("click", function (e) {
            e.preventDefault();
            alert("등록 페이지로 이동합니다. (실제 URL로 변경 필요)");
            // 예시: location.href = "/storage/register";
        });
    }

    // "삭제" 버튼 클릭 이벤트 (서버 연동 필요)
    const deleteBtn = document.querySelector(".delete-btn");
    if (deleteBtn) {
        deleteBtn.addEventListener("click", function () {
            const checked = Array.from(
                document.querySelectorAll(
                    '#warehouseTable tbody input[type="checkbox"]:checked'
                )
            );
            if (checked.length === 0) {
                alert("삭제할 항목을 선택하세요.");
                return;
            }
            if (confirm("정말 삭제하시겠습니까?")) {
                alert("선택된 항목을 삭제합니다. (서버 연동 로직 여기에 구현)");
                // 예시: 삭제할 ID들을 수집하여 fetch API로 DELETE 요청
                // const idsToDelete = checked.map((cb) => cb.dataset.id);
                // fetch('/api/storages/delete', { method: 'POST', body: JSON.stringify(idsToDelete), headers: { 'Content-Type': 'application/json' } })
                //    .then(response => { if (response.ok) location.reload(); else alert('삭제 실패'); });
            }
        });
    }

});