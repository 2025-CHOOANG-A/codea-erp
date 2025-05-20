// Sample data
const outData = [
	{
		출고번호: 'O20240601', 출고일: '2024-06-10', 제품코드: 'P1001', 제품명: '고급볼트', 규격: 'M8x30', 단가: 1200, 출고수량: 500, 단위: 'EA', 총액: 600000, 분류: '출고',
		주문번호: 'ORD20240601', 주문일자: '2024-06-09', 고객사명: '삼성전자', 창고명: '1창고', 담당자명: '홍길동', 담당자코드: 'E001', 고객사코드: 'C001', 창고코드: 'W01', 담당자전화번호: '010-1234-5678'
	},
	{
		출고번호: 'O20240602', 출고일: '2024-06-09', 제품코드: 'P1002', 제품명: '스프링와셔', 규격: 'M10', 단가: 300, 출고수량: 1000, 단위: 'EA', 총액: 300000, 분류: '생산',
		주문번호: 'ORD20240602', 주문일자: '2024-06-08', 고객사명: 'LG화학', 창고명: '2창고', 담당자명: '김철수', 담당자코드: 'E002', 고객사코드: 'C002', 창고코드: 'W02', 담당자전화번호: '010-2345-6789'
	},
	{
		출고번호: 'O20240603', 출고일: '2024-06-08', 제품코드: 'P1003', 제품명: '육각너트', 규격: 'M12', 단가: 500, 출고수량: 800, 단위: 'EA', 총액: 400000, 분류: '출고',
		주문번호: 'ORD20240603', 주문일자: '2024-06-07', 고객사명: '현대자동차', 창고명: '1창고', 담당자명: '이영희', 담당자코드: 'E003', 고객사코드: 'C003', 창고코드: 'W01', 담당자전화번호: '010-3456-7890'
	},
	{
		출고번호: 'O20240604', 출고일: '2024-06-07', 제품코드: 'P1004', 제품명: '플랜지', 규격: 'F20', 단가: 2000, 출고수량: 200, 단위: 'EA', 총액: 400000, 분류: '생산',
		주문번호: 'ORD20240604', 주문일자: '2024-06-06', 고객사명: '포스코', 창고명: '3창고', 담당자명: '박민수', 담당자코드: 'E004', 고객사코드: 'C004', 창고코드: 'W03', 담당자전화번호: '010-4567-8901'
	},
	{
		출고번호: 'O20240605', 출고일: '2024-06-06', 제품코드: 'P1005', 제품명: '와셔', 규격: 'M8', 단가: 150, 출고수량: 1500, 단위: 'EA', 총액: 225000, 분류: '출고',
		주문번호: 'ORD20240605', 주문일자: '2024-06-05', 고객사명: '한화솔루션', 창고명: '2창고', 담당자명: '최지훈', 담당자코드: 'E005', 고객사코드: 'C005', 창고코드: 'W02', 담당자전화번호: '010-5678-9012'
	},
	{
		출고번호: 'O20240606', 출고일: '2024-06-05', 제품코드: 'P1006', 제품명: '육각볼트', 규격: 'M16x40', 단가: 2500, 출고수량: 300, 단위: 'EA', 총액: 750000, 분류: '생산',
		주문번호: 'ORD20240606', 주문일자: '2024-06-04', 고객사명: 'SK하이닉스', 창고명: '1창고', 담당자명: '정유진', 담당자코드: 'E006', 고객사코드: 'C006', 창고코드: 'W01', 담당자전화번호: '010-6789-0123'
	},
	{
		출고번호: 'O20240607', 출고일: '2024-06-04', 제품코드: 'P1007', 제품명: '스페이서', 규격: 'S10', 단가: 800, 출고수량: 400, 단위: 'EA', 총액: 320000, 분류: '출고',
		주문번호: 'ORD20240607', 주문일자: '2024-06-03', 고객사명: '롯데케미칼', 창고명: '3창고', 담당자명: '한지민', 담당자코드: 'E007', 고객사코드: 'C007', 창고코드: 'W03', 담당자전화번호: '010-7890-1234'
	},
	{
		출고번호: 'O20240608', 출고일: '2024-06-03', 제품코드: 'P1008', 제품명: '너트', 규격: 'M10', 단가: 400, 출고수량: 900, 단위: 'EA', 총액: 360000, 분류: '생산',
		주문번호: 'ORD20240608', 주문일자: '2024-06-02', 고객사명: '기아', 창고명: '2창고', 담당자명: '서지훈', 담당자코드: 'E008', 고객사코드: 'C008', 창고코드: 'W02', 담당자전화번호: '010-8901-2345'
	},
	{
		출고번호: 'O20240609', 출고일: '2024-06-02', 제품코드: 'P1009', 제품명: '플레이트', 규격: 'PL50', 단가: 3500, 출고수량: 100, 단위: 'EA', 총액: 350000, 분류: '출고',
		주문번호: 'ORD20240609', 주문일자: '2024-06-01', 고객사명: '두산중공업', 창고명: '1창고', 담당자명: '이수진', 담당자코드: 'E009', 고객사코드: 'C009', 창고코드: 'W01', 담당자전화번호: '010-9012-3456'
	},
	{
		출고번호: 'O20240610', 출고일: '2024-06-01', 제품코드: 'P1010', 제품명: '볼트', 규격: 'M6x20', 단가: 1000, 출고수량: 600, 단위: 'EA', 총액: 600000, 분류: '생산',
		주문번호: 'ORD20240610', 주문일자: '2024-05-31', 고객사명: '현대모비스', 창고명: '3창고', 담당자명: '장민호', 담당자코드: 'E010', 고객사코드: 'C010', 창고코드: 'W03', 담당자전화번호: '010-0123-4567'
	},
	{
		출고번호: 'O20240611', 출고일: '2024-05-31', 제품코드: 'P1011', 제품명: '너트', 규격: 'M8', 단가: 350, 출고수량: 700, 단위: 'EA', 총액: 245000, 분류: '출고',
		주문번호: 'ORD20240611', 주문일자: '2024-05-30', 고객사명: '삼성SDI', 창고명: '2창고', 담당자명: '오지현', 담당자코드: 'E011', 고객사코드: 'C011', 창고코드: 'W02', 담당자전화번호: '010-2345-6780'
	}
];

