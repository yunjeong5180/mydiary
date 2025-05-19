// # index.html 전용 스크립트
import { checkLoginAndHandleUI, logout, checkAuthWithDelay, getLoginState, setLoginState } from '/js/auth.js';

document.addEventListener('DOMContentLoaded', () => {
  // ✅ DOM 요소 가져오기
  const writeBtn = document.getElementById('writeBtn');
  const listBtn = document.getElementById('listBtn');
  const loginModal = document.getElementById('loginModal');
  const loginBtn = document.getElementById('loginBtn');
  const signupBtn = document.getElementById('signupBtn');
  const cancelBtn = document.getElementById('cancelBtn');
  const logoutBtn = document.getElementById('logoutBtn');

  // ✅ 리다이렉트 경로 저장용 변수
  let pendingRedirect = '';

  // ✅ 로그인 확인 후 페이지 이동 or 모달
  async function checkAndNavigate(path) {
    // 먼저 로컬 스토리지의 로그인 상태를 확인
    const localLoginState = getLoginState();
    console.log("💡 로컬 스토리지 로그인 상태:", localLoginState);

    // 서버에 로그인 상태 확인 요청
    const isLoggedIn = await checkLoginAndHandleUI();
    console.log("💡 서버 확인 로그인 상태:", isLoggedIn);

    // 로그인 상태 업데이트
    setLoginState(isLoggedIn);

    if (isLoggedIn) {
      console.log("✅ 인증됨, 직접 이동:", path);
      window.location.href = path;
    } else {
      console.log("⚠️ 미인증, 모달 표시 후 저장된 경로:", path);
      pendingRedirect = path;
      loginModal.style.display = 'block';
    }
  }

  // ✅ 버튼 클릭 이벤트 연결
  if (writeBtn) writeBtn.onclick = () => checkAndNavigate('/write.html');
  if (listBtn) listBtn.onclick = () => checkAndNavigate('/list.html');

  // ✅ 모달 버튼 이벤트
  if (loginBtn) {
    loginBtn.onclick = () => {
      // 앞에 슬래시 제거하고 경로 정규화
      const path = pendingRedirect.replace(/^\//, '');
      console.log("🔁 로그인 모달 클릭 시 redirect 경로:", path);
      window.location.href = `/login.html?redirect=${encodeURIComponent(path)}`;
    };
  }

  if (signupBtn) {
    signupBtn.onclick = () => {
      // 앞에 슬래시 제거하고 경로 정규화
      const path = pendingRedirect.replace(/^\//, '');
      window.location.href = `/signup.html?redirect=${encodeURIComponent(path)}`;
    };
  }

  if (cancelBtn) {
    cancelBtn.onclick = () => {
      loginModal.style.display = 'none';
      pendingRedirect = '';
    };
  }

  if (loginModal) {
    loginModal.onclick = (e) => {
      if (e.target === loginModal) loginModal.style.display = 'none';
    };
  }

  const modalContent = document.querySelector('.modal-content');
  if (modalContent) {
    modalContent.onclick = (e) => e.stopPropagation();
  }

  // ✅ 로그아웃 버튼 이벤트
  if (logoutBtn) {
    logoutBtn.onclick = () => {
      // 로그아웃 처리 전 로컬 스토리지 상태 제거
      localStorage.removeItem('isLoggedIn');
      logout();
    };
  }

  // ✅ 페이지 진입 시 로그인 상태 확인 (지연 적용)
  setTimeout(() => {
    checkLoginAndHandleUI();
  }, 100);
  if (logoutBtn) {
      logoutBtn.onclick = () => {
        localStorage.removeItem('isLoggedIn'); // 로그아웃 시 로컬 스토리지 상태 제거
        logout(); // auth.js의 logout 함수 호출
      };
    }
});