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

// 입고 수량 자동 계산
document.getElementById("quantity").addEventListener("input", function(){
	const quantity = this.value;
	const itemUnitCost = document.getElementById("itemUnitCost").value;
	
	const total = quantity * itemUnitCost;
	
	document.getElementById("inCost").value = total;
});