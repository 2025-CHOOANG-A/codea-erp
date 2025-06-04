document.addEventListener('DOMContentLoaded', () => {
  let materialList = []; // 자재 목록 저장용

  const tableBody = document.querySelector('#materialTable tbody');
  const modal = new bootstrap.Modal(document.getElementById('materialAddModal'));
  const form = document.getElementById('materialAddForm');

  // 자재 테이블 렌더링
  function renderTable() {
    tableBody.innerHTML = '';

    materialList.forEach((item, index) => {
      const row = document.createElement('tr');

      row.innerHTML = `
        <td><input type="checkbox" class="material-checkbox" data-index="${index}" /></td>
        <td>${item.bomCode}</td>
        <td>${item.materialCode}</td>
        <td>${item.materialName}</td>
        <td>${item.spec}</td>
        <td>${item.unit}</td>
        <td>${item.price}</td>
        <td>${item.qty}</td>
        <td>
          <button type="button" class="btn btn-sm btn-outline-secondary" onclick="editMaterial(${index})">수정</button>
        </td>
      `;

      tableBody.appendChild(row);
    });
  }

  // 자재 추가 버튼
  document.querySelector('.btn-add-material').addEventListener('click', () => {
    form.reset();
    form.editIdx.value = '';
    document.getElementById('modalBomCode').value = document.getElementById('bomCodeInput').value;
    modal.show();
  });

  // 자재 삭제 버튼
  document.querySelector('.btn-delete-material').addEventListener('click', () => {
    const checked = document.querySelectorAll('.material-checkbox:checked');
    const indexesToDelete = [...checked].map(chk => parseInt(chk.dataset.index));
    materialList = materialList.filter((_, idx) => !indexesToDelete.includes(idx));
    renderTable();
  });

  // 자재 추가 또는 수정 저장
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(form).entries());

    const material = {
      bomCode: data.bomCode,
      materialCode: data.materialCode,
      materialName: data.materialName,
      spec: data.spec,
      unit: data.unit,
      price: parseInt(data.price),
      qty: parseInt(data.qty),
    };

    if (data.editIdx) {
      materialList[parseInt(data.editIdx)] = material;
    } else {
      materialList.push(material);
    }

    modal.hide();
    renderTable();
  });

  // 수정 버튼 클릭 시
  window.editMaterial = (index) => {
    const item = materialList[index];
    for (let key in item) {
      const el = form.querySelector(`[name="${key}"]`);
      if (el) el.value = item[key];
    }
    form.editIdx.value = index;
    modal.show();
  };
  
  document.getElementById("btnList").addEventListener("click", () => {
    // Thymeleaf를 쓰는 경우라면 다음처럼 경로를 바인딩해도 되고,
    // window.location.href = "/bom/bom_list"; 로 해도 됩니다.
    alert("목록 페이지로 이동합니다.");
    window.location.href = /*[@{/bom/bom_list}]*/ "/bom/bom_list";
  });

  // 저장 버튼 눌렀을 때 전체 materialList를 form에 추가

  document.getElementById('btnSave').addEventListener('click', (e) => {
    const form = document.getElementById('bomForm');
    // 기존 input 삭제
    form.querySelectorAll('.dynamic-input').forEach(el => el.remove());

    materialList.forEach((item, idx) => {
      for (let key in item) {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = `materials[${idx}].${key}`;
        input.value = item[key];
        input.classList.add('dynamic-input');
        form.appendChild(input);
      }
    });
  });

  // 전체 선택 체크박스
  document.getElementById('selectAllMaterial').addEventListener('change', function () {
    document.querySelectorAll('.material-checkbox').forEach(chk => {
      chk.checked = this.checked;
    });
  });


document.getElementById('btnSave').addEventListener('click', (e) => {
  const form = document.getElementById('bomForm');
  // 기존에 추가된 동적 input 제거
  form.querySelectorAll('.dynamic-input').forEach(el => el.remove());

  materialList.forEach((item, idx) => {
    for (let key in item) {
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = `materials[${idx}].${key}`;
      input.value = item[key];
      input.classList.add('dynamic-input');
      form.appendChild(input);
    }
  });
});
  // 초기 렌더링 (기존 자재 로딩은 서버에서 추가할 수 있음)
  renderTable();
});
