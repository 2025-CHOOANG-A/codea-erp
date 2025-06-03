// client_list.js - 완전한 버전 (모든 문제 해결)

document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    cleanupModalEvents(); // 초기 모달 정리
    
    // 전역 모달 이벤트 리스너
    document.addEventListener('hidden.bs.modal', function(e) {
        setTimeout(() => {
            const openModals = document.querySelectorAll('.modal.show');
            if (openModals.length === 0) {
                cleanupModalEvents();
            }
        }, 100);
    });
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // 라디오 버튼 선택 시 버튼 활성화
    const radioButtons = document.querySelectorAll('input[name="selectedClient"]');
    const editButton = document.getElementById('editClientButton');
    const deleteButton = document.getElementById('deleteClientButton');
    const addContactButton = document.getElementById('addContactButton');
    
    initializeBizCondSearch();

    radioButtons.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                if (editButton) editButton.disabled = false;
                if (deleteButton) deleteButton.disabled = false;
                if (addContactButton) addContactButton.disabled = false;
            }
        });
    });

    // 수정 버튼 클릭 이벤트
    if (editButton) {
        editButton.addEventListener('click', function() {
            const selectedRadio = document.querySelector('input[name="selectedClient"]:checked');
            if (selectedRadio) {
                editSelectedClient(selectedRadio);
            } else {
                alert('수정할 거래처를 선택해주세요.');
            }
        });
    }

    // 삭제 버튼 클릭 이벤트
    if (deleteButton) {
        deleteButton.addEventListener('click', function() {
            const selectedRadio = document.querySelector('input[name="selectedClient"]:checked');
            if (selectedRadio) {
                deleteSelectedClient(selectedRadio);
            } else {
                alert('삭제할 거래처를 선택해주세요.');
            }
        });
    }

    // 담당자 추가 버튼 클릭 이벤트
    if (addContactButton) {
        addContactButton.addEventListener('click', function() {
            const selectedRadio = document.querySelector('input[name="selectedClient"]:checked');
            if (selectedRadio) {
                const bpId = selectedRadio.getAttribute('data-bp-id');
                openContactModal('add', bpId);
            } else {
                alert('담당자를 추가할 거래처를 선택해주세요.');
            }
        });
    }

    // 상세 버튼 클릭 이벤트
    document.addEventListener('click', function(e) {
        if (e.target.closest('[data-bs-target="#clientDetailModal"]')) {
            const button = e.target.closest('[data-bs-target="#clientDetailModal"]');
            const bpId = button.getAttribute('data-bp-id');
            loadClientDetail(bpId);
        }
    });

    // 비고 버튼 클릭 이벤트
    document.addEventListener('click', function(e) {
        if (e.target.closest('.remark-btn')) {
            const button = e.target.closest('.remark-btn');
            const remark = button.getAttribute('data-remark');
            showRemarkModal(remark);
        }
    });

    // 검색 폼 제출 이벤트
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        filterForm.addEventListener('submit', function(e) {
            // 기본 폼 제출 동작 허용 (페이지 리로드)
        });
    }

    // 거래처 등록 모달 저장 버튼
    const modalSubmitButton = document.getElementById('modalSubmitButton');
    if (modalSubmitButton) {
        modalSubmitButton.addEventListener('click', saveClient);
    }

    // 담당자 모달 저장 버튼
    const contactSubmitButton = document.getElementById('contactSubmitButton');
    if (contactSubmitButton) {
        contactSubmitButton.addEventListener('click', saveContact);
    }

    // 거래처 등록 모달 열기 이벤트
    const addClientBtn = document.querySelector('[data-bs-target="#clientRegisterModal"]');
    if (addClientBtn) {
        addClientBtn.addEventListener('click', function() {
            openClientModal('add');
        });
    }

    // 담당자 추가 버튼 (상세 모달 내)
    const addContactInDetailBtn = document.getElementById('addContactInDetailBtn');
    if (addContactInDetailBtn) {
        addContactInDetailBtn.addEventListener('click', function() {
            const modalElement = document.getElementById('clientDetailModal');
            const bpId = modalElement.getAttribute('data-current-bp-id');
            if (bpId) {
                openContactModal('add', bpId);
            }
        });
    }
}

