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

const inck = document.getElementById("inck");

// 중복 체크
function inv_check(){
	if(itemCode.value == ""){
		alert("제품 정보를 입력하세요.");
	}
	else if(whCode.value == ""){
		alert("창고 정보를 입력하세요.");
	}
	else{
		const itemId = document.getElementById("itemId").value;
		const whId = document.getElementById("whId").value;
		
		fetch(`/inventory/writeCk?itemId=${itemId}&whId=${whId}`)
		.then(aa => {
			return aa.text();
		}).then(bb => {
			if(bb == "ok"){
				alert("등록 가능한 재고입니다.");
				inck.value = "Y";
			}
			else if(bb == "exist"){
				alert("이미 등록된 재고입니다.");
				inck.value = "N";
			}
			else if(bb == "nodata"){
				alert("입고 이력이 없어 등록할 수 없습니다.");
				inck.value = "N";
			}
		}).catch(error => {
			console.log(error);
		});
	}
}

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
	else if(inck.value != "Y"){
		alert("중복 여부를 먼저 확인해 주세요.");
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