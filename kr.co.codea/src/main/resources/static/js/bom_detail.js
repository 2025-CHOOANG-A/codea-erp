// Sidebar toggle
    const sidebar = document.getElementById("sidebar");
    const sidebarToggle = document.getElementById("sidebarToggle");
    sidebarToggle.addEventListener("click", () => {
      sidebar.classList.toggle("collapsed");
    });
    function handleResize() {
      if (window.innerWidth < 992) {
        sidebar.classList.add("collapsed");
      } else {
        sidebar.classList.remove("collapsed");
      }
    }
    window.addEventListener("resize", handleResize);
    handleResize();

    // 샘플 BOM 상세 데이터
    let bomDetail = {
      bomCode: "BOM001",
      productCode: "P001",
      productName: "제품A",
      spec: "100x200",
      unit: "EA",
      note: "",
      materials: [
        {
          bomCode: "BOM001-01",
          materialCode: "M001",
          materialName: "자재X",
          spec: "100x200",
          unit: "EA",
          price: 1000,
          qty: 10,
        },
        {
          bomCode: "BOM001-02",
          materialCode: "M002",
          materialName: "자재Y",
          spec: "50x100",
          unit: "EA",
          price: 500,
          qty: 5,
        },
      ],
    };

    // BOM 상세 정보 렌더링
    function renderBomDetail() {
      document.getElementById("bomCodeCell").textContent = bomDetail.bomCode;
      document.getElementById("productCodeCell").textContent =
        bomDetail.productCode;
      document.getElementById("productNameCell").textContent =
        bomDetail.productName;
      document.getElementById("specCell").textContent = bomDetail.spec;
      document.getElementById("unitCell").textContent = bomDetail.unit;
      document.getElementById("noteCell").textContent = bomDetail.note || "";
    }

    // 자재 목록 렌더링
    function renderMaterialTable() {
      const tbody = document.querySelector("#materialTable tbody");
      tbody.innerHTML = "";
      bomDetail.materials.forEach((mat, idx) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
                      <td><input type="checkbox" data-idx="${idx}" /></td>
                      <td>${mat.bomCode}</td>
                      <td>${mat.materialCode}</td>
                      <td>${mat.materialName}</td>
                      <td>${mat.spec}</td>
                      <td>${mat.unit}</td>
                      <td>${mat.price.toLocaleString()}</td>
                      <td>${mat.qty}</td>
                  `;
        tbody.appendChild(tr);
      });
      document.getElementById("selectAllMaterial").checked = false;
    }

    // 자재 추가 모달 열기
    function openMaterialAddModal() {
      const modal = new bootstrap.Modal(
        document.getElementById("materialAddModal")
      );
      // BOM 코드 자동 생성 (BOM001-0X)
      let nextIdx = bomDetail.materials.length + 1;
      let bomCode =
        bomDetail.bomCode + "-" + String(nextIdx).padStart(2, "0");
      document.getElementById("modalBomCode").value = bomCode;
      document.getElementById("materialAddForm").reset();
      modal.show();
    }

    // 자재 추가 폼 제출
    document
      .getElementById("materialAddForm")
      .addEventListener("submit", function (e) {
        e.preventDefault();
        const form = e.target;
        const newMat = {
          bomCode: form.bomCode.value,
          materialCode: form.materialCode.value.trim(),
          materialName: form.materialName.value.trim(),
          spec: form.spec.value.trim(),
          unit: form.unit.value,
          price: Number(form.price.value),
          qty: Number(form.qty.value),
        };
        if (!newMat.materialCode || !newMat.materialName || !newMat.spec) {
          alert("필수 항목을 입력하세요.");
          return;
        }
        bomDetail.materials.push(newMat);
        renderMaterialTable();
        bootstrap.Modal.getInstance(
          document.getElementById("materialAddModal")
        ).hide();
      });

    // 자재 선택 삭제
    document
      .querySelector(".btn-delete-material")
      .addEventListener("click", function () {
        const checked = Array.from(
          document.querySelectorAll(
            '#materialTable tbody input[type="checkbox"]:checked'
          )
        );
        if (checked.length === 0) {
          alert("삭제할 자재를 선택하세요.");
          return;
        }
        if (!confirm("정말 삭제하시겠습니까?")) return;
        // 인덱스 내림차순으로 삭제
        const idxs = checked
          .map((cb) => Number(cb.dataset.idx))
          .sort((a, b) => b - a);
        for (let idx of idxs) {
          bomDetail.materials.splice(idx, 1);
        }
        renderMaterialTable();
      });

    // 자재 전체 선택
    document
      .getElementById("selectAllMaterial")
      .addEventListener("change", function (e) {
        const checkboxes = document.querySelectorAll(
          '#materialTable tbody input[type="checkbox"]'
        );
        checkboxes.forEach((cb) => (cb.checked = e.target.checked));
      });

    // 자재 추가 버튼
    document
      .querySelector(".btn-add-material")
      .addEventListener("click", openMaterialAddModal);

    // 하단 버튼 이벤트 (샘플)
    document.getElementById("btnList").addEventListener("click", function () {
      alert("목록 페이지로 이동");
    });
	
	document.getElementById("btnEdit").addEventListener("click", function () {
	  alert("수정 페이지로 이동");

	  // 🔥 여기서 bomCode 가져오기
	  const bomCode = document.getElementById("bomCode").value;

	  if (bomCode) {
	    location.href = "/bom/bom_edit?bomCode=" + bomCode;
	  } else {
	    alert("BOM 코드가 없습니다.");
	  }
	});
	
	
	document.addEventListener("DOMContentLoaded", function () {
	  const editBtn = document.getElementById("btnEdit");

	  if (editBtn) {
	    editBtn.addEventListener("click", function () {
	      const bomCode = document.getElementById("bomCode").value;
	      if (bomCode) {
	        location.href = "/bom/bom_edit?bomCode=" + bomCode;
	      } else {
	        alert("BOM 코드가 없습니다.");
	      }
	    });
	  }
	});
	/*
	document.getElementById("btnEdit").addEventListener("click", function () {
	  const bomCode = document.getElementById("bomCode").value;
	  if (bomCode) {
	       location.href = "/bom/bom_detail?bomCode=" + bomCode;
	  } else {
	    alert("BOM 코드가 없습니다.");
	  }
	});
*/	
/*
    document.getElementById("btnEdit").addEventListener("click", function () {
      alert("수정 페이지로 이동");
    });
*/
    document
      .getElementById("btnDelete")
      .addEventListener("click", function () {
        if (confirm("정말 삭제하시겠습니까?")) {
          alert("삭제되었습니다.");
        }
      });

    // 초기 렌더링
    document.addEventListener("DOMContentLoaded", () => {
      renderBomDetail();
      renderMaterialTable();
    });