// 모달 이벤트 정리 함수
function cleanupModalEvents() {
    const backdrops = document.querySelectorAll('.modal-backdrop');
    backdrops.forEach(backdrop => backdrop.remove());
    
    document.body.classList.remove('modal-open');
    document.body.style.removeProperty('padding-right');
    document.body.style.removeProperty('overflow');
}

// 선택된 거래처 수정
function editSelectedClient(selectedRadio) {
    const clientData = {
        bpId: selectedRadio.getAttribute('data-bp-id'),
        bpCode: selectedRadio.getAttribute('data-bp-code'),
        bpName: selectedRadio.getAttribute('data-bp-name'),
        bpType: selectedRadio.getAttribute('data-bp-type'),
        ceoName: selectedRadio.getAttribute('data-ceo-name'),
        bizNo: selectedRadio.getAttribute('data-biz-no'),
        bizCond: selectedRadio.getAttribute('data-biz-cond'),
        bizCondCode: selectedRadio.getAttribute('data-biz-cond-code'),
        bizType: selectedRadio.getAttribute('data-biz-type'),
        bp_tel: selectedRadio.getAttribute('data-bp-tel'),
        fax: selectedRadio.getAttribute('data-fax'),
        postCode: selectedRadio.getAttribute('data-post-code'),
        address: selectedRadio.getAttribute('data-address'),
        addressDetail: selectedRadio.getAttribute('data-address-detail'),
        bp_remark: selectedRadio.getAttribute('data-bp-remark')
    };
    
    openClientModal('edit', clientData);
}

