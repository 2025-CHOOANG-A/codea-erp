// "상세 보기" 모달을 위한 함수 (AJAX로 상세 정보 가져오기)
async function showDetail(whId) {
    if (!whId) {
        console.error("Error: whId is missing for detail view.");
        return;
    }

    try {
        // 창고 상세 정보를 가져오는 API 호출 (JSON 데이터를 반환)
        const response = await fetch(`/storage/api/${whId}`);

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
			<tr><th>담당자 전화번호</th><td>${item.empTel || '-'}</td></tr>
            <tr><th>비고</th><td>${item.remark || '-'}</td></tr>
			<tr><td colspan="2" class="text-center">
			    <a href="/storage/${item.whId}/edit" class="btn btn-dark mt-3">
			        <i class="bi bi-pencil me-1"></i> 창고 정보 수정
			    </a>
			</td></tr>
        `;

        // Bootstrap 모달 인스턴스 생성 및 표시
        const detailModal = new bootstrap.Modal(document.getElementById("detailModal"));
        detailModal.show();
    } catch (error) {
        console.error("Failed to fetch warehouse detail:", error);
        alert("창고 상세 정보를 가져오는 중 오류가 발생했습니다.");
    }
}
