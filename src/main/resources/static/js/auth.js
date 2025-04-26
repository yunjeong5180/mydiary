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
  // 단순 로그인 페이지로 이동 (로그아웃이므로 리다이렉트 정보 없이 이동)
  location.href = '/login.html';
}

/**
 * ✅ 로그인 필요 시 로그인 페이지로 리다이렉트
 * - 현재 페이지 정보를 유지하여 로그인 후 원래 페이지로 돌아오도록 함
 */
export function redirectToLogin() {
  const currentPath = location.pathname;
  // 이미 로그인 페이지면 리다이렉트 하지 않음
  if (currentPath === '/login.html') return;

  // 현재 페이지 정보를 redirect 파라미터로 전달
  location.href = `/login.html?redirect=${encodeURIComponent(currentPath.replace(/^\//, ''))}`;
}

/**
 * ✅ 인증 체크 함수 (지연 실행 포함)
 * - 쿠키가 완전히 설정될 수 있도록 약간의 지연 후 인증 확인
 * - 인증 실패 시 로그인 페이지로 리다이렉트
 */
export function checkAuthWithDelay(delay = 100) {
  return new Promise(resolve => {
    setTimeout(async () => {
      try {
        // 인증 상태 확인 (쿠키 포함)
        const res = await fetch('http://localhost:8081/users/me', {
          method: 'GET',
          credentials: 'include'
        });

        const isLoggedIn = res.ok;

        if (!isLoggedIn) {
          // 로그인 페이지로 리다이렉트
          redirectToLogin();
        }

        resolve(isLoggedIn);
      } catch (err) {
        console.error("인증 체크 오류:", err);
        // 오류 시 로그인 페이지로 리다이렉트
        redirectToLogin();
        resolve(false);
      }
    }, delay);
  });
}

// 로그인 상태를 로컬 스토리지에 저장/확인하는 도우미 함수
export function setLoginState(isLoggedIn) {
  if (isLoggedIn) {
    localStorage.setItem('isLoggedIn', 'true');
  } else {
    localStorage.removeItem('isLoggedIn');
  }
}

export function getLoginState() {
  return localStorage.getItem('isLoggedIn') === 'true';
}