// 날짜 선택
const DateInput_se = document.getElementById('ship_sea_date');
const today_se = new Date();
const yyyy_se = today_se.getFullYear();
const mm_se = String(today_se.getMonth() + 1).padStart(2, '0');
const dd_se = String(today_se.getDate()).padStart(2, '0');
const todayStr_se = `${yyyy_se}-${mm_se}-${dd_se}`;

DateInput_se.max = todayStr_se;

// 주문 및 제품 검색
document.getElementById("sourceDocType_sel").addEventListener("change", function(){
	const sourceDocType = this.value;
	const header = document.querySelectorAll(".ship_sea_header");
	
	if(sourceDocType == "42"){
		header[0].textContent = "주문 번호";
		header[1].textContent = "주문 일자";
		header[2].textContent = "주문 수량";
	}
	else if(sourceDocType == "43"){
		header[0].textContent = "생산 계획 번호";
		header[1].textContent = "생산 일자";
		header[2].textContent = "생산 수량";
	}
});

document.getElementById("searchProductBtn").addEventListener("click", () => {
	const modal = new bootstrap.Modal(document.getElementById("ship_sea"));
	modal.show();
});

document.getElementById("ship_sea_form").addEventListener("submit", function(e){
	e.preventDefault();
	
	const sourceDocType = document.getElementById("sourceDocType_sel").value;
	const keyword = document.getElementById("ship_sea_date").value.trim();
	const tbody = document.querySelector("#item_sea_table tbody");
	tbody.innerHTML = "";
	
	if(!sourceDocType){
		tbody.innerHTML = `<tr><td colspan="6" class="text-center">옵션을 선택해 주세요.</td></tr>`;
		return;
	}
	
	fetch(`/shipment/searchItem?sourceDocType=${sourceDocType}&docDate=${encodeURIComponent(keyword)}`)
	.then(aa => {
		return aa.json();
	}).then(bb => {
		if(bb.length == 0){
			tbody.innerHTML = `<tr><td colspan="6" class="text-center">검색 결과가 없습니다.</td></tr>`;
			return;
		}
		else{
			bb.forEach(item => {
				const tr = document.createElement("tr");
				
				tr.innerHTML = `
					<td>${item.docNo}</td>
					<td>${item.docDate}</td>
					<td>${item.itemCode}</td>
					<td>${item.itemName}</td>
					<td>${item.docQty}</td>
					<td>
						<button type="button" class="btn btn-sm btn-primary select-product">선택</button>
					</td>
				`;
				
				// 선택 버튼 클릭
				tr.querySelector(".select-product").addEventListener("click", () => {
					const docNoLabel = document.querySelector("label[for='docNo']");
					const docDateLabel = document.querySelector("label[for='docDate']");
					const docQtyLabel = document.querySelector("label[for='docQty']");
					const docCostLabel = document.querySelector("label[for='docCost']");
					const inoutType = document.getElementById("inoutType");
					
					document.getElementById("itemCode").value = item.itemCode;
					document.getElementById("itemName").value = item.itemName;
					document.getElementById("itemType").value = item.itemType;
					document.getElementById("spec").value = item.spec;
					document.getElementById("code").value = item.code;
					document.getElementById("price").value = item.price.toLocaleString() + " 원";
					document.getElementById("docNo").value = item.docNo;
					document.getElementById("docDate").value = item.docDate.substring(0, 10);
					document.getElementById("docQty").value = item.docQty;
					document.getElementById("docCost").value = item.docCost.toLocaleString() + " 원";
					document.getElementById("qty").value = item.qty;
					
					document.getElementById("itemId").value = item.itemId;	// itemId 저장
					document.getElementById("itemUnitCost").value = item.itemUnitCost;	// itemUnitCost 저장
					document.getElementById("sourceDocType").value = item.sourceDocType;	// sourceDocType 저장
					document.getElementById("sourceDocHeaderId").value = item.sourceDocHeaderId;	// sourceDocHeaderId 저장
					document.getElementById("sourceDocDetailId").value = item.sourceDocDetailId;	// sourceDocDetailId 저장
					
					if(item.sourceDocType == "42"){
						docNoLabel.textContent = "주문 번호";
						docDateLabel.textContent = "주문 일자";
						docQtyLabel.textContent = "주문 수량";
						docCostLabel.textContent = "주문 총액";

						inoutType.value = "25";
					}
					if(item.sourceDocType == "43"){
						docNoLabel.textContent = "생산 계획 번호";
						docDateLabel.textContent = "생산 일자";
						docQtyLabel.textContent = "생산 수량";
						docCostLabel.textContent = "생산 총액";

						inoutType.value = "26";
					}
					
					load_qty();
					
					// 닫기
					bootstrap.Modal.getInstance(document.getElementById("ship_sea")).hide();
				});
				
				tbody.appendChild(tr);
			});
		}
	}).catch(error => {
		console.log(error);
	});
});

