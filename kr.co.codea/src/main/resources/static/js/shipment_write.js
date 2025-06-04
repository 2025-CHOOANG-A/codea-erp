const DateInput = document.getElementById('inoutTime');
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
	
	if(itemId && whId){
		fetch(`/shipment/curQty?itemId=${itemId}&whId=${whId}`)
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

// 출고 총액 자동 계산
document.getElementById("quantity").addEventListener("input", function(){
	const quantity = this.value;
	const itemUnitCost = document.getElementById("itemUnitCost").value;
	
	const total = quantity * itemUnitCost;
	
	document.getElementById("dis_outCost").value = total.toLocaleString() + " 원";	// 화면에 보여지는 금액
	document.getElementById("outCost").value = total;	// 서버에 전송되는 금액
});

// 중복 체크
function ship_check(){
	if(docNo.value == ""){
		alert("주문 및 생산 계획 정보를 입력하세요.");
	}
	else{
		const sourceDocType = document.getElementById("sourceDocType").value;
		const sourceDocHeaderId = document.getElementById("sourceDocHeaderId").value;
		const itemId = document.getElementById("itemId").value;
		
		fetch(`/shipment/writeCk?sourceDocType=${sourceDocType}&sourceDocHeaderId=${sourceDocHeaderId}&itemId=${itemId}`)
		.then(aa => {
			return aa.text();
		}).then(bb => {
			if(bb == "ok"){
				alert("해당 제품은 출고 등록이 가능합니다.");
				shck.value = "Y";
			}
			else if(bb == "exist"){
				alert("해당 제품은 이미 출고 처리된 상태입니다.");
				shck.value = "N";
			}
		}).catch(error => {
			console.log(error);
		});
	}
}

const qty = document.getElementById("qty");

// 등록 버튼
function ship_add(){
	if(docNo.value == ""){
		alert("주문 및 생산 계획 정보를 입력하세요.");
	}
	else if(quantity.value == ""){
		alert("출고 수량을 입력하세요.");
	}
	else if(Number(quantity.value) > Number(qty.value)){
		alert("출고 수량은 가출고 수량을 초과할 수 없습니다.");
		frm.quantity.value = "";
		frm.quantity.focus();
	}
	else if(whCode.value == ""){
		alert("창고 정보를 입력하세요.");
	}
	else if(hp.value == ""){
		alert("담당자 정보를 입력하세요.");
	}
	else if(shck.value != "Y"){
		alert("중복 여부를 먼저 확인해 주세요.");
	}
	else{
		frm.submit();
	}
}

// 취소 버튼
function ship_can(){
	if(confirm('출고 등록을 취소하겠습니까?')){
		location.href="/shipment/list";
	}
}