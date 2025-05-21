// 출고 일자 오늘 날짜로 min 설정
document.getElementById('releaseDate').min = new Date().toISOString().split('T')[0];

// 출고 코드 새로고침
document.getElementById('refreshReleaseCode').addEventListener('click', function() {
	const now = new Date();
	const code = 'RL' + now.getFullYear() + String(now.getMonth()+1).padStart(2,'0') + String(now.getDate()).padStart(2,'0') + '-' + String(Math.floor(Math.random()*900)+100);
	document.getElementById('releaseCode').value = code;
});

// 출고 수량 * 단가 = 총액
function updateTotalAmount() {
	const qty = Number(document.getElementById('releaseQty').value);
	const price = Number(document.getElementById('productPrice').value);
	document.getElementById('totalAmount').value = qty && price ? qty * price : '';
}
document.getElementById('releaseQty').addEventListener('input', updateTotalAmount);

// 모달 열기 함수
function openModal(id) {
	const modal = new bootstrap.Modal(document.getElementById(id));
	modal.show();
}

// 제품 검색
document.getElementById('searchProductBtn').addEventListener('click', () => openModal('productModal'));
// 주문 검색
document.getElementById('searchOrderBtn').addEventListener('click', () => openModal('orderModal'));
// 거래처 검색
document.getElementById('searchClientBtn').addEventListener('click', () => openModal('clientModal'));
// 창고 검색
document.getElementById('searchWarehouseBtn').addEventListener('click', () => openModal('warehouseModal'));
// 담당자 검색
document.getElementById('searchManagerBtn').addEventListener('click', () => openModal('managerModal'));

// 샘플 데이터
const products = [
	{code:'P001', name:'제품A', spec:'100ml', unit:'EA', price:1200},
	{code:'P002', name:'제품B', spec:'200ml', unit:'EA', price:2200},
	{code:'P003', name:'제품C', spec:'500ml', unit:'EA', price:3500}
];
const orders = [
	{no:'O20240601-01', date:'2024-06-01', pcode:'P001', pname:'제품A', qty:20, total:24000},
	{no:'O20240601-02', date:'2024-06-01', pcode:'P002', pname:'제품B', qty:10, total:22000}
];
const clients = [
	{code:'C001', name:'거래처A', addr:'서울시 강남구'},
	{code:'C002', name:'거래처B', addr:'부산시 해운대구'}
];
const warehouses = [
	{code:'W001', name:'창고A', addr:'경기도 수원시'},
	{code:'W002', name:'창고B', addr:'대전시 중구'}
];
const managers = [
	{name:'홍길동', code:'M001', phone:'010-1234-5678'},
	{name:'김철수', code:'M002', phone:'010-2345-6789'}
];

// 검색 및 테이블 렌더링 공통 함수
function renderTable(data, tableId, columns, selectHandler) {
	const tbody = document.getElementById(tableId).querySelector('tbody');
	tbody.innerHTML = '';
	data.forEach(row => {
		const tr = document.createElement('tr');
		columns.forEach(col => {
			const td = document.createElement('td');
			td.textContent = row[col];
			tr.appendChild(td);
		});
		const tdBtn = document.createElement('td');
		const btn = document.createElement('button');
		btn.className = 'btn btn-sm btn-primary';
		btn.textContent = '선택';
		btn.addEventListener('click', () => selectHandler(row));
		tdBtn.appendChild(btn);
		tr.appendChild(tdBtn);
		tbody.appendChild(tr);
	});
}

// 제품 검색
document.getElementById('productSearchBtn').addEventListener('click', function() {
	const keyword = document.getElementById('productSearchInput').value.trim();
	const filtered = products.filter(p => p.name.includes(keyword));
	renderTable(filtered, 'productTable', ['code','name','spec','unit','price'], function(row){
		document.getElementById('productCode').value = row.code;
		document.getElementById('productName').value = row.name;
		document.getElementById('productSpec').value = row.spec;
		document.getElementById('productUnit').value = row.unit;
		document.getElementById('productPrice').value = row.price;
		bootstrap.Modal.getInstance(document.getElementById('productModal')).hide();
		updateTotalAmount();
	});
});

// 주문 검색
document.getElementById('orderSearchBtn').addEventListener('click', function() {
	const keyword = document.getElementById('orderSearchInput').value.trim();
	const filtered = orders.filter(o => o.pname.includes(keyword));
	renderTable(filtered, 'orderTable', ['no','date','pcode','pname','qty','total'], function(row){
		document.getElementById('orderNo').value = row.no;
		document.getElementById('orderDate').value = row.date;
		document.getElementById('orderQty').value = row.qty;
		bootstrap.Modal.getInstance(document.getElementById('orderModal')).hide();
	});
});

// 거래처 검색
document.getElementById('clientSearchBtn').addEventListener('click', function() {
	const keyword = document.getElementById('clientSearchInput').value.trim();
	const filtered = clients.filter(c => c.name.includes(keyword));
	renderTable(filtered, 'clientTable', ['code','name','addr'], function(row){
		document.getElementById('clientCode').value = row.code;
		document.getElementById('clientName').value = row.name;
		bootstrap.Modal.getInstance(document.getElementById('clientModal')).hide();
	});
});

// 창고 검색
document.getElementById('warehouseSearchBtn').addEventListener('click', function() {
	const keyword = document.getElementById('warehouseSearchInput').value.trim();
	const filtered = warehouses.filter(w => w.name.includes(keyword));
	renderTable(filtered, 'warehouseTable', ['code','name','addr'], function(row){
		document.getElementById('warehouseCode').value = row.code;
		document.getElementById('warehouseName').value = row.name;
		bootstrap.Modal.getInstance(document.getElementById('warehouseModal')).hide();
	});
});

// 담당자 검색
document.getElementById('managerSearchBtn').addEventListener('click', function() {
	const keyword = document.getElementById('managerSearchInput').value.trim();
	const filtered = managers.filter(m => m.name.includes(keyword));
	renderTable(filtered, 'managerTable', ['name','code','phone'], function(row){
		document.getElementById('managerName').value = row.name;
		document.getElementById('managerPhone').value = row.phone;
		bootstrap.Modal.getInstance(document.getElementById('managerModal')).hide();
	});
});

// 모달 열릴 때마다 검색결과 초기화
['productModal','orderModal','clientModal','warehouseModal','managerModal'].forEach(id => {
	document.getElementById(id).addEventListener('show.bs.modal', function() {
		const input = this.querySelector('.search-input');
		if(input) input.value = '';
		const tbody = this.querySelector('tbody');
		if(tbody) tbody.innerHTML = '';
	});
});

// 검색 input에서 엔터키로 검색
document.getElementById('productSearchInput').addEventListener('keydown', function(e){
	if(e.key==='Enter') document.getElementById('productSearchBtn').click();
});
document.getElementById('orderSearchInput').addEventListener('keydown', function(e){
	if(e.key==='Enter') document.getElementById('orderSearchBtn').click();
});
document.getElementById('clientSearchInput').addEventListener('keydown', function(e){
	if(e.key==='Enter') document.getElementById('clientSearchBtn').click();
});
document.getElementById('warehouseSearchInput').addEventListener('keydown', function(e){
	if(e.key==='Enter') document.getElementById('warehouseSearchBtn').click();
});
document.getElementById('managerSearchInput').addEventListener('keydown', function(e){
	if(e.key==='Enter') document.getElementById('managerSearchBtn').click();
});