// 로그인 상태 확인용 JS (세션 기반)
fetch('/users/check-login')
    .then(response => {
        if (!response.ok) {
            // 로그인 안 되어 있으면 로그인 페이지로 이동
            window.location.href = '/login.html';
        }
    })
    .catch(() => {
        // 에러가 나도 로그인 페이지로 이동
        window.location.href = '/login.html';
    });