// 창고 검색
document.getElementById("searchWarehouseBtn").addEventListener("click", () => {
	const modal = new bootstrap.Modal(document.getElementById("wh_sea"));
	modal.show();
});

document.getElementById("wh_sea_form").addEventListener("submit", function(e){
	e.preventDefault();
	
	const keyword = document.getElementById("wh_sea_in").value.trim();
	const tbody = document.querySelector("#wh_sea_table tbody");
	tbody.innerHTML = "";
	
	fetch(`/shipment/searchWh?whName=${encodeURIComponent(keyword)}`)
	.then(aa => {
		return aa.json();
	}).then(bb => {
		if(bb.length == 0){
			tbody.innerHTML = `<tr><td colspan='4'>검색 결과가 없습니다.</td></tr>`;
			return;
		}
		else{
			bb.forEach(wh => {
				const tr = document.createElement("tr");
				
				tr.innerHTML = `
					<td class="text-center">${wh.whCode}</td>
					<td class="text-center">${wh.whName}</td>
					<td>${wh.address}${wh.addressDetail}</td>
					<td class="text-center">
						<button type="button" class="btn btn-sm btn-primary select-wh">선택</button>
					</td>
				`;
				
				// 선택 버튼 클릭
				tr.querySelector(".select-wh").addEventListener("click", () => {
					document.getElementById("whCode").value = wh.whCode;
					document.getElementById("whName").value = wh.whName;
					
					document.getElementById("whId").value = wh.whId;	// whId 저장
					
					load_qty();

					// 닫기
					bootstrap.Modal.getInstance(document.getElementById("wh_sea")).hide();
				});
				
				tbody.appendChild(tr);
			});
		}
	}).catch(error => {
		console.log(error);
	});
});

// 담당자 검색
document.getElementById("searchManagerBtn").addEventListener("click", () => {
	const modal = new bootstrap.Modal(document.getElementById("emp_sea"));
	modal.show();
});

document.getElementById("emp_sea_form").addEventListener("submit", function(e){
	e.preventDefault();
	
	const keyword = document.getElementById("emp_sea_in").value.trim();
	const tbody = document.querySelector("#emp_sea_table tbody");
	tbody.innerHTML = "";
	
	fetch(`/shipment/searchEmp?empName=${encodeURIComponent(keyword)}`)
	.then(aa => {
		return aa.json();
	}).then(bb => {
		if(bb.length == 0){
			tbody.innerHTML = `<tr><td colspan='4'>검색 결과가 없습니다.</td></tr>`;
			return;
		}
		else{
			bb.forEach(emp => {
				const tr = document.createElement("tr");
				
				tr.innerHTML = `
					<td>${emp.empNo}</td>
					<td>${emp.empName}</td>
					<td>${emp.hp}</td>
					<td>
						<button type="button" class="btn btn-sm btn-primary select-emp">선택</button>
					</td>
				`;
				
				// 선택 버튼 클릭
				tr.querySelector(".select-emp").addEventListener("click", () => {
					document.getElementById("empName").value = emp.empName;
					document.getElementById("hp").value = emp.hp;
					
					document.getElementById("empId").value = emp.empId;	// empId 저장
					document.getElementById("empNo").value = emp.empNo;	// empNo 저장
					
					// 닫기
					bootstrap.Modal.getInstance(document.getElementById("emp_sea")).hide();
				});
				
				tbody.appendChild(tr);
			});
		}
	}).catch(error => {
		console.log(error);
	});
});