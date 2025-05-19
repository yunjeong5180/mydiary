// # 로그인 상태 확인, 로그아웃 등 공통 인증 관련
/**
 * ✅ 로그인 상태 확인 및 UI 처리
 * - 세션 기반 인증 확인
 * - 로그인된 경우: 로그아웃 버튼 표시
 * - 로그인 안 된 경우: 숨김 처리
 * - 콜백 함수가 주어지면 로그인된 경우 실행
 */
export async function checkLoginAndHandleUI(onLoggedIn) {
  try
  {
    const res = await fetch(`${location.origin}/api/users/me`,
    {
//        Workspace 함수의 URL 부분에 users/me 앞에 /api를 추가하여 /api/users/me로 변경했습니다.
//        location.origin은 현재 페이지의 주소 (예: http://localhost:8080)를 나타내므로,
//        최종 경로는 http://localhost:8080/api/users/me가 됩니다.
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
    await fetch(`${location.origin}/api/users/logout`, {
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
        // 수정 전: const res = await fetch('http://localhost:8081/users/me', { ... });
        const res = await fetch(`${location.origin}/api/users/me`,
        {
//            기존의 http://localhost:8081/users/me는 포트번호도 다르고 /api도 빠져있었습니다.
//            올바른 API 경로인 /api/users/me로 수정했습니다.
//            location.origin을 사용하거나 간단히 상대 경로로 지정하는 것이 좋습니다.

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