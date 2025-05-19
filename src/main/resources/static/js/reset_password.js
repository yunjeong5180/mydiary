document.addEventListener('DOMContentLoaded', () => {
    const resetPasswordForm = document.getElementById('resetPasswordForm');
    const messageDiv = document.getElementById('resetPasswordMessage');
    let currentToken = ''; // 페이지 로드 시 추출한 토큰을 저장할 변수

    // 1. 페이지 로드 시 URL에서 토큰 추출
    try
    {
        const urlParams = new URLSearchParams(window.location.search);
        currentToken = urlParams.get('token'); // 'token' 파라미터 값 가져오기
        console.log("[DEBUG] URL에서 추출한 토큰:", currentToken);

        if (!currentToken)
        {
            console.error("[ERROR] URL에서 토큰을 찾을 수 없습니다.");
            if (messageDiv)
            {
                messageDiv.textContent = '유효하지 않은 접근입니다. 링크를 다시 확인해주세요.';
                messageDiv.className = 'message error';
                messageDiv.style.display = 'block';
            }
            if (resetPasswordForm)
            {
                // 폼의 모든 입력 필드와 버튼을 비활성화
                Array.from(resetPasswordForm.elements).forEach(element => element.disabled = true);
            }
            return; // 토큰 없으면 더 이상 진행 안 함
        }
        // 필요하다면 <input type="hidden" id="resetToken"> 에 값을 설정할 수도 있음
        // const hiddenTokenInput = document.getElementById('resetToken');
        // if (hiddenTokenInput) hiddenTokenInput.value = currentToken;

    } catch (e)
    {
        console.error("[ERROR] URL 파라미터 처리 중 오류:", e);
        if (messageDiv)
        {
            messageDiv.textContent = '페이지 로드 중 오류가 발생했습니다.';
            messageDiv.className = 'message error';
            messageDiv.style.display = 'block';
        }
        return;
    }


    if (resetPasswordForm)
    {
        resetPasswordForm.addEventListener('submit', async function(event)
        {
            event.preventDefault();
            console.log("[DEBUG] 새 비밀번호 설정 폼 제출 이벤트 발생됨.");

            const newPasswordInput = document.getElementById('newPassword');
            const confirmPasswordInput = document.getElementById('confirmPassword');

            if (!newPasswordInput || !confirmPasswordInput)
            {
                console.error("[ERROR] 새 비밀번호 또는 확인 입력 필드를 찾을 수 없습니다.");
                // 사용자에게 메시지 표시 로직 (messageDiv 사용)
                return;
            }

            const newPassword = newPasswordInput.value;
            const confirmPassword = confirmPasswordInput.value;

            if(messageDiv)
            { // 메시지 영역 초기화
                messageDiv.textContent = '';
                messageDiv.className = 'message';
                messageDiv.style.display = 'none';
            }

            // 프론트엔드 유효성 검사
            if (!newPassword || !confirmPassword)
            {
                console.log("[DEBUG] 프론트엔드 유효성: 비밀번호 필드가 비어있습니다.");
                if (messageDiv)
                {
                    messageDiv.textContent = '새 비밀번호와 확인 비밀번호를 모두 입력해주세요.';
                    messageDiv.className = 'message error';
                    messageDiv.style.display = 'block';
                }
                return;
            }
            if (newPassword !== confirmPassword)
            {
                console.log("[DEBUG] 프론트엔드 유효성: 비밀번호가 일치하지 않습니다.");
                if (messageDiv)
                {
                    messageDiv.textContent = '새 비밀번호와 확인 비밀번호가 일치하지 않습니다.';
                    messageDiv.className = 'message error';
                    messageDiv.style.display = 'block';
                }
                return;
            }

            if (newPassword.length < 8 || newPassword.length > 20)
            {
                console.log("[DEBUG] 프론트엔드 유효성: 비밀번호 길이가 정책(8~20자)에 맞지 않습니다.");
                if (messageDiv)
                {
                    messageDiv.textContent = '비밀번호는 8자 이상 20자 이하로 입력해주세요.';
                    messageDiv.className = 'message error';
                    messageDiv.style.display = 'block';
                }
                return; // API 호출하지 않고 여기서 중단
            }

            // TODO: 여기에 비밀번호 복잡도 검사 로직 추가 (예: 최소 8자, 영문/숫자/특수문자 조합 등)
            // if (newPassword.length < 8) { ... }

            console.log("[DEBUG] 새 비밀번호 설정 요청 - Token:", currentToken, "New Password:", newPassword.substring(0,2) + "***"); // 실제 비밀번호는 로그에 남기지 않도록 주의


            try
            {
                console.log("[DEBUG] API 호출 시도: /api/users/reset-password");
                const requestBody = { token: currentToken, newPassword: newPassword };
                console.log("[DEBUG] API 요청 본문 (비밀번호 마스킹):", JSON.stringify({ token: currentToken, newPassword: newPassword.substring(0,2) + "***"}));


                const response = await fetch('/api/users/reset-password', {
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
                        messageDiv.textContent = result.message || '비밀번호가 성공적으로 변경되었습니다. 로그인 페이지로 이동합니다.';
                        messageDiv.className = 'message success';
                        // 성공 시 폼 비활성화 및 로그인 페이지로 리디렉션
                        Array.from(resetPasswordForm.elements).forEach(element => element.disabled = true);
                        setTimeout(() => {
                            window.location.href = '/login.html'; // 로그인 페이지 경로
                        }, 3000); // 3초 후 이동
                    } else
                    {
                        messageDiv.textContent = result.message || '비밀번호 변경에 실패했습니다. 다시 시도해주세요.';
                        messageDiv.className = 'message error';
                        // 실패 시 비밀번호 입력 필드 초기화
                        newPasswordInput.value = '';
                        confirmPasswordInput.value = '';
                        newPasswordInput.focus();
                    }
                    messageDiv.style.display = 'block';
                }

            } catch (error)
            {
                console.error("[ERROR] API 호출 또는 응답 처리 중 예외 발생:", error);
                if (messageDiv)
                {
                    messageDiv.textContent = '네트워크 오류 또는 서버에 연결할 수 없습니다.';
                    messageDiv.className = 'message error';
                    messageDiv.style.display = 'block';
                }
            }
        });
    } else
    {
        console.error("[ERROR] resetPasswordForm 요소를 찾을 수 없습니다.");
    }
});