// # login.html 전용 스크립트

document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('loginForm');
  const msgElem = document.getElementById('msg');
  const signupLinkBtn = document.getElementById('signupLinkBtn');

  // 🔁 URL에서 redirect 파라미터 읽기
  const urlParams = new URLSearchParams(location.search);
  const redirectTo = urlParams.get("redirect");

  // 🔗 회원가입 버튼에도 redirect 정보 추가
  if (redirectTo) {
    try {
      if (redirectTo.startsWith('/') || redirectTo.endsWith('.html') || !redirectTo.includes(':')) {
        signupLinkBtn.onclick = () => {
          location.href = `/signup.html?redirect=${encodeURIComponent(redirectTo)}`;
        };
      } else {
        console.warn("Invalid redirect parameter for signup link:", redirectTo);
        signupLinkBtn.onclick = () => { location.href = '/signup.html'; };
      }
    } catch (e) {
      console.error("Redirect parse error:", e);
      signupLinkBtn.onclick = () => { location.href = '/signup.html'; };
    }
  } else {
    signupLinkBtn.onclick = () => { location.href = '/signup.html'; };
  }

  // ✅ 로그인 폼 제출 처리
  loginForm.onsubmit = async (e) => {
    e.preventDefault();
    msgElem.textContent = '';

    const formData = new FormData(e.target);
    const body = Object.fromEntries(formData.entries());

    try {
      const res = await fetch(`${location.origin}/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(body)
      });

      if (res.ok) {
        let finalRedirectUrl = '/index.html';
        if (redirectTo) {
          if ((redirectTo.startsWith('/') || redirectTo.endsWith('.html')) && !redirectTo.includes(':')) {
            finalRedirectUrl = '/' + redirectTo.replace(/^\//, '');
          } else {
            console.warn("Invalid redirect parameter ignored:", redirectTo);
          }
        }
        location.href = finalRedirectUrl;

      } else {
        let errorText = '아이디 또는 비밀번호가 잘못되었습니다.';
        try {
          if (res.headers.get('content-type')?.includes('application/json')) {
            const errorJson = await res.json();
            errorText = errorJson.message || errorText;
          } else {
            const text = await res.text();
            if (text) errorText = text;
          }
        } catch (parseError) {
          console.error("Error parsing login error response:", parseError);
        }
        msgElem.textContent = `❌ ${errorText}`;
      }
    } catch (error) {
      console.error('Login fetch error:', error);
      msgElem.textContent = '로그인 중 오류가 발생했습니다. 네트워크를 확인해주세요.';
    }
  };
});
