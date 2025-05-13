document.addEventListener('DOMContentLoaded', () => {
    // 폼 요소 가져오기
    const signupForm = document.getElementById('signupForm');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmInput = document.getElementById('confirmPassword');

    // 메시지 표시 요소 가져오기
    const usernameCheckMsg = document.getElementById('usernameCheckMsg'); // 아이디 중복 메시지
    const emailCheckMsg = document.getElementById('emailCheckMsg');       // 이메일 중복 메시지
    const matchMsg = document.getElementById('passwordMatchMsg');         // 비밀번호 일치 메시지
    const signupGeneralMsg = document.getElementById('msg');              // 최종 회원가입 결과 메시지 (기존 #msg 사용)

    const loginLink = document.getElementById("loginLink");

    // redirect 파라미터 처리 (기존 로직 유지)
    const urlParams = new URLSearchParams(location.search);
    const redirectTo = urlParams.get("redirect");
    let baseLoginUrl = '/login.html';
    if (redirectTo)
    {
        try
        {
            if ((redirectTo.startsWith('/') || redirectTo.endsWith('.html')) && !redirectTo.includes(':'))
            {
                baseLoginUrl = `/login.html?redirect=${encodeURIComponent(redirectTo)}`;
            }
        } catch (e)
        {
            console.error("Error processing redirect:", e);
        }
    }
    if(loginLink) loginLink.href = baseLoginUrl;


    // --- 아이디 중복 실시간 체크 ---
    if (usernameInput && usernameCheckMsg)
    {
        usernameInput.addEventListener('blur', async () => {
            const username = usernameInput.value.trim();
            usernameCheckMsg.textContent = '';
            usernameCheckMsg.className = 'validation-msg'; // 기본 클래스로 리셋

            if (username.length < 4)
            {
                usernameCheckMsg.textContent = '아이디는 4자 이상 입력해주세요.';
                usernameCheckMsg.classList.add('error');
                return;
            }

            try
            {
                const response = await fetch(`/api/users/check-username/${encodeURIComponent(username)}`);
                const message = await response.text();
                if (response.ok)
                {
                    usernameCheckMsg.textContent = message;
                    usernameCheckMsg.classList.add('success');
                } else
                {
                    usernameCheckMsg.textContent = message;
                    usernameCheckMsg.classList.add('error');
                }
            } catch (error)
            {
                console.error('아이디 중복 체크 오류:', error);
                usernameCheckMsg.textContent = '아이디 중복 확인 중 오류가 발생했습니다.';
                usernameCheckMsg.classList.add('error');
            }
        });
    }

    // --- 이메일 중복 실시간 체크 ---
    if (emailInput && emailCheckMsg)
    {
        emailInput.addEventListener('blur', async () => {
            const email = emailInput.value.trim();
            emailCheckMsg.textContent = '';
            emailCheckMsg.className = 'validation-msg'; // 기본 클래스로 리셋

            if (!email.includes('@') || email.length < 5)
            {
                emailCheckMsg.textContent = '유효한 이메일 주소를 입력해주세요.';
                emailCheckMsg.classList.add('error');
                return;
            }

            try
            {
                const response = await fetch(`/api/users/check-email?email=${encodeURIComponent(email)}`);
                const message = await response.text();
                if (response.ok)
                {
                    emailCheckMsg.textContent = message;
                    emailCheckMsg.classList.add('success');
                } else
                {
                    emailCheckMsg.textContent = message;
                    emailCheckMsg.classList.add('error');
                }
            } catch (error)
            {
                console.error('이메일 중복 체크 오류:', error);
                emailCheckMsg.textContent = '이메일 중복 확인 중 오류가 발생했습니다.';
                emailCheckMsg.classList.add('error');
            }
        });
    }

    // --- 비밀번호 일치 확인 함수 (기존 로직 유지) ---
    function checkPasswordMatch()
    {
        if (!passwordInput || !confirmInput || !matchMsg) return; // 요소들이 없으면 실행 안함

        if (confirmInput.value === "")
        {
            matchMsg.textContent = "";
            confirmInput.style.borderColor = '#ccc'; // 기본 테두리 색상
        } else if (passwordInput.value === confirmInput.value)
        {
            matchMsg.textContent = "✔ 일치!";
            matchMsg.style.color = "green";
            confirmInput.style.borderColor = 'green';
        } else
        {
            matchMsg.textContent = "❌ 불일치";
            matchMsg.style.color = "red";
            confirmInput.style.borderColor = 'red';
        }
    }
    if (passwordInput) passwordInput.addEventListener("input", checkPasswordMatch);
    if (confirmInput) confirmInput.addEventListener("input", checkPasswordMatch);


    // --- 회원가입 폼 제출 처리 (기존 로직 + 중복체크 결과 반영) ---
    if (signupForm)
    {
        signupForm.onsubmit = async (e) =>
        {
            e.preventDefault();
            if(signupGeneralMsg) signupGeneralMsg.textContent = ''; // 최종 메시지 초기화

            // 실시간 중복 체크 결과 확인
            if (usernameCheckMsg && usernameCheckMsg.classList.contains('error'))
            {
                if(signupGeneralMsg) signupGeneralMsg.textContent = "❌ 아이디 중복 확인을 해주세요.";
                usernameInput.focus();
                return;
            }
            if (emailCheckMsg && emailCheckMsg.classList.contains('error'))
            {
                if(signupGeneralMsg) signupGeneralMsg.textContent = "❌ 이메일 중복 확인을 해주세요.";
                emailInput.focus();
                return;
            }

            // 비밀번호 일치 여부 재확인 (필수)
            if (passwordInput.value !== confirmInput.value)
            {
                if(signupGeneralMsg) signupGeneralMsg.textContent = "❌ 비밀번호가 일치하지 않습니다.";
                confirmInput.focus();
                return;
            }

            // 비밀번호 길이 확인 (필수)
            if (passwordInput.value.length < 6)
            {
                if(signupGeneralMsg) signupGeneralMsg.textContent = "❌ 비밀번호는 6자 이상이어야 합니다.";
                passwordInput.focus();
                return;
            }

            const formData = new FormData(e.target);
            const data = Object.fromEntries(formData.entries());
            delete data.confirmPassword; // confirmPassword 필드는 서버로 보낼 필요 없음

            try
            {
                const res = await fetch(`/api/users/signup`, { // API 경로 확인
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                let responseText = '';
                try
                {
                    responseText = await res.text(); // 우선 텍스트로 응답을 받음
                } catch (textError)
                {
                    console.warn("응답 텍스트 파싱 불가:", textError);
                }


                if (res.ok)
                {
                    alert("회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.");
                    if (loginLink)
                    { // loginLink가 존재하면 해당 링크로 이동
                        location.href = loginLink.href;
                    } else
                    {
                        location.href = '/login.html'; // 기본 로그인 페이지
                    }
                } else
                {
                    // 서버에서 온 메시지가 있다면 사용, 없다면 기본 메시지
                    const displayMessage = responseText || '회원가입에 실패했습니다. 입력 정보를 확인해주세요.';
                    if(signupGeneralMsg) signupGeneralMsg.textContent = `❌ ${displayMessage} (Status: ${res.status})`;
                }
            } catch (error)
            {
                console.error('Signup fetch error:', error);
                if(signupGeneralMsg) signupGeneralMsg.textContent = '회원가입 중 오류가 발생했습니다. 네트워크 상태를 확인해주세요.';
            }
        };
    } else
    {
        console.warn("signupForm을 찾을 수 없습니다.");
    }
});