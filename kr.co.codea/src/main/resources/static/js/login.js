// 로그인 폼 제출 이벤트
  document
    .getElementById("loginForm")
    .addEventListener("submit", async function (event) {
      event.preventDefault(); // 폼의 기본 제출 동작 방지

      const loginId = document.getElementById("loginIdInput").value; // 변경된 ID 사용
      const password = document.getElementById("passwordInput").value; // 변경된 ID 사용
      const loginMessageDiv = document.getElementById("loginMessage");
      loginMessageDiv.textContent = ''; // 이전 메시지 초기화
      loginMessageDiv.className = 'message text-center'; // CSS 클래스 초기화

      // 서버로 보낼 데이터 객체 생성 (LoginRequestDto와 필드명 일치)
      const loginData = {
        loginId: loginId, // 서버 DTO의 필드명과 일치해야 함
        password: password
      };

      try {
        // 서버의 /auth/login API로 POST 요청
        const response = await fetch('/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(loginData) // 데이터를 JSON 문자열로 변환
        });

        if (response.ok) { // HTTP 상태 코드가 200-299 범위일 때
          const tokenDto = await response.json(); // 응답 바디를 JSON으로 파싱

          // 토큰 저장 (보안상 localStorage보다는 HttpOnly 쿠키 + CSRF 방어 또는 적절한 상태관리 라이브러리 사용 권장)
          // 여기서는 간단히 localStorage 사용 예시
          localStorage.setItem('accessToken', tokenDto.accessToken);
          localStorage.setItem('refreshToken', tokenDto.refreshToken);

          loginMessageDiv.textContent = '로그인 성공! 잠시 후 메인 페이지로 이동합니다.';
          loginMessageDiv.classList.add('success');

          // 로그인 성공 후 이동할 페이지 (예: /index 또는 /home)
          // '/index'가 HomeController에 매핑된 경로라고 가정
          setTimeout(() => {
            window.location.href = '/index';
          }, 1000); // 1초 후 이동

        } else {
          // 로그인 실패 처리 (서버에서 오류 메시지를 JSON으로 보낸다고 가정)
          let errorMessage = '로그인에 실패했습니다.';
          try {
              const errorData = await response.json();
              if (errorData && errorData.message) {
                  errorMessage = errorData.message;
              } else {
                  errorMessage = `오류 상태: ${response.status} (${response.statusText})`;
              }
          } catch (e) {
              // 응답이 JSON이 아니거나 파싱 실패 시
              errorMessage = `오류 상태: ${response.status} (${response.statusText})`;
          }
          loginMessageDiv.textContent = errorMessage;
          loginMessageDiv.classList.add('error');
        }
      } catch (error) {
        // 네트워크 오류 등 fetch 자체의 실패
        console.error('로그인 요청 중 오류 발생:', error);
        loginMessageDiv.textContent = '로그인 요청 중 오류가 발생했습니다. 네트워크 연결을 확인해주세요.';
        loginMessageDiv.classList.add('error');
      }
    });

  document
    .getElementById("findIdBtn")
    .addEventListener("click", function () {
      alert("아이디 찾기 기능은 준비 중입니다.");
    });

  document
    .getElementById("findPwBtn")
    .addEventListener("click", function () {
      alert("비밀번호 찾기 기능은 준비 중입니다.");
    });