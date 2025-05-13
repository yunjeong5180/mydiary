// /js/find_id.js
document.addEventListener('DOMContentLoaded', () => {
    const findIdForm = document.getElementById('findIdForm');
    if (findIdForm)
    {
        findIdForm.addEventListener('submit', async function(event)
        { // async 키워드 추가
            event.preventDefault();
            const email = document.getElementById('email').value;
            const messageDiv = document.getElementById('findIdMessage');
            messageDiv.style.display = 'none'; // 이전 메시지 숨김
            messageDiv.className = 'message'; // 기본 클래스로 리셋

            // 입력된 이메일 유효성 검사 (간단한 예시)
            if (!email || !email.includes('@'))
            {
                messageDiv.textContent = '유효한 이메일 주소를 입력해주세요.';
                messageDiv.className = 'message error';
                messageDiv.style.display = 'block';
                return;
            }

            console.log("아이디 찾기 요청 이메일:", email); // 콘솔 로그는 유지

            try
            {
                // 백엔드 API 호출
                const response = await fetch('/api/users/find-id', { // 실제 API 엔드포인트
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ email: email }), // 요청 본문에 이메일 포함
                });

                const result = await response.json(); // 응답을 JSON으로 파싱

                if (response.ok && result.success)
                { // 서버 응답 상태(ok)와 응답 내용(result.success) 모두 확인
                    messageDiv.textContent = result.message + (result.maskedUserId ? ` [${result.maskedUserId}]` : '');
                    messageDiv.className = 'message success';
                } else
                {
                    // 서버에서 보내는 오류 메시지가 있다면 result.message 사용, 없다면 기본 메시지
                    messageDiv.textContent = result.message || '입력하신 정보와 일치하는 아이디를 찾을 수 없습니다.';
                    messageDiv.className = 'message error';
                }
            } catch (error)
            {
                // 네트워크 오류 또는 JSON 파싱 오류 등
                console.error('아이디 찾기 API 호출 중 오류 발생:', error);
                messageDiv.textContent = '아이디를 찾는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                messageDiv.className = 'message error';
            }
            messageDiv.style.display = 'block';
        });
    }
});