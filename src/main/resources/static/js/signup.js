// # signup.html 전용 스크립트


const passwordInput = document.getElementById("password");
const confirmInput = document.getElementById("confirmPassword");
const matchMsg = document.getElementById("passwordMatchMsg");
const signupForm = document.getElementById('signupForm');
const msgElem = document.getElementById("msg");
const loginLink = document.getElementById("loginLink");

const urlParams = new URLSearchParams(location.search);
const redirectTo = urlParams.get("redirect");

let baseLoginUrl = '/login.html';
if (redirectTo) {
  try {
    if ((redirectTo.startsWith('/') || redirectTo.endsWith('.html')) && !redirectTo.includes(':')) {
      baseLoginUrl = `/login.html?redirect=${encodeURIComponent(redirectTo)}`;
    }
  } catch (e) {
    console.error("Error processing redirect:", e);
  }
}
loginLink.href = baseLoginUrl;

function checkPasswordMatch() {
  if (confirmInput.value === "") {
    matchMsg.textContent = "";
    confirmInput.style.borderColor = '#ccc';
  } else if (passwordInput.value === confirmInput.value) {
    matchMsg.textContent = "✔ 일치!";
    matchMsg.style.color = "green";
    confirmInput.style.borderColor = 'green';
  } else {
    matchMsg.textContent = "❌ 불일치";
    matchMsg.style.color = "red";
    confirmInput.style.borderColor = 'red';
  }
}
passwordInput.addEventListener("input", checkPasswordMatch);
confirmInput.addEventListener("input", checkPasswordMatch);

signupForm.onsubmit = async (e) => {
  e.preventDefault();
  msgElem.textContent = '';

  if (passwordInput.value !== confirmInput.value) {
    msgElem.textContent = "❌ 비밀번호가 일치하지 않습니다.";
    confirmInput.focus();
    return;
  }

  if (passwordInput.value.length < 6) {
    msgElem.textContent = "❌ 비밀번호는 6자 이상이어야 합니다.";
    passwordInput.focus();
    return;
  }

  const formData = new FormData(e.target);
  const data = Object.fromEntries(formData.entries());
  delete data.confirmPassword;

  try {
    const res = await fetch(`${location.origin}/users/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });

    if (res.ok) {
      alert("회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.");
      location.href = loginLink.href;
    } else {
      let errorText = '회원가입에 실패했습니다. 입력 정보를 확인해주세요.';
      try {
        if (res.headers.get('content-type')?.includes('application/json')) {
          const errorJson = await res.json();
          errorText = errorJson.message || errorText;
        } else {
          const text = await res.text();
          if (text) errorText = text;
        }
      } catch (parseError) {
        console.error("파싱 오류:", parseError);
      }
      msgElem.textContent = `❌ ${errorText} (Status: ${res.status})`;
    }
  } catch (error) {
    console.error('Signup fetch error:', error);
    msgElem.textContent = '회원가입 중 오류가 발생했습니다. 네트워크 상태를 확인해주세요.';
  }
};
