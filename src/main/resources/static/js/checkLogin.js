// ✅ [개선된 로그인 상태 확인용 스크립트]
// 모든 보호된 페이지에 포함시킬 스크립트
import { redirectToLogin, checkAuthWithDelay, getLoginState } from '/js/auth.js';

document.addEventListener('DOMContentLoaded', () => {
  // 로컬 스토리지 상태 먼저 확인
  const localLoginState = getLoginState();
  console.log("💡 로컬 스토리지 로그인 상태:", localLoginState);

  // 지연 적용 인증 체크 (쿠키 설정 시간 확보)
  setTimeout(async () => {
    try {
      const res = await fetch('/api/users/me', {
        credentials: 'include'  // 쿠키 포함 설정
      });

      if (!res.ok) {
        console.log("⚠️ 인증 실패, 로그인 페이지로 리다이렉트");
        // 현재 페이지를 저장하여 로그인 후 돌아올 수 있도록 함
        redirectToLogin();
      } else {
        console.log("✅ 인증 성공, 정상 진행");
        localStorage.setItem('isLoggedIn', 'true');
      }
    } catch (error) {
      console.error("인증 확인 오류:", error);
      // 오류 발생 시에도 로그인 페이지로 리다이렉트
      redirectToLogin();
    }
  }, 100); // 100ms 지연
});