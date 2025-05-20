// 샘플 데이터
const sampleData = [
	{
		입고번호: 'IN20240610-001', 입고일: '2024-06-10', 제품코드: 'P1001', 제품명: '고급볼트', 발주번호: 'PO20240601-001', 발주일: '2024-06-01', 규격: 'M10x50', 단가: 1200, 입고수량: 500, 총액: 600000, 창고명: '본사창고', 담당자명: '홍길동', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240609-002', 입고일: '2024-06-09', 제품코드: 'P1002', 제품명: '스테인리스너트', 발주번호: 'PO20240601-002', 발주일: '2024-06-01', 규격: 'M10', 단가: 800, 입고수량: 1000, 총액: 800000, 창고명: '2공장', 담당자명: '김철수', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240608-003', 입고일: '2024-06-08', 제품코드: 'P1003', 제품명: '와셔', 발주번호: 'PO20240602-001', 발주일: '2024-06-02', 규격: 'M10', 단가: 100, 입고수량: 2000, 총액: 200000, 창고명: '본사창고', 담당자명: '이영희', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240607-004', 입고일: '2024-06-07', 제품코드: 'P1004', 제품명: '육각볼트', 발주번호: 'PO20240603-001', 발주일: '2024-06-03', 규격: 'M12x60', 단가: 1500, 입고수량: 300, 총액: 450000, 창고명: '3공장', 담당자명: '박민수', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240606-005', 입고일: '2024-06-06', 제품코드: 'P1005', 제품명: '플랜지너트', 발주번호: 'PO20240604-001', 발주일: '2024-06-04', 규격: 'M12', 단가: 900, 입고수량: 700, 총액: 630000, 창고명: '본사창고', 담당자명: '최지우', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240605-006', 입고일: '2024-06-05', 제품코드: 'P1006', 제품명: '스프링와셔', 발주번호: 'PO20240605-001', 발주일: '2024-06-05', 규격: 'M8', 단가: 110, 입고수량: 1500, 총액: 165000, 창고명: '2공장', 담당자명: '정우성', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240604-007', 입고일: '2024-06-04', 제품코드: 'P1007', 제품명: '육각너트', 발주번호: 'PO20240606-001', 발주일: '2024-06-06', 규격: 'M8', 단가: 700, 입고수량: 800, 총액: 560000, 창고명: '3공장', 담당자명: '한지민', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240603-008', 입고일: '2024-06-03', 제품코드: 'P1008', 제품명: '평와셔', 발주번호: 'PO20240607-001', 발주일: '2024-06-07', 규격: 'M6', 단가: 90, 입고수량: 2500, 총액: 225000, 창고명: '본사창고', 담당자명: '이준호', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240602-009', 입고일: '2024-06-02', 제품코드: 'P1009', 제품명: '육각볼트', 발주번호: 'PO20240608-001', 발주일: '2024-06-08', 규격: 'M16x80', 단가: 2200, 입고수량: 200, 총액: 440000, 창고명: '2공장', 담당자명: '김소연', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240601-010', 입고일: '2024-06-01', 제품코드: 'P1010', 제품명: '스테인리스볼트', 발주번호: 'PO20240609-001', 발주일: '2024-06-09', 규격: 'M8x40', 단가: 1800, 입고수량: 600, 총액: 1080000, 창고명: '3공장', 담당자명: '오세훈', 담당자링크: '#'
	},
	{
		입고번호: 'IN20240531-011', 입고일: '2024-05-31', 제품코드: 'P1011', 제품명: '육각볼트', 발주번호: 'PO20240610-001', 발주일: '2024-06-10', 규격: 'M20x100', 단가: 3000, 입고수량: 100, 총액: 300000, 창고명: '본사창고', 담당자명: '박지성', 담당자링크: '#'
	}
];

// 페이징 및 정렬
let currentPage = 1;
const rowsPerPage = 10;
let filteredData = [...sampleData];

function renderTable() {
	// 최신 입고일 기준 내림차순
	filteredData.sort((a, b) => b.입고일.localeCompare(a.입고일) || b.입고번호.localeCompare(a.입고번호));
	const startIdx = (currentPage - 1) * rowsPerPage;
	const pageData = filteredData.slice(startIdx, startIdx + rowsPerPage);
	
	const tbody = document.getElementById('inListTableBody');
	tbody.innerHTML = pageData.map(row => `
		<tr class="text-center">
			<td><input type="checkbox" /></td>
			<td>${row.입고번호}</td>
			<td>${row.입고일}</td>
			<td>${row.제품코드}</td>
			<td><a href="#" class="text-link">${row.제품명}</a></td>
			<td>${row.발주번호}</td>
			<td>${row.발주일}</td>
			<td>${row.규격}</td>
			<td class="text-end">${row.단가.toLocaleString()}</td>
			<td class="text-end">${row.입고수량.toLocaleString()}</td>
			<td class="text-end">${row.총액.toLocaleString()}</td>
			<td>${row.창고명}</td>
			<td><a href="${row.담당자링크}" class="text-link">${row.담당자명}</a></td>
		</tr>
	`).join('');
}

function renderPagination() {
	const totalPages = Math.ceil(filteredData.length / rowsPerPage);
	const pagination = document.getElementById('pagination');
	let html = '';
	
	if (totalPages > 5) {
		if (currentPage > 1) {
			html += `<li class="page-item"><a class="page-link" href="#" data-page="1">&laquo;</a></li>`;
			html += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage - 1}">&lt;</a></li>`;
		}
		let start = Math.max(1, currentPage - 2);
		let end = Math.min(totalPages, start + 4);
		if (end - start < 4) start = Math.max(1, end - 4);
		for (let i = start; i <= end; i++) {
			html += `<li class="page-item${i === currentPage ? ' active' : ''}"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
		}
		if (currentPage < totalPages) {
			html += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage + 1}">&gt;</a></li>`;
			html += `<li class="page-item"><a class="page-link" href="#" data-page="${totalPages}">&raquo;</a></li>`;
		}
	} else {
		for (let i = 1; i <= totalPages; i++) {
			html += `<li class="page-item${i === currentPage ? ' active' : ''}"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
		}
	}
	pagination.innerHTML = html;
}

document.getElementById('pagination').addEventListener('click', function(e) {
	if (e.target.tagName === 'A') {
		e.preventDefault();
		const page = Number(e.target.getAttribute('data-page'));
		if (!isNaN(page)) {
			currentPage = page;
			renderTable();
			renderPagination();
		}
	}
});

// 검색 필터
document.getElementById('searchForm').addEventListener('submit', function(e) {
	e.preventDefault();
	const field = document.getElementById('searchField').value;
	const keyword = document.getElementById('searchInput').value.trim();
	if (!keyword) {
		filteredData = [...sampleData];
	} else {
		filteredData = sampleData.filter(row => {
			let value = row[field] || '';
			return value.toString().toLowerCase().includes(keyword.toLowerCase());
		});
	}
	currentPage = 1;
	renderTable();
	renderPagination();
});

// 초기 렌더링
renderTable();
renderPagination();