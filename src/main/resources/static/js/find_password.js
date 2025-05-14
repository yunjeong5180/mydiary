document.addEventListener('DOMContentLoaded', () => { // DOM이 완전히 로드된 후 스크립트 실행
    const findPasswordForm = document.getElementById('findPasswordForm');
    const messageDiv = document.getElementById('findPasswordMessage');

    if (findPasswordForm)
    {
        console.log("[DEBUG] findPasswordForm 요소를 찾았습니다. 이벤트 리스너를 추가합니다.");

        findPasswordForm.addEventListener('submit', async function(event)
        {
            event.preventDefault();
            console.log("[DEBUG] 비밀번호 찾기 폼 제출 이벤트 발생됨.");

            // HTML의 id="username"에 맞춰서 값을 가져옵니다.
            const usernameInput = document.getElementById('username'); // << 여기가 중요!
            const emailInput = document.getElementById('email');

            if (!usernameInput || !emailInput)
            {
                console.error("[ERROR] 아이디 또는 이메일 입력 필드를 찾을 수 없습니다. HTML의 id를 확인하세요.");
                if(messageDiv)
                {
                    messageDiv.textContent = '오류: 입력 필드를 찾을 수 없습니다.';
                    messageDiv.className = 'message error';
                    messageDiv.style.display = 'block';
                }
                return;
            }

            const username = usernameInput.value; // 변수명도 username으로 통일 (가독성)
            const email = emailInput.value;

            console.log("[DEBUG] 비밀번호 찾기 요청 - 아이디(username):", username, ", 이메일:", email);

            if(messageDiv)
            {
                messageDiv.textContent = '';
                messageDiv.className = 'message';
                messageDiv.style.display = 'none';
            }

            if (!username.trim() || !email.trim())
            {
                console.log("[DEBUG] 프론트엔드 유효성 검사: 아이디 또는 이메일이 비어있습니다.");
                if (messageDiv)
                {
                    messageDiv.textContent = '아이디와 이메일을 모두 입력해주세요.';
                    messageDiv.className = 'message error';
                    messageDiv.style.display = 'block';
                }
                return;
            }

            try
            {
                console.log("[DEBUG] API 호출 시도: /api/users/request-password-reset");

                // 백엔드 PasswordResetRequest DTO의 필드명이 'userId'이고 이것이 username을 의미한다면 아래와 같이 전송
                // 만약 백엔드 DTO 필드명도 'username'이라면 { username: username, email: email } 로 변경
                const requestBody = { userId: username, email: email };
                console.log("[DEBUG] API 요청 본문:", JSON.stringify(requestBody)); // 요청 본문 확인용 로그


                const response = await fetch('/api/users/request-password-reset',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(requestBody),
                });

                console.log("[DEBUG] API 응답 상태 코드:", response.status);

                const result = await response.json();
                console.log("[DEBUG] API 응답 결과:", result);

                if (messageDiv)
                {
                    if (response.ok && result.success)
                    {
                        messageDiv.textContent = result.message || '입력하신 이메일로 비밀번호 재설정 안내 메일을 발송했습니다. 메일을 확인해주세요.';
                        messageDiv.className = 'message success';
                    } else
                    {
                        messageDiv.textContent = result.message || '요청 처리 중 오류가 발생했습니다. 다시 시도해주세요.';
                        messageDiv.className = 'message error';
                    }
                    messageDiv.style.display = 'block';
                }

            } catch (error)
            {
                console.error("[ERROR] API 호출 또는 응답 처리 중 예외 발생:", error);
                if (messageDiv)
                {
                    messageDiv.textContent = '네트워크 오류 또는 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.';
                    messageDiv.className = 'message error';
                    messageDiv.style.display = 'block';
                }
            }
        });
    } else
    {
        console.error("[ERROR] findPasswordForm 요소를 찾을 수 없습니다. HTML의 id가 'findPasswordForm'인지 확인하세요.");
    }
});