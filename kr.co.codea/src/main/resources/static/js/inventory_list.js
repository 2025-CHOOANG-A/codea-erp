// 샘플 데이터
const stockData = [
	{재고번호:'S001', 제품코드:'P1001', 제품명:'가위', 제품구분:'자재', 규격:'7인치', 단가:1200, 보유수량:150, 단위:'EA', 재고창고:'본사창고', 창고코드:'W01', 담당자명:'김철수', 담당자코드:'E01', 창고명:'본사창고'},
	{재고번호:'S002', 제품코드:'P1002', 제품명:'노트', 제품구분:'완제품', 규격:'A4', 단가:2500, 보유수량:80, 단위:'EA', 재고창고:'2공장', 창고코드:'W02', 담당자명:'이영희', 담당자코드:'E02', 창고명:'2공장'},
	{재고번호:'S003', 제품코드:'P1003', 제품명:'연필', 제품구분:'자재', 규격:'HB', 단가:500, 보유수량:300, 단위:'EA', 재고창고:'본사창고', 창고코드:'W01', 담당자명:'박민수', 담당자코드:'E03', 창고명:'본사창고'},
	{재고번호:'S004', 제품코드:'P1004', 제품명:'지우개', 제품구분:'자재', 규격:'소', 단가:300, 보유수량:200, 단위:'EA', 재고창고:'3공장', 창고코드:'W03', 담당자명:'최지훈', 담당자코드:'E04', 창고명:'3공장'},
	{재고번호:'S005', 제품코드:'P1005', 제품명:'볼펜', 제품구분:'완제품', 규격:'0.7mm', 단가:800, 보유수량:120, 단위:'EA', 재고창고:'본사창고', 창고코드:'W01', 담당자명:'김철수', 담당자코드:'E01', 창고명:'본사창고'},
	{재고번호:'S006', 제품코드:'P1006', 제품명:'파일', 제품구분:'완제품', 규격:'A4', 단가:1800, 보유수량:60, 단위:'EA', 재고창고:'2공장', 창고코드:'W02', 담당자명:'이영희', 담당자코드:'E02', 창고명:'2공장'},
	{재고번호:'S007', 제품코드:'P1007', 제품명:'테이프', 제품구분:'자재', 규격:'18mm', 단가:700, 보유수량:90, 단위:'EA', 재고창고:'3공장', 창고코드:'W03', 담당자명:'최지훈', 담당자코드:'E04', 창고명:'3공장'},
	{재고번호:'S008', 제품코드:'P1008', 제품명:'클립', 제품구분:'자재', 규격:'대', 단가:200, 보유수량:500, 단위:'EA', 재고창고:'본사창고', 창고코드:'W01', 담당자명:'박민수', 담당자코드:'E03', 창고명:'본사창고'},
	{재고번호:'S009', 제품코드:'P1009', 제품명:'스테이플러', 제품구분:'완제품', 규격:'중', 단가:3500, 보유수량:40, 단위:'EA', 재고창고:'2공장', 창고코드:'W02', 담당자명:'이영희', 담당자코드:'E02', 창고명:'2공장'},
	{재고번호:'S010', 제품코드:'P1010', 제품명:'자', 제품구분:'자재', 규격:'30cm', 단가:900, 보유수량:110, 단위:'EA', 재고창고:'본사창고', 창고코드:'W01', 담당자명:'김철수', 담당자코드:'E01', 창고명:'본사창고'},
	{재고번호:'S011', 제품코드:'P1011', 제품명:'메모지', 제품구분:'완제품', 규격:'100매', 단가:1200, 보유수량:75, 단위:'EA', 재고창고:'3공장', 창고코드:'W03', 담당자명:'최지훈', 담당자코드:'E04', 창고명:'3공장'},
	{재고번호:'S012', 제품코드:'P1012', 제품명:'칼', 제품구분:'자재', 규격:'소', 단가:1500, 보유수량:55, 단위:'EA', 재고창고:'2공장', 창고코드:'W02', 담당자명:'이영희', 담당자코드:'E02', 창고명:'2공장'}
];

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

// 페이지네이션 렌더링
function renderPagination() {
	let filtered = stockData.slice();
	if(currentFilter !== '전체') filtered = filtered.filter(row => row.제품구분 === currentFilter);
	if(searchKeyword) filtered = filtered.filter(row => {
		const value = row[searchField] || '';
		return String(value).toLowerCase().includes(searchKeyword.toLowerCase());
	});
	filtered.sort((a, b) => a.제품명.localeCompare(b.제품명, 'ko'));
	const totalRows = filtered.length;
	const totalPages = Math.max(1, Math.ceil(totalRows/rowsPerPage));
	const pagination = document.getElementById('pagination');
	pagination.innerHTML = '';
	let pageStart = Math.max(1, currentPage-2);
	let pageEnd = Math.min(totalPages, pageStart+4);
	if(pageEnd-pageStart<4) pageStart = Math.max(1, pageEnd-4);

	if(totalPages > 5 && currentPage > 1) {
		pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="1">&laquo;</a></li>`;
		pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage-1}">&lsaquo;</a></li>`;
	}
	for(let i=pageStart; i<=pageEnd; i++) {
		pagination.innerHTML += `<li class="page-item${i===currentPage?' active':''}"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
	}
	if(totalPages > 5 && currentPage < totalPages) {
		pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage+1}">&rsaquo;</a></li>`;
		pagination.innerHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${totalPages}">&raquo;</a></li>`;
	}
	// 이벤트
	pagination.querySelectorAll('a.page-link').forEach(a => {
		a.addEventListener('click', function(e) {
			e.preventDefault();
			const page = Number(this.dataset.page);
			if(page && page !== currentPage) {
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