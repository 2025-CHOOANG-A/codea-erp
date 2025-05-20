// Set max date for stockDate and allow today to be selected
const stockDateInput = document.getElementById('stockDate');
const today = new Date();
const yyyy = today.getFullYear();
const mm = String(today.getMonth() + 1).padStart(2, '0');
const dd = String(today.getDate()).padStart(2, '0');
const todayStr = `${yyyy}-${mm}-${dd}`;
stockDateInput.max = todayStr;
stockDateInput.value = todayStr;

// 제품 검색 모달
const productSearchModal = new bootstrap.Modal(document.getElementById('productSearchModal'));
document.getElementById('searchProductBtn').addEventListener('click', () => {
	document.getElementById('productSearchInput').value = '';
	document.querySelector('#productSearchTable tbody').innerHTML = '';
	productSearchModal.show();
});
document.getElementById('productSearchForm').addEventListener('submit', function(e){
	e.preventDefault();
	// 예시 데이터
	const products = [
		{code:'P001', name:'제품A', type:'완제품', spec:'100x200', unit:'EA', price:1000},
		{code:'P002', name:'제품B', type:'반제품', spec:'50x100', unit:'BOX', price:500},
		{code:'P003', name:'제품C', type:'원자재', spec:'30x60', unit:'EA', price:300}
	];
	const keyword = document.getElementById('productSearchInput').value.trim();
	const filtered = products.filter(p => p.name.includes(keyword));
	const tbody = document.querySelector('#productSearchTable tbody');
	tbody.innerHTML = '';
	filtered.forEach(p => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${p.code}</td>
			<td>${p.name}</td>
			<td>${p.type}</td>
			<td>${p.spec}</td>
			<td>${p.unit}</td>
			<td>${p.price}</td>
			<td><button type="button" class="btn btn-sm btn-primary select-product">선택</button></td>
		`;
		tr.querySelector('.select-product').addEventListener('click', () => {
			document.getElementById('productCode').value = p.code;
			document.getElementById('productName').value = p.name;
			document.getElementById('productType').value = p.type;
			document.getElementById('productSpec').value = p.spec;
			document.getElementById('productUnit').value = p.unit;
			document.getElementById('productPrice').value = p.price;
			productSearchModal.hide();
		});
		tbody.appendChild(tr);
	});
});

// 창고 검색 모달
const warehouseSearchModal = new bootstrap.Modal(document.getElementById('warehouseSearchModal'));
document.getElementById('searchWarehouseBtn').addEventListener('click', () => {
	document.getElementById('warehouseSearchInput').value = '';
	document.querySelector('#warehouseSearchTable tbody').innerHTML = '';
	warehouseSearchModal.show();
});
document.getElementById('warehouseSearchForm').addEventListener('submit', function(e){
	e.preventDefault();
	// 예시 데이터
	const warehouses = [
	    {code:'W001', name:'본사창고', addr:'서울시 강남구'},
	    {code:'W002', name:'2공장창고', addr:'경기도 수원시'},
	    {code:'W003', name:'외주창고', addr:'인천시 남동구'}
	];
	const keyword = document.getElementById('warehouseSearchInput').value.trim();
	const filtered = warehouses.filter(w => w.name.includes(keyword));
	const tbody = document.querySelector('#warehouseSearchTable tbody');
	tbody.innerHTML = '';
	filtered.forEach(w => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${w.code}</td>
			<td>${w.name}</td>
			<td>${w.addr}</td>
			<td><button type="button" class="btn btn-sm btn-primary select-warehouse">선택</button></td>
		`;
		tr.querySelector('.select-warehouse').addEventListener('click', () => {
			document.getElementById('warehouseCode').value = w.code;
			document.getElementById('warehouseName').value = w.name;
			warehouseSearchModal.hide();
		});
		tbody.appendChild(tr);
	});
});

// 담당자 검색 모달
const managerSearchModal = new bootstrap.Modal(document.getElementById('managerSearchModal'));
document.getElementById('searchManagerBtn').addEventListener('click', () => {
	document.getElementById('managerSearchInput').value = '';
	document.querySelector('#managerSearchTable tbody').innerHTML = '';
	managerSearchModal.show();
});
document.getElementById('managerSearchForm').addEventListener('submit', function(e){
	e.preventDefault();
	// 예시 데이터
	const managers = [
		{code:'M001', name:'홍길동', phone:'010-1234-5678'},
		{code:'M002', name:'김철수', phone:'010-2345-6789'},
		{code:'M003', name:'이영희', phone:'010-3456-7890'}
	];
	const keyword = document.getElementById('managerSearchInput').value.trim();
	const filtered = managers.filter(m => m.name.includes(keyword));
	const tbody = document.querySelector('#managerSearchTable tbody');
	tbody.innerHTML = '';
	filtered.forEach(m => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${m.code}</td>
			<td>${m.name}</td>
			<td>${m.phone}</td>
			<td><button type="button" class="btn btn-sm btn-primary select-manager">선택</button></td>
		`;
		tr.querySelector('.select-manager').addEventListener('click', () => {
			document.getElementById('managerName').value = m.name;
			document.getElementById('managerPhone').value = m.phone;
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