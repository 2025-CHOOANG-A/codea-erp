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

// 제품 검색 모달
const productSearchModal = new bootstrap.Modal(document.getElementById('productSearchModal'));

const sea_item = JSON.parse(document.getElementById("sea_item_json").textContent);

document.getElementById('searchProductBtn').addEventListener('click', () => {
	document.getElementById('productSearchInput').value = '';
	document.querySelector('#productSearchTable tbody').innerHTML = '';
	productSearchModal.show();
});
document.getElementById('productSearchForm').addEventListener('submit', function(e){
	e.preventDefault();

	const keyword = document.getElementById('productSearchInput').value.trim();
	const filtered = sea_item.filter(p => p.name.includes(keyword));
	const tbody = document.querySelector('#productSearchTable tbody');
	tbody.innerHTML = '';
	filtered.forEach(p => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${p.itemCode}</td>
			<td>${p.itemName}</td>
			<td>${p.itemType}</td>
			<td>${p.spec}</td>
			<td>${p.code}</td>
			<td><button type="button" class="btn btn-sm btn-primary select-product">선택</button></td>
		`;
		tr.querySelector('.select-product').addEventListener('click', () => {
			document.getElementById('itemCode').value = p.itemCode;
			document.getElementById('itemName').value = p.itemName;
			document.getElementById('itemType').value = p.itemType;
			document.getElementById('spec').value = p.spec;
			document.getElementById('code').value = p.code;
			productSearchModal.hide();
		});
		tbody.appendChild(tr);
	});
});

// 창고 검색 모달
const warehouseSearchModal = new bootstrap.Modal(document.getElementById('warehouseSearchModal'));

const sea_wh   = JSON.parse(document.getElementById("sea_wh_json").textContent);

document.getElementById('searchWarehouseBtn').addEventListener('click', () => {
	document.getElementById('warehouseSearchInput').value = '';
	document.querySelector('#warehouseSearchTable tbody').innerHTML = '';
	warehouseSearchModal.show();
});
document.getElementById('warehouseSearchForm').addEventListener('submit', function(e){
	e.preventDefault();

	const keyword = document.getElementById('warehouseSearchInput').value.trim();
	const filtered = sea_wh.filter(w => w.name.includes(keyword));
	const tbody = document.querySelector('#warehouseSearchTable tbody');
	tbody.innerHTML = '';
	filtered.forEach(w => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${w.whCode}</td>
			<td>${w.whName}</td>
			<td>${w.address + w.addressDetail}</td>
			<td><button type="button" class="btn btn-sm btn-primary select-warehouse">선택</button></td>
		`;
		tr.querySelector('.select-warehouse').addEventListener('click', () => {
			document.getElementById('whCode').value = w.whCode;
			document.getElementById('whName').value = w.whName;
			warehouseSearchModal.hide();
		});
		tbody.appendChild(tr);
	});
});

// 담당자 검색 모달
const managerSearchModal = new bootstrap.Modal(document.getElementById('managerSearchModal'));

const sea_emp  = JSON.parse(document.getElementById("sea_emp_json").textContent);

document.getElementById('searchManagerBtn').addEventListener('click', () => {
	document.getElementById('managerSearchInput').value = '';
	document.querySelector('#managerSearchTable tbody').innerHTML = '';
	managerSearchModal.show();
});
document.getElementById('managerSearchForm').addEventListener('submit', function(e){
	e.preventDefault();

	const keyword = document.getElementById('managerSearchInput').value.trim();
	const filtered = sea_emp.filter(m => m.name.includes(keyword));
	const tbody = document.querySelector('#managerSearchTable tbody');
	tbody.innerHTML = '';
	filtered.forEach(m => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${m.empNo}</td>
			<td>${m.empName}</td>
			<td>${m.hp}</td>
			<td><button type="button" class="btn btn-sm btn-primary select-manager">선택</button></td>
		`;
		tr.querySelector('.select-manager').addEventListener('click', () => {
			document.getElementById('empName').value = m.empName;
			document.getElementById('empHp').value = m.hp;
			managerSearchModal.hide();
		});
		tbody.appendChild(tr);
	});
});

// Prevent modal input line breaks
document.querySelectorAll('.modal input').forEach(input => {
	input.addEventListener('keydown', e => {
		if (e.key === 'Enter') e.preventDefault();
	});
});