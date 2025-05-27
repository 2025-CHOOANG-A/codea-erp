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