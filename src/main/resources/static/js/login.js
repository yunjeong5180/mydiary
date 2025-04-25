// ✅ 전역 에러 감지용 (JS 런타임 오류 추적)
window.addEventListener('error', (e) => {
  console.error("❌ JS 런타임 에러 발생:", e.message);
});

document.addEventListener('DOMContentLoaded', () => {
  console.log("✅ login.js loaded"); // 🔍 스크립트 실행 확인용

  const loginForm = document.getElementById('loginForm');
  const msgElem = document.getElementById('msg');
  const signupLinkBtn = document.getElementById('signupLinkBtn');
  const loginBtn = document.getElementById('loginBtn'); // 🔘 로그인 버튼 가져오기

  console.log("✅ loginForm 존재 여부:", loginForm);

  // 🔘 로그인 버튼 클릭 시 명시적으로 submit 트리거
if (loginBtn && loginForm) {
  loginBtn.addEventListener('click', (e) => {
    e.preventDefault(); // 기본 제출 방지
    console.log("🔘 로그인 버튼 클릭됨!");
    loginForm.requestSubmit(); // form의 onsubmit 강제 실행
  });
}

  // 📌 submit 이벤트도 별도로 확인
  if (loginForm) {
    loginForm.addEventListener('submit', () => {
      console.log("📌 form submit 이벤트 캐치됨!");
    });
  }

  // 🔁 URL에서 redirect 파라미터 읽기
  const urlParams = new URLSearchParams(location.search);
  const redirectTo = urlParams.get("redirect");
  console.log("✅ 로그인 후 이동할 redirectTo 값:", redirectTo);

  // 🔗 회원가입 버튼에 redirect 정보 추가
  if (signupLinkBtn) {
    signupLinkBtn.onclick = () => {
      let base = '/signup.html';
      if (
        redirectTo &&
        (redirectTo.startsWith('/') || redirectTo.endsWith('.html')) &&
        !redirectTo.includes(':') &&
        !redirectTo.includes('//')
      ) {
        base += `?redirect=${encodeURIComponent(redirectTo)}`;
      }
      console.log("🔗 회원가입 버튼 클릭 → 이동할 주소:", base);
      location.href = base;
    };
  }

  // ✅ 로그인 폼 제출 처리
  if (loginForm) {
    loginForm.onsubmit = async (e) => {
      e.preventDefault();
      console.log("✅ loginForm.onsubmit 함수 실행됨!");
      msgElem.textContent = '';

      const formData = new FormData(e.target);
      const body = Object.fromEntries(formData.entries());
      console.log("📤 로그인 요청 데이터:", body);

      try {
        const res = await fetch(`${location.origin}/users/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify(body)
        });

        console.log("📥 로그인 응답 상태:", res.status);

        if (res.ok) {
          let finalRedirectUrl = '/index.html';
          if (
            redirectTo &&
            (redirectTo.startsWith('/') || redirectTo.endsWith('.html')) &&
            !redirectTo.includes(':') &&
            !redirectTo.includes('//')
          ) {
            finalRedirectUrl = '/' + redirectTo.replace(/^\//, '');
          }

          console.log("✅ 로그인 성공 → 이동:", finalRedirectUrl);
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
          console.warn("⚠️ 로그인 실패 메시지:", errorText);
        }
      } catch (error) {
        console.error('Login fetch error:', error);
        msgElem.textContent = '로그인 중 오류가 발생했습니다. 네트워크를 확인해주세요.';
      }
    };
  } else {
    console.warn("❗ loginForm을 찾을 수 없습니다. HTML에서 id='loginForm' 확인 필요!");
  }
});
