const DateInput = document.getElementById('createdAt');
const today = new Date();
const yyyy = today.getFullYear();
const mm = String(today.getMonth() + 1).padStart(2, '0');
const dd = String(today.getDate()).padStart(2, '0');
const todayStr = `${yyyy}-${mm}-${dd}`;

DateInput.min = todayStr;
DateInput.max = todayStr;
DateInput.value = todayStr;
DateInput.readOnly = true;	// 날짜 선택 불가

// 보유 수량 자동 등록
function load_qty(){
	const itemId = document.getElementById("itemId").value;
	const whId = document.getElementById("whId").value;
	const itemType = document.getElementById("itemType").value;
	const empNo = document.getElementById("empNo").value;
	
	if(itemId && whId && itemType){	// itemId, whId, itemType.value 값이 존재할 때		
		fetch(`/inventory/curQty?itemId=${itemId}&whId=${whId}&itemType=${encodeURIComponent(itemType)}`)
		.then(aa => {
			return aa.json();
		}).then(bb => {
			document.getElementById("currentQty").value = bb;
		}).catch(error => {
			console.log(error);
		});
	}
}

itemCode.addEventListener("change", load_qty);
whCode.addEventListener("change", load_qty);

// 등록 버튼
function in_add(){
	if(itemCode.value == ""){
		alert("제품 정보를 입력하세요.");
	}
	else if(whCode.value == ""){
		alert("창고 정보를 입력하세요.");
	}
	else if(hp.value == ""){
		alert("담당자 정보를 입력하세요.");
	}
	else{
		frm.submit();
	}
}

// 취소 버튼
function in_can(){
	if(confirm('재고 등록을 취소하겠습니까?')){
		location.href="/inventory/list";
	}
}