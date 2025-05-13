// ✅ 전역 에러 감지용 (JS 런타임 오류 추적)
window.addEventListener('error', (e) => {
  console.error("❌ JS 런타임 에러 발생:", e.message, e.filename, e.lineno);
});

document.addEventListener('DOMContentLoaded', () => {
  console.log("✅ login.js loaded"); // 🔍 스크립트 실행 확인용

  const loginForm = document.getElementById('loginForm');
  const msgElem = document.getElementById('msg');
  const signupLinkBtn = document.getElementById('signupLinkBtn'); // ❗ 주석 해제

  console.log("✅ loginForm 존재 여부:", loginForm);
  if (signupLinkBtn) { // ❗ signupLinkBtn 존재 여부도 로그로 확인
    console.log("✅ signupLinkBtn 존재 여부:", signupLinkBtn);
  } else {
    console.warn("❗ signupLinkBtn을 찾을 수 없습니다. HTML에서 id='signupLinkBtn' 확인 필요!"); // ❗ HTML에 ID가 없으면 이 로그가 찍힐 것임
  }


  // 🔁 페이지 로드 시 한 번만 redirect 값 읽어서 회원가입 링크 설정
  const initialUrlParams = new URLSearchParams(location.search);
  const initialRedirectTo = initialUrlParams.get("redirect");
  console.log("✅ 페이지 로드 시 redirectTo 값:", initialRedirectTo);

  // 🔗 회원가입 버튼에 redirect 정보 추가 (페이지 로드 시 값 기준)
  if (signupLinkBtn) { // 이 조건문은 이제 ID가 있다면 항상 true가 됨
    signupLinkBtn.onclick = (event) => { // event 파라미터 추가 (선택적이지만 좋은 습관)
      event.preventDefault(); // 버튼의 기본 동작(만약 있다면) 방지
      let signupUrl = '/signup.html';
      if (
        initialRedirectTo &&
        (initialRedirectTo.startsWith('/') || initialRedirectTo.endsWith('.html')) &&
        !initialRedirectTo.includes(':') &&
        !initialRedirectTo.includes('//')
      ) {
        // 유효한 로컬 경로일 경우에만 redirect 파라미터 추가
        signupUrl += `?redirect=${encodeURIComponent(initialRedirectTo)}`;
      }
      console.log("🔗 회원가입 버튼 클릭 → 이동할 주소:", signupUrl);
      location.href = signupUrl;
    };
  }
  // else 부분은 signupLinkBtn을 못찾는 경우의 로그이므로 그대로 둡니다.
  // 하지만 HTML에 ID를 추가하면 이 else 블록은 실행되지 않아야 합니다.

  // ✅ 로그인 폼 제출 처리
  if (loginForm) {
    loginForm.onsubmit = async (e) => {
      e.preventDefault(); // 기본 폼 제출 방지
      console.log("✅ loginForm.onsubmit 함수 실행됨!");
      msgElem.textContent = ''; // 이전 오류 메시지 초기화

      // *** 중요: 폼 제출 시점의 URL에서 redirect 파라미터를 다시 읽어옴 ***
      const currentUrlParams = new URLSearchParams(location.search);
      const redirectToOnSubmit = currentUrlParams.get("redirect");
      console.log("✅ 폼 제출 시점의 redirectTo 값:", redirectToOnSubmit);

      const formData = new FormData(e.target);
      const body = Object.fromEntries(formData.entries());
      console.log("📤 로그인 요청 데이터:", body);

      // API URL 생성 (절대 경로 사용)
      const loginUrl = `${location.origin}/users/login`;
      console.log("📤 로그인 요청 URL:", loginUrl);

      try {
        const res = await fetch(loginUrl, { // 수정된 URL 사용
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include', // 쿠키 전송/수신을 위해 필요
          body: JSON.stringify(body)
        });

        console.log("📥 로그인 응답 상태:", res.status);
        console.log("📥 로그인 응답 헤더:", Object.fromEntries([...res.headers.entries()]));
        console.log("📝 현재 쿠키:", document.cookie);

        if (res.ok) {
          // 로그인 성공
          console.log("✅ 로그인 성공!");

          // 로그인 상태 로컬 스토리지에 저장
          localStorage.setItem('isLoggedIn', 'true');

          // 쿠키가 설정되었는지 확인
          if (!document.cookie.includes('session=') && !document.cookie.includes('auth=')) {
            console.warn("⚠️ 로그인 성공했으나 인증 쿠키가 없습니다!");
          }

          // 리디렉션 경로 결정 (폼 제출 시점의 redirectTo 값 사용)
          let finalRedirectUrl = '/index.html'; // 기본 리디렉션 경로

          if (redirectToOnSubmit) {
            // 보안 검사 (유효하고 안전한 경로인지 확인)
            if ((redirectToOnSubmit.startsWith('/') || redirectToOnSubmit.endsWith('.html')) &&
                !redirectToOnSubmit.includes(':') &&
                !redirectToOnSubmit.includes('//')) {

              // 경로 정규화 (슬래시 처리)
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

          // 쿠키 설정을 위한 약간의 지연 후 리다이렉트
          setTimeout(() => {
            location.href = finalRedirectUrl;
          }, 100);

        } else {
          // 로그인 실패 시 서버에서 보낸 오류 메시지를 표시
          let errorText = '아이디 또는 비밀번호가 잘못되었습니다.'; // 기본 오류 메시지
          try {
            if (res.headers.get('content-type')?.includes('application/json')) {
                const errorJson = await res.json();
                errorText = errorJson.message || errorText;
            } else {
                const text = await res.text();
                if (text) errorText = text;
            }
          } catch (parseError) {
            console.error("❌ 로그인 오류 응답 파싱 실패:", parseError);
          }
          msgElem.textContent = `❌ ${errorText}`;
          console.warn("⚠️ 로그인 실패 메시지:", errorText);

          // 로그인 실패 시 로컬 스토리지 상태 제거
          localStorage.removeItem('isLoggedIn');
        }
      } catch (error) {
        // 네트워크 오류 등 fetch 자체 실패
        console.error('❌ 로그인 fetch 오류:', error);
        msgElem.textContent = '로그인 중 오류가 발생했습니다. 네트워크 연결을 확인해주세요.';

        // 로그인 실패 시 로컬 스토리지 상태 제거
        localStorage.removeItem('isLoggedIn');
      }
    };
  } else {
    console.warn("❗ loginForm을 찾을 수 없습니다. HTML에서 id='loginForm' 확인 필요!");
  }
});