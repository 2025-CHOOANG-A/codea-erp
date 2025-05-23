// 상태
let currentFilter = '전체';
let currentPage = 1;
let rowsPerPage = 10;
let searchField = '재고번호';
let searchKeyword = '';

// 필터 버튼
document.querySelectorAll('.filter-group .btn').forEach(btn => {
	btn.addEventListener('click', function() {
		document.querySelectorAll('.filter-group .btn').forEach(b => b.classList.remove('active'));
		this.classList.add('active');
		currentFilter = this.dataset.filter;
		currentPage = 1;
		renderTable();
		renderPagination();
	});
});

// 검색 폼
document.getElementById('searchForm').addEventListener('submit', function(e) {
	e.preventDefault();
	searchField = document.getElementById('searchField').value;
	searchKeyword = document.getElementById('searchInput').value.trim();
	currentPage = 1;
	renderTable();
	renderPagination();
});

// 테이블 렌더링
function renderTable() {
	let filtered = stockData.slice();
	// 분류 필터
	if(currentFilter !== '전체') {
		filtered = filtered.filter(row => row.제품구분 === currentFilter);
	}
	// 검색
	if(searchKeyword) {
		filtered = filtered.filter(row => {
			const value = row[searchField] || '';
			return String(value).toLowerCase().includes(searchKeyword.toLowerCase());
		});
	}
	// 가나다순 정렬(제품명)
	filtered.sort((a, b) => a.제품명.localeCompare(b.제품명, 'ko'));
	// 페이징
	const totalRows = filtered.length;
	const start = (currentPage-1)*rowsPerPage;
	const end = start + rowsPerPage;
	const pageRows = filtered.slice(start, end);
	
	const tbody = document.querySelector('#stockTable tbody');
	tbody.innerHTML = '';
	pageRows.forEach(row => {
		tbody.innerHTML += `
			<tr>
				<td>${row.재고번호}</td>
				<td>${row.제품코드}</td>
				<td><a href="#" class="name-link">${row.제품명}</a></td>
				<td>${row.제품구분}</td>
				<td>${row.규격}</td>
				<td class="text-end">${row.단가.toLocaleString()}</td>
				<td>${row.보유수량}</td>
				<td>${row.단위}</td>
				<td>${row.재고창고}</td>
				<td>${row.창고코드}</td>
				<td><a href="#" class="name-link">${row.담당자명}</a></td>
			</tr>
		`;
	});
}

function renderPagination() {
	let filtered = stockData.slice();
	if(currentFilter !== '전체') filtered = filtered.filter(row => row.제품구분 === currentFilter);
	if(searchKeyword) filtered = filtered.filter(row => {
		const value = row[searchField] || '';
		return String(value).toLowerCase().includes(searchKeyword.toLowerCase());
	});
	filtered.sort((a, b) => a.제품명.localeCompare(b.제품명, 'ko'));

	const totalRows = filtered.length;
	const totalPages = Math.max(1, Math.ceil(totalRows / rowsPerPage));
	const pagination = document.getElementById('pagination');
	pagination.innerHTML = '';

	let pageStart = Math.max(1, currentPage - 2);
	let pageEnd = Math.min(totalPages, pageStart + 4);
	if (pageEnd - pageStart < 4) pageStart = Math.max(1, pageEnd - 4);

	// << & < 버튼
	if (totalPages > 1 && currentPage > 1) {
		pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="1">&laquo;</a></li>`;
		pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage - 1}">&lsaquo;</a></li>`;
	}

	// 페이지 번호 버튼
	for (let i = pageStart; i <= pageEnd; i++) {
		pagination.innerHTML += `<li class="page-item${i === currentPage ? ' active' : ''}">
			<a class="page-link" href="#" data-page="${i}">${i}</a>
		</li>`;
	}

	// > & >> 버튼
	if (totalPages > 1 && currentPage < totalPages) {
		pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage + 1}">&rsaquo;</a></li>`;
		pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${totalPages}">&raquo;</a></li>`;
	}

	// 페이지 버튼 클릭 이벤트
	pagination.querySelectorAll('a.page-link').forEach(a => {
		a.addEventListener('click', function(e) {
			e.preventDefault();
			const page = Number(this.dataset.page);
			if (page && page !== currentPage) {
				currentPage = page;
				renderTable();
				renderPagination();
			}
		});
	});
}

// 초기 렌더링
renderTable();
renderPagination();