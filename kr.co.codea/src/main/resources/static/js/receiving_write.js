// Set min date for 입고 일자
document.getElementById('inDate').min = new Date().toISOString().split('T')[0];

// 총액 자동 계산
function calcTotal() {
	const qty = parseInt(document.getElementById('inQty').value) || 0;
	const price = parseInt(document.getElementById('productPrice').value) || 0;
	document.getElementById('totalAmount').value = qty * price;
}
document.getElementById('inQty').addEventListener('input', calcTotal);

// 샘플 데이터
const products = [
	{code:'P001', name:'제품A', spec:'100ml', unit:'EA', price:1000},
	{code:'P002', name:'제품B', spec:'200ml', unit:'EA', price:2000},
	{code:'P003', name:'제품C', spec:'300ml', unit:'EA', price:3000}
];
const orders = [
	{no:'O001', date:'2024-06-01', pcode:'P001', pname:'제품A', qty:10, amount:10000},
	{no:'O002', date:'2024-06-02', pcode:'P002', pname:'제품B', qty:20, amount:40000}
];
const warehouses = [
	{code:'W001', name:'창고A', addr:'서울시 강남구'},
	{code:'W002', name:'창고B', addr:'부산시 해운대구'}
];
const managers = [
	{name:'홍길동', code:'M001', phone:'010-1111-2222'},
	{name:'김철수', code:'M002', phone:'010-3333-4444'}
];

// 제품 검색
document.getElementById('productSearchBtn').onclick = function() {
	const val = document.getElementById('productSearchInput').value.trim();
	const tbody = document.querySelector('#productSearchTable tbody');
	tbody.innerHTML = '';
	products.filter(p => p.name.includes(val)).forEach(p => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${p.code}</td>
			<td>${p.name}</td>
			<td>${p.spec}</td>
			<td>${p.unit}</td>
			<td>${p.price}</td>
			<td><button class="btn btn-sm btn-primary" type="button">선택</button></td>
		`;
		tr.querySelector('button').onclick = function() {
			document.getElementById('productCode').value = p.code;
			document.getElementById('productName').value = p.name;
			document.getElementById('productSpec').value = p.spec;
			document.getElementById('productUnit').value = p.unit;
			document.getElementById('productPrice').value = p.price;
			document.getElementById('productModal').querySelector('.btn-close').click();
			calcTotal();
		};
		tbody.appendChild(tr);
	});
};

// 발주 검색
document.getElementById('orderSearchBtn').onclick = function() {
	const val = document.getElementById('orderSearchInput').value.trim();
	const tbody = document.querySelector('#orderSearchTable tbody');
	tbody.innerHTML = '';
	orders.filter(o => o.pname.includes(val)).forEach(o => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${o.no}</td>
			<td>${o.date}</td>
			<td>${o.pcode}</td>
			<td>${o.pname}</td>
			<td>${o.qty}</td>
			<td>${o.amount}</td>
			<td><button class="btn btn-sm btn-primary" type="button">선택</button></td>
		`;
		tr.querySelector('button').onclick = function() {
			document.getElementById('orderNo').value = o.no;
			document.getElementById('orderDate').value = o.date;
			document.getElementById('orderQty').value = o.qty;
			document.getElementById('orderModal').querySelector('.btn-close').click();
		};
		tbody.appendChild(tr);
	});
};

// 창고 검색
document.getElementById('warehouseSearchBtn').onclick = function() {
	const val = document.getElementById('warehouseSearchInput').value.trim();
	const tbody = document.querySelector('#warehouseSearchTable tbody');
	tbody.innerHTML = '';
	warehouses.filter(w => w.name.includes(val)).forEach(w => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${w.code}</td>
			<td>${w.name}</td>
			<td>${w.addr}</td>
			<td><button class="btn btn-sm btn-primary" type="button">선택</button></td>
		`;
		tr.querySelector('button').onclick = function() {
			document.getElementById('warehouseCode').value = w.code;
			document.getElementById('warehouseName').value = w.name;
			document.getElementById('warehouseModal').querySelector('.btn-close').click();
		};
		tbody.appendChild(tr);
	});
};

// 담당자 검색
document.getElementById('managerSearchBtn').onclick = function() {
	const val = document.getElementById('managerSearchInput').value.trim();
	const tbody = document.querySelector('#managerSearchTable tbody');
	tbody.innerHTML = '';
	managers.filter(m => m.name.includes(val)).forEach(m => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${m.name}</td>
			<td>${m.code}</td>
			<td>${m.phone}</td>
			<td><button class="btn btn-sm btn-primary" type="button">선택</button></td>
		`;
		tr.querySelector('button').onclick = function() {
			document.getElementById('managerName').value = m.name;
			document.getElementById('managerPhone').value = m.phone;
			document.getElementById('managerModal').querySelector('.btn-close').click();
		};
		tbody.appendChild(tr);
	});
};

// 검색 input 엔터키로 검색
['productSearchInput','orderSearchInput','warehouseSearchInput','managerSearchInput'].forEach(id=>{
	document.getElementById(id).addEventListener('keydown',function(e){
		if(e.key==='Enter'){
			e.preventDefault();
			document.getElementById(id.replace('Input','Btn')).click();
		}
	});
});