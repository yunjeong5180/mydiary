// # index.html 전용 스크립트
import { checkLoginAndHandleUI, logout } from '/js/auth.js';

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
    const isLoggedIn = await checkLoginAndHandleUI();
    if (isLoggedIn) {
      window.location.href = path;
    } else {
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
      const path = pendingRedirect.replace(/^\//, '');
      window.location.href = `/login.html?redirect=${encodeURIComponent(path)}`;
    };
  }

  if (signupBtn) {
    signupBtn.onclick = () => {
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
    logoutBtn.onclick = logout;
  }

  // ✅ 페이지 진입 시 로그인 상태 확인
  checkLoginAndHandleUI();
});
