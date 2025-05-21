

  // 법인/개인 선택에 따라 법인등록번호 활성화
  document.addEventListener("DOMContentLoaded", function () {
    const corporateRadio = document.getElementById("corporate");
    const individualRadio = document.getElementById("individual");
    const corporateNumber = document.getElementById("corporateNumber");

    function toggleCorporateNumber() {
      if (corporateRadio.checked) {
        corporateNumber.disabled = false;
        corporateNumber.required = true;
      } else {
        corporateNumber.disabled = true;
        corporateNumber.required = false;
        corporateNumber.value = "";
      }
    }

    corporateRadio.addEventListener("change", toggleCorporateNumber);
    individualRadio.addEventListener("change", toggleCorporateNumber);
  });

  // 주소 검색
  function execDaumPostcode() {
    new daum.Postcode({
      oncomplete: function (data) {
        document.getElementById("zipcode").value = data.zonecode;
        document.getElementById("address").value =
          data.roadAddress || data.jibunAddress;
      },
    }).open();
  }