// 선택된 거래처 삭제
function deleteSelectedClient(selectedRadio) {
    const bpId = selectedRadio.getAttribute('data-bp-id');
    const bpName = selectedRadio.getAttribute('data-bp-name');
    
    if (!confirm(`정말 "${bpName}" 거래처를 삭제하시겠습니까?\n관련된 담당자 정보도 함께 삭제됩니다.`)) {
        return;
    }

    fetch(`/api/client/${bpId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('거래처 삭제에 실패했습니다.');
        }
        return response.json();
    })
    .then(data => {
        alert(data.message || '거래처가 삭제되었습니다.');
        location.reload();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('거래처 삭제 중 오류가 발생했습니다.');
    });
}

// 거래처 모달 열기 - backdrop 문제 해결
function openClientModal(mode, clientData = null) {
    const modal = document.getElementById('clientRegisterModal');
    const title = document.getElementById('clientRegisterModalLabel');
    const submitBtn = document.getElementById('modalSubmitButton');
    
    if (mode === 'add') {
        title.textContent = '거래처 등록';
        submitBtn.textContent = '등록';
        clearClientForm();
    } else if (mode === 'edit') {
        title.textContent = '거래처 수정';
        submitBtn.textContent = '수정';
        fillClientForm(clientData);
    }
    
    const modalInstance = new bootstrap.Modal(modal, {
        backdrop: true,
        keyboard: true
    });
    modalInstance.show();
    
    // 모달 닫힐 때 backdrop 정리
    modal.addEventListener('hidden.bs.modal', function() {
        cleanupModalEvents();
    }, { once: true });
}

// 거래처 폼 초기화
function clearClientForm() {
    const form = document.getElementById('clientForm');
    if (form) {
        form.reset();
        
        // 숨겨진 필드들 초기화
        const bpIdField = document.getElementById('modalBpId');
        if (bpIdField) bpIdField.value = '';
        
        // 업태 관련 필드들 초기화
        const bizCondInput = document.getElementById('modalBizCondInput');
        const bizCond = document.getElementById('modalBizCond');
        const bizCondCode = document.getElementById('modalBizCondCode');
        
        if (bizCondInput) bizCondInput.value = '';
        if (bizCond) bizCond.value = '';
        if (bizCondCode) bizCondCode.value = '';
        
        hideBizCondDropdown();
    }
}

// 거래처 폼 채우기
function fillClientForm(clientData) {
    if (!clientData) return;
    
    const fields = {
        'modalBpId': clientData.bpId,
        'modalBpCode': clientData.bpCode,
        'modalBpName': clientData.bpName,
        'modalBpType': clientData.bpType,
        'modalCeoName': clientData.ceoName,
        'modalBizNo': clientData.bizNo,
        'modalBizType': clientData.bizType,
        'modalBpTel': clientData.bp_tel,
        'modalFax': clientData.fax,
        'modalPostCode': clientData.postCode,
        'modalAddress': clientData.address,
        'modalAddressDetail': clientData.addressDetail,
        'modalBpRemark': clientData.bp_remark
    };
    
    Object.entries(fields).forEach(([fieldId, value]) => {
        const field = document.getElementById(fieldId);
        if (field) {
            field.value = value || '';
        }
    });
    
    // 업태 관련 필드들 특별 처리
    const bizCondInput = document.getElementById('modalBizCondInput');
    const bizCond = document.getElementById('modalBizCond');
    const bizCondCode = document.getElementById('modalBizCondCode');
    
    if (bizCondInput && bizCond && bizCondCode) {
        // 수정할 때는 한글명(bizCondCode)을 표시하고, 숫자ID(bizCond)도 설정
        bizCondInput.value = clientData.bizCondCode || '';
        bizCond.value = clientData.bizCond || '';
        bizCondCode.value = clientData.bizCondCode || '';
    }
}

// 비고 모달 표시
function showRemarkModal(remark) {
    const content = document.getElementById('modalRemarkContent');
    if (content) {
        content.textContent = remark || '비고가 없습니다.';
    }
}

// 거래처 상세 정보 로드
function loadClientDetail(bpId) {
    if (!bpId) {
        alert('거래처 ID가 없습니다.');
        return;
    }

    const modalElement = document.getElementById('clientDetailModal');
    if (modalElement) {
        modalElement.setAttribute('data-current-bp-id', bpId);
    }

    fetch(`/api/client/${bpId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('거래처 정보를 불러오는데 실패했습니다.');
            }
            return response.json();
        })
        .then(client => {
            displayClientDetail(client);
            loadContacts(bpId);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('거래처 상세 정보를 불러오는데 실패했습니다.');
        });
}

// 거래처 상세 정보 표시
function displayClientDetail(client) {
    const fields = {
        'detailBpCode': client.bpCode,
        'detailBpName': client.bpName,
        'detailBpType': client.bpType,
        'detailCeoName': client.ceoName,
        'detailBizNo': client.bizNo,
        'detailBizCond': client.bizCondCode || client.bizCond,
        'detailBizType': client.bizType,
        'detailBpTel': client.bp_tel,
        'detailFax': client.fax,
        'detailFullAddress': client.fullAddress,
        'detailBpRemark': client.bp_remark
    };

    Object.entries(fields).forEach(([id, value]) => {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value || '-';
        }
    });
}

// 담당자 목록 로드
function loadContacts(bpId) {
    fetch(`/api/client/${bpId}/contacts`)
        .then(response => {
            if (!response.ok) {
                throw new Error('담당자 목록을 불러오는데 실패했습니다.');
            }
            return response.json();
        })
        .then(contacts => {
            displayContacts(contacts, bpId);
        })
        .catch(error => {
            console.error('Error:', error);
            displayContactsError();
        });
}

