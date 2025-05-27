// 제품 검색
document.getElementById("searchProductBtn").addEventListener("click", () => {
	const modal = new bootstrap.Modal(document.getElementById("item_sea"));
	modal.show();
});

document.getElementById("item_sea_form").addEventListener("submit", function(e){
	e.preventDefault();
	
	const keyword = document.getElementById("item_sea_in").value.trim();
	const tbody = document.querySelector("#item_sea_table tbody");
	tbody.innerHTML = "";
	
	fetch(`/inventory/searchItem?itemName=${encodeURIComponent(keyword)}`)
	.then(aa => {
		return aa.json();
	}).then(bb => {
		if(bb.length == 0){
			tbody.innerHTML = `<tr><td colspan='6'>검색 결과가 없습니다.</td></tr>`;
			return;
		}
		else{
			bb.forEach(item => {
				const tr = document.createElement("tr");
				
				tr.innerHTML = `
					<td>${item.itemCode}</td>
					<td>${item.itemName}</td>
					<td>${item.itemType}</td>
					<td>${item.spec}</td>
					<td>${item.code}</td>
					<td>
						<button type="button" class="btn btn-sm btn-primary select-product">선택</button>
					</td>
				`;
				
				// 선택 버튼 클릭
				tr.querySelector(".select-product").addEventListener("click", () => {
					document.getElementById("itemCode").value = item.itemCode;
					document.getElementById("itemName").value = item.itemName;
					document.getElementById("itemType").value = item.itemType;
					document.getElementById("spec").value = item.spec;
					document.getElementById("code").value = item.code;
					
					document.getElementById("itemId").value = item.itemId;	// itemId 저장
					
					load_qty();	// 재고 등록시 보유 수량 자동 입력을 위해 필요
					
					// 닫기
					bootstrap.Modal.getInstance(document.getElementById("item_sea")).hide();
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
	
	fetch(`/inventory/searchWh?whName=${encodeURIComponent(keyword)}`)
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
					<td>${wh.whCode}</td>
					<td>${wh.whName}</td>
					<td>${wh.address}${wh.addressDetail}</td>
					<td>
						<button type="button" class="btn btn-sm btn-primary select-product">선택</button>
					</td>
				`;
				
				// 선택 버튼 클릭
				tr.querySelector(".select-product").addEventListener("click", () => {
					document.getElementById("whCode").value = wh.whCode;
					document.getElementById("whName").value = wh.whName;
					
					document.getElementById("whId").value = wh.whId;	// whId 저장
					
					load_qty();	// 재고 등록시 보유 수량 자동 입력을 위해 필요
					
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
	
	fetch(`/inventory/searchEmp?empName=${encodeURIComponent(keyword)}`)
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
						<button type="button" class="btn btn-sm btn-primary select-product">선택</button>
					</td>
				`;
				
				// 선택 버튼 클릭
				tr.querySelector(".select-product").addEventListener("click", () => {
					document.getElementById("empName").value = emp.empName;
					document.getElementById("hp").value = emp.hp;
					
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