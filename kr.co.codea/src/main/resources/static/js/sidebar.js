// Sidebar toggle 스크립트
const sidebar = document.getElementById("sidebar");
const sidebarToggle = document.getElementById("sidebarToggle");
const sidebarClose = document.getElementById("sidebarClose");

if (sidebarToggle && sidebar && sidebarClose) {
    sidebarToggle.addEventListener("click", () => sidebar.classList.add("show"));
    sidebarClose.addEventListener("click", () => sidebar.classList.remove("show"));
    document.addEventListener("click", (e) => {
        if (
            sidebar &&
            sidebar.classList.contains("show") &&
            !sidebar.contains(e.target) &&
            sidebarToggle &&
            !sidebarToggle.contains(e.target)
        ) {
            sidebar.classList.remove("show");
        }
    });
} else {
    console.warn("Sidebar-related elements not found. Sidebar functionality might be affected.");
}

// 로그아웃 버튼 이벤트 처리 (Thymeleaf로 버튼이 생성되었을 때만 이벤트 바인딩)
document.addEventListener('DOMContentLoaded', function() {
    const logoutButton = document.getElementById('logoutButton'); // Thymeleaf가 생성한 버튼 ID

    if (logoutButton) {
        logoutButton.addEventListener('click', async function() {
            try {
                const response = await fetch('/auth/logout', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                        // CSRF 토큰이 필요하다면 여기에 추가
                        // 'X-CSRF-TOKEN': '서버로부터 받은 CSRF 토큰 값'
                    }
                });

                if (response.ok) {
                    localStorage.removeItem('refreshToken'); // Refresh Token은 localStorage에서 관리한다고 가정
                    // Access Token은 HttpOnly 쿠키이므로 서버에서 만료시킴
                    alert('로그아웃 되었습니다.');
                    window.location.href = '/login';
                } else {
                    const errorText = await response.text();
                    alert(`로그아웃에 실패했습니다: ${response.status} ${errorText || ''}`);
                    localStorage.removeItem('refreshToken'); // 실패 시에도 로컬 정보 정리
                    window.location.href = '/login';
                }
            } catch (error) {
                console.error('로그아웃 요청 중 오류 발생:', error);
                alert('로그아웃 처리 중 오류가 발생했습니다.');
                localStorage.removeItem('refreshToken');
                window.location.href = '/login';
            }
        });
    }
});