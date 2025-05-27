const DateInput = document.getElementById('updatedAt');
const today = new Date();
const yyyy = today.getFullYear();
const mm = String(today.getMonth() + 1).padStart(2, '0');
const dd = String(today.getDate()).padStart(2, '0');
const todayStr = `${yyyy}-${mm}-${dd}`;

DateInput.min = todayStr;
DateInput.max = todayStr;
DateInput.value = todayStr;
DateInput.readOnly = true;	// 날짜 선택 불가

const before_Qty = document.getElementById("before_Qty");
const currentQty = document.getElementById("currentQty");
const hp = document.getElementById("hp");
const reason = document.getElementById("reason");

function in_mod(){	// 수정 버튼
	if(currentQty.value == ""){
		alert("수정할 재고 수량을 입력하세요.");
		frm.currentQty.focus();
	}
	else if(before_Qty.value == currentQty.value){
		alert("입력하신 수량은 현재 수량과 동일합니다.");
	}
	else if(hp.value == ""){
		alert("담당자 정보를 입력하세요.");
	}
	else if(reason.value == ""){
		alert("수정 사유를 입력해 주세요.");
		frm.reason.focus();
	}
	else{
		frm.submit();
	}
}

function in_can(inventoryId){	// 취소 버튼
	if(confirm('재고 수정을 취소하겠습니까?')){
		location.href = '/inventory/detail?inventoryId=' + inventoryId;
	}
}