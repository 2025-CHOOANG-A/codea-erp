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

// Daum 우편번호 검색 함수 (client_write.html에서 가져옴)
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

// 사이드바 토글 함수 (client_write.html에서 가져옴)
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const content = document.getElementById('mainContent');
    if (sidebar) sidebar.classList.toggle('active'); // client_write.html에서는 'active' 클래스 사용
    if (content) content.classList.toggle('shifted'); // mainContent에도 shifted 클래스 토글
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
        const feedback = el.nextElementSibling; // invalid-feedback div (input-group 뒤에 있을 수 있으므로 변경)

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

    // 실제 서버 전송 로직 (fetch API 등 사용)
    // 현재는 데모용 메시지 표시 및 리다이렉션으로 대체
    formMessageDiv.innerHTML = '<div class="alert alert-success" role="alert">창고 정보가 성공적으로 등록되었습니다.</div>';
    showCustomAlert("창고 정보가 등록되었습니다.");
    setTimeout(() => {
        // location.href = '/storage'; // 성공 후 이동할 페이지
        console.log("창고 정보 등록 성공 (데모)");
        // 실제로는 폼을 제출하거나 AJAX 요청을 보냅니다.
        // warehouseForm.submit();
    }, 1000);
}

// 취소 처리 함수 (client_write.html에서 가져옴)
async function go_cancel() {
    const confirmed = await showCustomConfirm("등록을 취소하시겠습니까?");
    if (confirmed) {
        location.href = '/storage'; // 취소 시 이동할 페이지 (client_write.html의 /client 대신 /storage로 변경)
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
