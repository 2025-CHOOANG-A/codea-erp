// js/storage_write.js

// 커스텀 알림 모달 표시 함수 (alert() 대체)
function showCustomAlert(message) {
    const modalBody = document.getElementById('customAlertModalBody');
    if (modalBody) {
        modalBody.textContent = message;
        const customAlertModal = new bootstrap.Modal(document.getElementById('customAlertModal'));
        customAlertModal.show();
    } else {
        console.warn("customAlertModalBody 요소를 찾을 수 없습니다. 메시지: " + message);
    }
}

// 커스텀 확인 모달 표시 함수 (confirm() 대체)
function showCustomConfirm(message) {
    return new Promise((resolve) => {
        const modalBody = document.getElementById('customConfirmModalBody');
        const customConfirmModalElement = document.getElementById('customConfirmModal');

        if (modalBody && customConfirmModalElement) {
            modalBody.textContent = message;
            const customConfirmModal = new bootstrap.Modal(customConfirmModalElement);

            const okBtn = document.getElementById('confirmOkBtn');
            const cancelBtn = document.getElementById('confirmCancelBtn');

            // 기존 이벤트 리스너 제거 (중복 호출 방지)
            okBtn.onclick = null;
            cancelBtn.onclick = null;

            okBtn.onclick = () => {
                customConfirmModal.hide();
                resolve(true);
            };
            cancelBtn.onclick = () => {
                customConfirmModal.hide();
                resolve(false);
            };

            customConfirmModal.show();
        } else {
            console.warn("customConfirmModalBody 또는 customConfirmModal 요소를 찾을 수 없습니다. 기본 confirm 사용.");
            resolve(confirm(message));
        }
    });
}

// Daum 우편번호 검색 함수
function searchPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = ''; // 주소 변수
            var extraAddr = ''; // 참고항목 변수

            // 사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 도로명 주소를 선택 했을경우
                addr = data.roadAddress;
            } else { // 지번 주소를 선택 했을경우(J)
                addr = data.jibunAddress;
            }

            // 사용자가 선택한 주소가 도로명 타입일때 참고항목을 조합한다.
            if(data.userSelectedType === 'R'){
                // 법정동명이 있을 경우 추가한다. (법정리는 제외)
                // 법정동의 경우 마지막 글자가 "동" "로" "가" 인 경우만.
                // gtest는 정규식 객체에 대한 메서드이며, 문자열에 대한 테스트는 test()를 사용해야 합니다.
                // 여기서는 문자열 메서드 match() 또는 test()를 사용하는 것이 더 적합합니다.
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){ // .gtest()를 .test()로 수정
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
                document.getElementById('addressDetail').value = extraAddr; // id로 변경
            } else {
                document.getElementById('addressDetail').value = ''; // id로 변경
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('postCode').value = data.zonecode; // id로 변경
            document.getElementById('address').value = addr; // id로 변경
            // 커서를 상세주소 필드로 이동한다.
            document.getElementById('addressDetail').focus(); // id로 변경

            // 유효성 검사 피드백 제거
            document.getElementById('postCode').classList.remove('is-invalid');
            if (document.getElementById('postCode').nextElementSibling && document.getElementById('postCode').nextElementSibling.classList.contains('invalid-feedback')) {
                 document.getElementById('postCode').nextElementSibling.style.display = 'none';
            }
            document.getElementById('address').classList.remove('is-invalid');
            if (document.getElementById('address').nextElementSibling && document.getElementById('address').nextElementSibling.classList.contains('invalid-feedback')) {
                document.getElementById('address').nextElementSibling.style.display = 'none';
            }
        }
    }).open();
}