// Table columns order
const columns = [
	'출고번호', '출고일', '제품코드', '제품명', '규격', '단가', '출고수량', '단위', '총액', '분류',
	'주문번호', '주문일자', '고객사명', '창고명', '담당자명'
];

// Paging
const rowsPerPage = 10;
let currentPage = 1;
let filteredData = [...outData];

function renderTable() {
	const tbody = document.getElementById('outTableBody');
	tbody.innerHTML = '';
	// Sort by 출고일 desc
	filteredData.sort((a, b) => b.출고일.localeCompare(a.출고일));
	const start = (currentPage - 1) * rowsPerPage;
	const pageData = filteredData.slice(start, start + rowsPerPage);

	pageData.forEach((row, idx) => {
		const tr = document.createElement('tr');
		// Checkbox
		const tdCheck = document.createElement('td');
		tdCheck.innerHTML = `<input type="checkbox" class="row-check">`;
		tr.appendChild(tdCheck);
		
		columns.forEach(col => {
			const td = document.createElement('td');
			// 오른쪽 정렬
			if (['단가', '출고수량', '총액'].includes(col)) {
			    td.className = 'text-end';
			    td.textContent = row[col].toLocaleString();
			}
			// 이름 링크
			else if (col === '제품명') {
			    td.innerHTML = `<a href="#" class="name-link" tabindex="0">${row[col]}</a>`;
			}
			else if (col === '고객사명') {
			    td.innerHTML = `<a href="#" class="name-link" tabindex="0">${row[col]}</a>`;
			}
			else if (col === '창고명') {
			    td.innerHTML = `<a href="#" class="name-link" tabindex="0">${row[col]}</a>`;
			}
			else if (col === '담당자명') {
			    td.innerHTML = `<a href="#" class="name-link" tabindex="0">${row[col]}</a>`;
			}
			// 가운데 정렬
			else {
			    td.textContent = row[col];
			}
			tr.appendChild(td);
		});
		tbody.appendChild(tr);
	});
	
	// 체크박스 전체선택 해제
	document.getElementById('checkAll').checked = false;
}