// 담당자 목록 표시
function displayContacts(contacts, bpId) {
    const tbody = document.querySelector('#contactTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (contacts.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center py-5 text-muted border-light">
                    <div class="py-3">
                        <i class="bi bi-person-x fs-2 text-muted opacity-50"></i>
                        <div class="mt-2 small">등록된 담당자가 없습니다.</div>
                    </div>
                </td>
            </tr>
        `;
    } else {
        contacts.forEach(contact => {
            const row = createContactRow(contact, bpId);
            tbody.appendChild(row);
        });
    }
}

// 담당자 행 생성 - 필드명 수정
function createContactRow(contact, bpId) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td class="py-3 border-light">${contact.bcName || '-'}</td>
        <td class="py-3 border-light">${contact.bcPosition || '-'}</td>
        <td class="py-3 border-light">${contact.bc_tel || '-'}</td>
        <td class="py-3 border-light">${contact.hp || '-'}</td>
        <td class="py-3 border-light">${contact.email || '-'}</td>
        <td class="py-3 border-light">${contact.bc_remark || '-'}</td>
        <td class="py-3 border-light text-center">
            <div class="btn-group" role="group">
                <button type="button" class="btn btn-sm btn-outline-primary px-3" 
                        onclick="editContact(${contact.bcId}, ${bpId})">
                    수정
                </button>
                <button type="button" class="btn btn-sm btn-outline-danger px-3" 
                        onclick="deleteContact(${contact.bcId}, ${bpId})">
                    삭제
                </button>
            </div>
        </td>
    `;
    return tr;
}

// 담당자 목록 에러 표시
function displayContactsError() {
    const tbody = document.querySelector('#contactTable tbody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-danger py-4">
                    담당자 목록을 불러오는데 실패했습니다.
                </td>
            </tr>
        `;
    }
}

// 담당자 모달 열기 - 포커스 문제 및 중첩 모달 해결
function openContactModal(mode, bpId, contactData = null) {
    const clientDetailModal = document.getElementById('clientDetailModal');
    const clientDetailModalInstance = bootstrap.Modal.getInstance(clientDetailModal);
    
    const modal = document.getElementById('contactModal');
    const title = document.getElementById('contactModalLabel');
    const submitBtn = document.getElementById('contactSubmitButton');
    
    if (mode === 'add') {
        title.textContent = '담당자 등록';
        submitBtn.textContent = '등록';
        clearContactForm();
        document.getElementById('contactBpId').value = bpId;
    } else if (mode === 'edit') {
        title.textContent = '담당자 수정';
        submitBtn.textContent = '수정';
        fillContactForm(contactData);
    }
    
    // 기존 상세 모달이 열려있다면 잠시 숨기기
    let wasDetailModalOpen = false;
    if (clientDetailModalInstance && clientDetailModal.classList.contains('show')) {
        wasDetailModalOpen = true;
        clientDetailModalInstance.hide();
    }
    
    // 담당자 모달 열기
    setTimeout(() => {
        const modalInstance = new bootstrap.Modal(modal, {
            backdrop: 'static',
            keyboard: false
        });
        modalInstance.show();
        
        // 모달이 완전히 열린 후 첫 번째 입력 필드에 포커스
        modal.addEventListener('shown.bs.modal', function() {
            setTimeout(() => {
                const firstInput = document.getElementById('contactBcName');
                if (firstInput) {
                    firstInput.focus();
                }
            }, 100);
        }, { once: true });
        
        // 담당자 모달이 닫힐 때 상세 모달 다시 열기
        if (wasDetailModalOpen) {
            modal.addEventListener('hidden.bs.modal', function() {
                setTimeout(() => {
                    const detailModalInstance = new bootstrap.Modal(clientDetailModal);
                    detailModalInstance.show();
                }, 100);
            }, { once: true });
        }
    }, 200);
}

// 담당자 폼 초기화 - 안전한 방법
function clearContactForm() {
    const form = document.getElementById('contactForm');
    if (form) {
        form.reset();
    }
}

// 담당자 폼 채우기 - 필드명 수정
function fillContactForm(contactData) {
    if (!contactData) return;
    
    const fields = {
        'contactBpId': contactData.bpId,
        'contactBcId': contactData.bcId,
        'contactBcName': contactData.bcName,
        'contactBcPosition': contactData.bcPosition,
        'contactTel': contactData.bc_tel,        // bc_tel로 수정
        'contactHp': contactData.hp,
        'contactEmail': contactData.email,
        'contactRemark': contactData.bc_remark   // bc_remark로 수정
    };
    
    Object.entries(fields).forEach(([fieldId, value]) => {
        const field = document.getElementById(fieldId);
        if (field) {
            field.value = value || '';
        }
    });
}

// 거래처 저장
function saveClient() {
    const form = document.getElementById('clientForm');
    if (!form) return;

    if (!validateClientForm(form)) {
        return;
    }

    const formData = new FormData(form);
    const clientData = Object.fromEntries(formData.entries());

    const bpId = document.getElementById('modalBpId')?.value;
    const url = bpId ? `/api/client/${bpId}` : '/api/client';
    const method = bpId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(clientData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('저장에 실패했습니다.');
        }
        return response.json();
    })
    .then(data => {
        alert(data.message || '저장되었습니다.');
        const modalInstance = bootstrap.Modal.getInstance(document.getElementById('clientRegisterModal'));
        modalInstance.hide();
        location.reload();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('저장 중 오류가 발생했습니다.');
    });
}

// 담당자 저장 - 더 자세한 디버깅
function saveContact() {
    const form = document.getElementById('contactForm');
    if (!form) return;

    if (!validateContactForm(form)) {
        return;
    }

    const formData = new FormData(form);
    const contactData = Object.fromEntries(formData.entries());

    const bpId = contactData.bpId;
    const bcId = contactData.bcId;
    
    const url = bcId ? 
        `/api/client/${bpId}/contact/${bcId}` : 
        `/api/client/${bpId}/contact`;
    const method = bcId ? 'PATCH' : 'POST';

    console.log('=== 담당자 저장 디버깅 ===');
    console.log('URL:', url);
    console.log('Method:', method);
    console.log('요청 데이터:', contactData);
    console.log('JSON 문자열:', JSON.stringify(contactData));

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(contactData)
    })
    .then(response => {
        console.log('응답 상태:', response.status, response.statusText);
        console.log('응답 헤더:', response.headers.get('content-type'));
        
        // 500 에러인 경우 응답 본문도 확인
        if (response.status === 500) {
            return response.text().then(errorText => {
                console.log('500 에러 응답 본문:', errorText);
                throw new Error(`서버 내부 오류 (500): ${errorText.slice(0, 200)}`);
            });
        }
        
        if (!response.ok) {
            return response.text().then(errorText => {
                console.log('에러 응답 본문:', errorText);
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            });
        }
        
        // 응답이 JSON인지 확인
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return response.json();
        } else {
            return response.text().then(text => {
                console.log('응답 내용 (텍스트):', text);
                return { message: '저장되었습니다.' };
            });
        }
    })
    .then(data => {
        console.log('파싱된 응답 데이터:', data);
        alert(data.message || '저장되었습니다.');
        const modalInstance = bootstrap.Modal.getInstance(document.getElementById('contactModal'));
        modalInstance.hide();
        loadContacts(bpId);
    })
    .catch(error => {
        console.error('담당자 저장 에러:', error);
        alert('저장 중 오류가 발생했습니다: ' + error.message);
    });
}

// 담당자 수정
function editContact(bcId, bpId) {
    fetch(`/api/client/${bpId}/contact/${bcId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('담당자 정보를 불러오는데 실패했습니다.');
            }
            return response.json();
        })
        .then(contactData => {
            openContactModal('edit', bpId, contactData);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('담당자 정보를 불러오는데 실패했습니다.');
        });
}

