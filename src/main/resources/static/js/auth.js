// # 로그인 상태 확인, 로그아웃 등 공통 인증 관련
/**
 * ✅ 로그인 상태 확인 및 UI 처리
 * - 세션 기반 인증 확인
 * - 로그인된 경우: 로그아웃 버튼 표시
 * - 로그인 안 된 경우: 숨김 처리
 * - 콜백 함수가 주어지면 로그인된 경우 실행
 */
export async function checkLoginAndHandleUI(onLoggedIn) {
  try {
    const res = await fetch(`${location.origin}/users/me`, {
      method: 'GET',
      credentials: 'include'
    });

    const isLoggedIn = res.ok;
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.style.display = isLoggedIn ? 'inline-block' : 'none';
    }

    if (isLoggedIn && typeof onLoggedIn === 'function') {
      onLoggedIn(); // 로그인된 경우 콜백 실행
    }

    return isLoggedIn;
  } catch (err) {
    console.error("로그인 상태 확인 중 오류:", err);
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) logoutBtn.style.display = 'none';
    return false;
  }
}

/**
 * ✅ 로그아웃 처리 함수
 * - 세션 만료 및 페이지 이동 처리 포함
 */
export async function logout() {
  try {
    await fetch(`${location.origin}/users/logout`, {
      method: 'POST',
      credentials: 'include'
    });
  } catch (err) {
    console.error("로그아웃 요청 실패:", err);
  }
  location.href = '/login.html';
}
