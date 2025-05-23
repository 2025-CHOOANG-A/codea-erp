// storage_write.js

// 메시지를 표시하는 함수 (alert 대신 사용)
function showMessage(id, message, isError = false) {
    const element = document.getElementById(id);
    element.textContent = message;
    element.classList.remove('hidden'); // 숨김 클래스 제거
    if (isError) {
        element.classList.remove('text-green-500'); // 성공 메시지 색상 제거
        element.classList.add('text-red-500'); // 에러 메시지 색상 추가
    } else {
        element.classList.remove('text-red-500'); // 에러 메시지 색상 제거
        element.classList.add('text-green-500'); // 성공 메시지 색상 추가
    }
}

// 모든 메시지를 숨기는 함수
function hideMessages() {
    document.getElementById('formError').classList.add('hidden');
    document.getElementById('formSuccess').classList.add('hidden');
}

// Daum 우편번호 검색 함수
function searchPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = ''; // 주소 변수
            var extraAddr = ''; // 참고항목 변수

            // 사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 도로명 주소를 선택했을 경우
                addr = data.roadAddress;
            } else { // 지번 주소를 선택했을 경우(J)
                addr = data.jibunAddress;
            }

            // 사용자가 선택한 주소가 도로명 타입일 때 참고항목을 조합한다.
            if(data.userSelectedType === 'R'){
                // 법정동명이 있을 경우 추가한다. (법정리는 제외)
                // 법정동의 경우 마지막 글자가 "동" "로" "가" 인 경우만.
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraAddr += data.bname;
                }
                // 건물명이 있고, 공동주택일 경우 추가한다.
                if(data.buildingName !== '' && data.apartment === 'Y'){
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
                if(extraAddr !== ''){
                    extraAddr = ' (' + extraAddr + ')';
                }
                // 조합된 참고항목을 상세주소 필드에 넣는다.
                document.getElementById('addressDetail').value = extraAddr;
            } else {
                document.getElementById('addressDetail').value = '';
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('postCode').value = data.zonecode;
            document.getElementById('address').value = addr;
            // 커서를 상세주소 필드로 이동한다.
            document.getElementById('addressDetail').focus();
        }
    }).open();
}

// 폼 유효성 검사 및 제출 처리
document.getElementById("warehouseForm").addEventListener("submit", function (e) {
    e.preventDefault(); // 기본 폼 제출 동작 방지
    hideMessages(); // 이전 메시지 숨기기

    // 필수 입력 필드 목록
    const requiredFields = [
        "whCode",
        "whName",
        "address",
        "postCode",
        "addressDetail",
        "empName",
        "empNo",
        "tel",
    ];
    let valid = true; // 유효성 검사 결과
    let firstInvalid = null; // 첫 번째 유효하지 않은 필드

    // 각 필수 필드에 대해 유효성 검사 수행
    requiredFields.forEach((id) => {
        const el = document.getElementById(id);
        if (!el.value.trim()) { // 값이 비어있거나 공백만 있는 경우
            el.classList.add("is-invalid"); // 유효하지 않음 스타일 추가
            if (!firstInvalid) firstInvalid = el; // 첫 번째 유효하지 않은 필드 저장
            valid = false; // 유효성 검사 실패
        } else {
            el.classList.remove("is-invalid"); // 유효함 스타일 제거
        }
    });

    // 유효성 검사 실패 시
    if (!valid) {
        showMessage("formError", "필수 입력 항목을 모두 입력해 주세요.", true); // 에러 메시지 표시
        if (firstInvalid) firstInvalid.focus(); // 첫 번째 유효하지 않은 필드로 포커스 이동
        return; // 함수 종료
    }

    // 유효성 검사 통과 시
    showMessage("formSuccess", "창고 정보가 성공적으로 등록되었습니다.", false); // 성공 메시지 표시

    // 여기에 실제 서버로 데이터를 전송하는 API 호출 코드를 추가할 수 있습니다.
    // 예시:
    // const formData = new FormData(this);
    // fetch('/api/registerWarehouse', {
    //     method: 'POST',
    //     body: formData
    // })
    // .then(response => response.json())
    // .then(data => {
    //     console.log('성공:', data);
    //     showMessage("formSuccess", "창고 정보가 성공적으로 등록되었습니다.", false);
    //     // 성공 후 리다이렉트 또는 폼 초기화
    //     setTimeout(() => {
    //         window.location.href = "/warehouse_list.html";
    //     }, 2000); // 2초 후 리다이렉트
    // })
    // .catch((error) => {
    //     console.error('에러:', error);
    //     showMessage("formError", "등록 중 오류가 발생했습니다.", true);
    // });

    // 데모를 위한 리다이렉션 시뮬레이션
    setTimeout(() => {
        window.location.href = "/storage/storage_register"; // 예시 리다이렉션
    }, 2000);
});

// 취소 버튼 클릭 이벤트 처리
document.getElementById("cancelBtn").addEventListener("click", function () {
    window.location.href = "/storage/write"; // 원래 폼 페이지로 리다이렉트
});

// 사이드바 토글 및 반응형 사이드바 (원래 코드에서 가져왔으나, 이 폼 기능과는 직접적인 관련 없음)
// 이 부분은 폼 기능과 직접적으로 관련이 없지만, 원본 JS에 있었으므로 주석 처리하여 유지합니다.
// const sidebar = document.getElementById("sidebar");
// const sidebarToggle = document.getElementById("sidebarToggle");
// if (sidebar && sidebarToggle) {
//     sidebarToggle.addEventListener("click", () => {
//         sidebar.classList.toggle("collapsed");
//     });
//     function handleResize() {
//         if (window.innerWidth < 992) {
//             sidebar.classList.add("collapsed");
//         } else {
//             sidebar.classList.remove("collapsed");
//         }
//     }
//     window.addEventListener("resize", handleResize);
//     handleResize();
// }
