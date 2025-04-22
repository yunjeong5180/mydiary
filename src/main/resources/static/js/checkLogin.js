// ✅ [로그인 상태 확인용 스크립트]
//
// - 현재 사용자가 로그인 중인지 서버에 물어봄
// - 로그인 안 되어 있으면 로그인 페이지로 자동 이동

fetch('/users/check-login')  // 👈 서버에 로그인 상태 요청
    .then(response =>
    {
        if (!response.ok)
        {
            // ❌ 로그인 상태 아님 → 로그인 페이지로 이동
            window.location.href = '/login.html';
        }
    })
    .catch(() =>
    {
        // ⚠️ 네트워크 오류 등 → 로그인 페이지로 이동
        window.location.href = '/login.html';
    });