// 창고 등록 처리 함수 (storage_write.html 폼에 맞춰 수정)
async function registerWarehouse() {
    const warehouseForm = document.getElementById("warehouseForm");
    const formMessageDiv = document.getElementById("formMessage");

    if (!warehouseForm) {
        console.error("Error: warehouseForm not found.");
        return;
    }

    formMessageDiv.innerHTML = ''; // 이전 메시지 지우기

    const requiredFields = [
        "whCode",
        "whName",
        "postCode",
        "address",
        "addressDetail",
        "empName",
        "empNo",
        "tel",
    ];
    let valid = true;
    let firstInvalid = null;

    // 각 필수 필드에 대해 유효성 검사 수행
    requiredFields.forEach((id) => {
        const el = document.getElementById(id);
        const feedback = el.nextElementSibling; // invalid-feedback div

        if (!el.value.trim()) { // 값이 비어있거나 공백만 있는 경우
            el.classList.add("is-invalid"); // 유효하지 않음 스타일 추가
            if (feedback && feedback.classList.contains('invalid-feedback')) {
                feedback.style.display = 'block'; // 피드백 표시
            }
            if (!firstInvalid) firstInvalid = el;
            valid = false;
        } else {
            el.classList.remove("is-invalid"); // 유효함 스타일 제거
            if (feedback && feedback.classList.contains('invalid-feedback')) {
                feedback.style.display = 'none'; // 피드백 숨김
            }
        }
    });

    // 유효성 검사 실패 시
    if (!valid) {
        formMessageDiv.innerHTML = '<div class="alert alert-danger" role="alert">필수 입력 항목을 모두 입력해 주세요.</div>';
        if (firstInvalid) firstInvalid.focus();
        return;
    }

    // 제출 확인
    const confirmed = await showCustomConfirm("창고 정보를 등록하시겠습니까?");
    if (!confirmed) {
        return; // 사용자가 취소한 경우
    }

    // FormData 객체를 사용하여 폼 데이터 수집
    const formData = new FormData(warehouseForm);
    // FormData를 JSON 객체로 변환 (서버가 JSON을 기대하는 경우)
    const jsonData = {};
    formData.forEach((value, key) => {
        jsonData[key] = value;
    });

    try {
        const response = await fetch('/storage/storage_register', { // 백엔드 API 엔드포인트
            method: 'POST',
            headers: {
                'Content-Type': 'application/json' // 서버가 JSON을 소비하도록 설정
            },
            body: JSON.stringify(jsonData) // JSON 데이터를 문자열로 변환하여 전송
        });

        // 응답이 JSON이 아닐 경우 또는 네트워크 오류 발생 시
        if (!response.ok) {
            // 서버에서 에러 메시지를 JSON 형태로 보낼 것으로 예상
            // 만약 서버가 JSON이 아닌 HTML 응답을 보낸다면 여기서 오류가 발생할 수 있습니다.
            // 일반 컨트롤러에서 JSON 응답을 받으려면 해당 PostMapping 메서드에 @ResponseBody를 추가해야 합니다.
            const errorText = await response.text(); // 응답을 텍스트로 읽어 오류 디버깅에 활용
            console.error("Server response was not OK:", response.status, errorText);
            let errorMessage = '서버 오류가 발생했습니다.';
            try {
                const errorData = JSON.parse(errorText); // JSON 파싱 시도
                errorMessage = errorData.message || errorMessage;
            } catch (e) {
                // JSON 파싱 실패 시 (예: HTML 응답)
                errorMessage = `서버 응답 오류 (HTTP ${response.status}). 자세한 내용은 콘솔을 확인하세요.`;
            }

            formMessageDiv.innerHTML = `<div class="alert alert-danger" role="alert">등록 실패: ${errorMessage}</div>`;
            showCustomAlert(`창고 등록에 실패했습니다: ${errorMessage}`);
            return;
        }

        const result = await response.json(); // 서버 응답 파싱

        if (result.success) {
            formMessageDiv.innerHTML = '<div class="alert alert-success" role="alert">창고 정보가 성공적으로 등록되었습니다.</div>';
            showCustomAlert("창고 정보가 성공적으로 등록되었습니다.");
            // 성공 시 페이지 이동
            setTimeout(() => {
                location.href = '/storage'; // 창고 목록 페이지로 이동
            }, 1000);
        } else {
            formMessageDiv.innerHTML = `<div class="alert alert-danger" role="alert">등록 실패: ${result.message || '알 수 없는 오류'}</div>`;
            showCustomAlert(`창고 등록에 실패했습니다: ${result.message || '알 수 없는 오류'}`);
        }

    } catch (error) {
        console.error("Error during warehouse registration:", error);
        formMessageDiv.innerHTML = '<div class="alert alert-danger" role="alert">네트워크 오류 또는 서버 응답 처리 중 문제가 발생했습니다.</div>';
        showCustomAlert("창고 등록 중 네트워크 오류가 발생했습니다.");
    }
}

// 취소 처리 함수
async function go_cancel() {
    const confirmed = await showCustomConfirm("등록을 취소하시겠습니까?");
    if (confirmed) {
        location.href = '/storage'; // 취소 시 이동할 페이지
    }
}

// DOMContentLoaded 이벤트 리스너
document.addEventListener("DOMContentLoaded", () => {
    // 폼 제출 이벤트 리스너
    const warehouseForm = document.getElementById("warehouseForm");
    if (warehouseForm) {
        warehouseForm.addEventListener("submit", function(event) {
            event.preventDefault(); // 기본 폼 제출 방지
            registerWarehouse();
        });
    }

    // 취소 버튼 이벤트 리스너
    const cancelBtn = document.getElementById("cancelBtn");
    if (cancelBtn) {
        cancelBtn.addEventListener("click", go_cancel);
    }
});