// 담당자 삭제
function deleteContact(bcId, bpId) {
    if (!confirm('정말 이 담당자를 삭제하시겠습니까?')) {
        return;
    }

    fetch(`/api/client/${bpId}/contact/${bcId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('담당자 삭제에 실패했습니다.');
        }
        return response.json();
    })
    .then(data => {
        alert(data.message || '삭제되었습니다.');
        loadContacts(bpId);
    })
    .catch(error => {
        console.error('Error:', error);
        alert('담당자 삭제 중 오류가 발생했습니다.');
    });
}

// 폼 유효성 검사
function validateClientForm(form) {
    const required = ['modalBpType', 'modalBpName', 'modalBpTel'];
    
    for (const fieldId of required) {
        const field = form.querySelector(`#${fieldId}`);
        if (!field || !field.value.trim()) {
            field?.focus();
            alert('필수 항목을 입력해주세요.');
            return false;
        }
    }
    return true;
}

function validateContactForm(form) {
    const bcName = form.querySelector('#contactBcName');
    if (!bcName || !bcName.value.trim()) {
        bcName?.focus();
        alert('담당자명을 입력해주세요.');
        return false;
    }
    return true;
}

// 주소 검색 함수 (다음 우편번호 서비스)
function searchAddress() {
    new daum.Postcode({
        oncomplete: function(data) {
            document.getElementById('modalPostCode').value = data.zonecode;
            document.getElementById('modalAddress').value = data.address;
            document.getElementById('modalAddressDetail').focus();
        }
    }).open();
}