function renderPagination() {
	const totalPages = Math.max(1, Math.ceil(filteredData.length / rowsPerPage));
	const pagination = document.getElementById('pagination');
	pagination.innerHTML = '';
	
	let startPage = Math.max(1, currentPage - 2);
	let endPage = Math.min(totalPages, startPage + 4);
	if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);
	
	// Prev
	if (currentPage > 1) {
		const prev = document.createElement('li');
		prev.className = 'page-item';
		prev.innerHTML = `<a class="page-link" href="#" aria-label="이전">&laquo;</a>`;
		prev.onclick = e => { e.preventDefault(); currentPage--; updateTable(); };
		pagination.appendChild(prev);
	}
	
	// First page
	if (startPage > 1) {
		const first = document.createElement('li');
		first.className = 'page-item';
		first.innerHTML = `<a class="page-link" href="#">1</a>`;
		first.onclick = e => { e.preventDefault(); currentPage = 1; updateTable(); };
		pagination.appendChild(first);
		
		if (startPage > 2) {
			const dots = document.createElement('li');
			dots.className = 'page-item disabled';
			dots.innerHTML = `<span class="page-link">...</span>`;
			pagination.appendChild(dots);
		}
	}

	// Page numbers
	for (let i = startPage; i <= endPage; i++) {
		const li = document.createElement('li');
		li.className = 'page-item' + (i === currentPage ? ' active' : '');
		li.innerHTML = `<a class="page-link" href="#">${i}</a>`;
		li.onclick = e => { e.preventDefault(); currentPage = i; updateTable(); };
		pagination.appendChild(li);
	}

	// Last page
	if (endPage < totalPages) {
		if (endPage < totalPages - 1) {
			const dots = document.createElement('li');
			dots.className = 'page-item disabled';
			dots.innerHTML = `<span class="page-link">...</span>`;
			pagination.appendChild(dots);
		}
		const last = document.createElement('li');
		last.className = 'page-item';
		last.innerHTML = `<a class="page-link" href="#">${totalPages}</a>`;
		last.onclick = e => { e.preventDefault(); currentPage = totalPages; updateTable(); };
		pagination.appendChild(last);
	}

	// Next
	if (currentPage < totalPages) {
		const next = document.createElement('li');
		next.className = 'page-item';
		next.innerHTML = `<a class="page-link" href="#" aria-label="다음">&raquo;</a>`;
		next.onclick = e => { e.preventDefault(); currentPage++; updateTable(); };
		pagination.appendChild(next);
	}
}

function updateTable() {
	renderTable();
	renderPagination();
}

// 검색 및 필터
function filterData() {
	const category = document.querySelector('input[name="category"]:checked').value;
	const field = document.getElementById('searchField').value;
	const keyword = document.getElementById('searchInput').value.trim();
	
	filteredData = outData.filter(row => {
		let match = true;
		if (category !== '전체') match = row.분류 === category;
		if (match && keyword) {
			let fieldKey = field;
			// 필드명 매핑
			if (field === '고객사코드') fieldKey = '고객사코드';
			else if (field === '창고코드') fieldKey = '창고코드';
			else if (field === '담당자코드') fieldKey = '담당자코드';
			else if (field === '담당자전화번호') fieldKey = '담당자전화번호';
			else if (field === '출고번호') fieldKey = '출고번호';
			else if (field === '출고일') fieldKey = '출고일';
			else if (field === '제품코드') fieldKey = '제품코드';
			else if (field === '제품명') fieldKey = '제품명';
			else if (field === '주문번호') fieldKey = '주문번호';
			else if (field === '주문일자') fieldKey = '주문일자';
			else if (field === '고객사명') fieldKey = '고객사명';
			else if (field === '창고명') fieldKey = '창고명';
			else if (field === '담당자명') fieldKey = '담당자명';
			match = row[fieldKey] && row[fieldKey].toString().includes(keyword);
		}
		return match;
	});
	currentPage = 1;
	updateTable();
}

// 이벤트 바인딩
document.getElementById('searchBtn').addEventListener('click', filterData);
document.getElementById('searchForm').addEventListener('submit', filterData);
document.querySelectorAll('input[name="category"]').forEach(radio => {
	radio.addEventListener('change', filterData);
});

// 체크박스 전체선택
document.getElementById('checkAll').addEventListener('change', function() {
	document.querySelectorAll('#outTableBody .row-check').forEach(cb => cb.checked = this.checked);
});

// 등록 버튼 이동
document.getElementById('registerBtn').addEventListener('click', function(e) {
	e.preventDefault();
	window.location.href = '/register_out.html';
});

// 이름 링크 이동 (샘플)
document.getElementById('outTableBody').addEventListener('click', function(e) {
	if (e.target.classList.contains('name-link')) {
		e.preventDefault();
		// 실제 페이지 이동은 각 항목별로 구현 필요
		alert(e.target.textContent + ' 상세 페이지로 이동');
	}
});

// 초기 렌더링
updateTable();