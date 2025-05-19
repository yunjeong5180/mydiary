// ✅ 전역 에러 감지용 (JS 런타임 오류 추적)
window.addEventListener('error', (e) => {
  console.error("❌ JS 런타임 에러 발생:", e.message, e.filename, e.lineno);
});

document.addEventListener('DOMContentLoaded', () => {
  console.log("✅ login.js loaded"); // 🔍 스크립트 실행 확인용

  const loginForm = document.getElementById('loginForm');
  const msgElem = document.getElementById('msg'); // login.html에 id="msg" 요소가 있어야 합니다.
  const signupLinkBtn = document.getElementById('signupLinkBtn');

  console.log("✅ loginForm 존재 여부:", loginForm);
  if (signupLinkBtn) {
    console.log("✅ signupLinkBtn 존재 여부:", signupLinkBtn);
  } else {
    console.warn("❗ signupLinkBtn을 찾을 수 없습니다. HTML에서 id='signupLinkBtn' 확인 필요!");
  }

  const initialUrlParams = new URLSearchParams(location.search);
  const initialRedirectTo = initialUrlParams.get("redirect");
  console.log("✅ 페이지 로드 시 redirectTo 값:", initialRedirectTo);

  if (signupLinkBtn) {
    signupLinkBtn.onclick = (event) => {
      event.preventDefault();
      let signupUrl = '/signup.html'; // signup.html이 static 폴더 바로 아래에 있다고 가정
      if (
        initialRedirectTo &&
        (initialRedirectTo.startsWith('/') || initialRedirectTo.endsWith('.html')) &&
        !initialRedirectTo.includes(':') &&
        !initialRedirectTo.includes('//')
      ) {
        signupUrl += `?redirect=${encodeURIComponent(initialRedirectTo)}`;
      }
      console.log("🔗 회원가입 버튼 클릭 → 이동할 주소:", signupUrl);
      location.href = signupUrl;
    };
  }

  if (loginForm) {
    loginForm.onsubmit = async (e) => {
      e.preventDefault();
      console.log("✅ loginForm.onsubmit 함수 실행됨!");
      if (msgElem) { // msgElem이 존재하는 경우에만 초기화
        msgElem.textContent = '';
      }


      const currentUrlParams = new URLSearchParams(location.search);
      const redirectToOnSubmit = currentUrlParams.get("redirect");
      console.log("✅ 폼 제출 시점의 redirectTo 값:", redirectToOnSubmit);

      const formData = new FormData(e.target);
      const body = Object.fromEntries(formData.entries());
      console.log("📤 로그인 요청 데이터:", body);

      // --- 👇 여기가 수정된 부분입니다 ---
      // API URL 생성 (절대 경로 및 올바른 API 경로 사용)
      // const loginUrl = `${location.origin}/users/login`; // 수정 전
      const loginUrl = `${location.origin}/api/users/login`; // 수정 후 ('/api' 추가)
      // 또는 상대 경로로: const loginUrl = '/api/users/login';
      // --- 👆 수정된 부분 끝 ---
      console.log("📤 로그인 요청 URL:", loginUrl);

      try {
        const res = await fetch(loginUrl, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify(body)
        });

        console.log("📥 로그인 응답 상태:", res.status);
        console.log("📥 로그인 응답 헤더:", Object.fromEntries([...res.headers.entries()]));
        console.log("📝 현재 쿠키:", document.cookie);

        if (res.ok) {
          console.log("✅ 로그인 성공!");
          localStorage.setItem('isLoggedIn', 'true');

          if (!document.cookie.includes('session=') && !document.cookie.includes('auth=')) { // JSESSIONID 등 실제 세션 쿠키명으로 변경 가능
            console.warn("⚠️ 로그인 성공했으나 인증 쿠키가 없습니다!");
          }

          let finalRedirectUrl = '/index.html'; // 기본 리디렉션 경로 (index.html이 static 폴더 바로 아래에 있다고 가정)

          if (redirectToOnSubmit) {
            if ((redirectToOnSubmit.startsWith('/') || redirectToOnSubmit.endsWith('.html')) &&
                !redirectToOnSubmit.includes(':') &&
                !redirectToOnSubmit.includes('//')) {
              finalRedirectUrl = redirectToOnSubmit.startsWith('/')
                ? redirectToOnSubmit
                : '/' + redirectToOnSubmit;
              console.log(`✅ 유효한 redirect 경로 사용: ${finalRedirectUrl}`);
            } else {
              console.warn(`⚠️ 유효하지 않은 redirect 값 무시: ${redirectToOnSubmit}`);
            }
          } else {
            console.log("✅ redirect 파라미터 없음. 기본 경로(/index.html)로 이동.");
          }
          console.log("🚀 최종 이동 경로:", finalRedirectUrl);
          setTimeout(() => {
            location.href = finalRedirectUrl;
          }, 100);

        } else {
          let errorText = '아이디 또는 비밀번호가 잘못되었습니다.';
          try {
            if (res.headers.get('content-type')?.includes('application/json')) {
                const errorJson = await res.json(); // API가 JSON 형태의 오류 메시지를 보낸다면
                errorText = errorJson.message || errorText;
            } else {
                const text = await res.text(); // 그렇지 않다면 텍스트로 받음
                if (text) errorText = text.startsWith("❌ ") ? text.substring(2) : text; // "❌ " 접두어 제거
            }
          } catch (parseError) {
            console.error("❌ 로그인 오류 응답 파싱 실패:", parseError);
          }
          if (msgElem) { // msgElem이 존재하는 경우에만 메시지 표시
            msgElem.textContent = `❌ ${errorText}`;
          }
          console.warn("⚠️ 로그인 실패 메시지:", errorText);
          localStorage.removeItem('isLoggedIn');
        }
      } catch (error) {
        console.error('❌ 로그인 fetch 오류:', error);
        if (msgElem) { // msgElem이 존재하는 경우에만 메시지 표시
            msgElem.textContent = '로그인 중 오류가 발생했습니다. 네트워크 연결을 확인해주세요.';
        }
        localStorage.removeItem('isLoggedIn');
      }
    };
  } else {
    console.warn("❗ loginForm을 찾을 수 없습니다. HTML에서 id='loginForm' 확인 필요!");
  }
});