// 업태 검색 초기화
function initializeBizCondSearch() {
    const bizCondInput = document.getElementById('modalBizCondInput');
    const bizCondDropdown = document.getElementById('bizCondDropdown');
    
    if (!bizCondInput || !bizCondDropdown) return;

    let searchTimeout;

    bizCondInput.addEventListener('input', function() {
        const query = this.value.trim();
        
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            if (query.length >= 1) {
                searchBizCond(query);
            } else {
                hideBizCondDropdown();
            }
        }, 300);
    });

    bizCondInput.addEventListener('blur', function() {
        setTimeout(() => {
            hideBizCondDropdown();
        }, 200);
    });

    bizCondInput.addEventListener('focus', function() {
        if (bizCondDropdown.children.length > 0) {
            showBizCondDropdown();
        }
    });
}

// 업태 검색 API 호출
function searchBizCond(query) {
    fetch(`/api/common-codes/biz-cond?query=${encodeURIComponent(query)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('업태 검색에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            displayBizCondResults(data);
        })
        .catch(error => {
            console.error('Error:', error);
            hideBizCondDropdown();
        });
}

// 업태 검색 결과 표시
function displayBizCondResults(results) {
    const dropdown = document.getElementById('bizCondDropdown');
    
    if (!results || results.length === 0) {
        dropdown.innerHTML = '<div class="dropdown-item text-muted">검색 결과가 없습니다.</div>';
        showBizCondDropdown();
        return;
    }

    dropdown.innerHTML = '';
    
    results.forEach(item => {
        const dropdownItem = document.createElement('div');
        dropdownItem.className = 'dropdown-item cursor-pointer';
        dropdownItem.innerHTML = `
            <div class="d-flex justify-content-between">
                <span>${item.code}</span>
            </div>
        `;
        
        dropdownItem.addEventListener('click', function() {
            selectBizCond(item);
        });
        
        dropdown.appendChild(dropdownItem);
    });
    
    showBizCondDropdown();
}

// 업태 선택
function selectBizCond(item) {
    document.getElementById('modalBizCondInput').value = item.code;
    document.getElementById('modalBizCond').value = item.codeId;      // DB에 저장될 숫자값
    document.getElementById('modalBizCondCode').value = item.code;    // 표시용 한글명
    hideBizCondDropdown();
}

// 드롭다운 표시/숨기기
function showBizCondDropdown() {
    const dropdown = document.getElementById('bizCondDropdown');
    dropdown.style.display = 'block';
}

function hideBizCondDropdown() {
    const dropdown = document.getElementById('bizCondDropdown');
    dropdown.style.display = 'none';
}