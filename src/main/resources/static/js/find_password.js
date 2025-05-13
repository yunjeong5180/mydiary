document.addEventListener('DOMContentLoaded', () => { // DOM이 완전히 로드된 후 스크립트 실행
    const findPasswordForm = document.getElementById('findPasswordForm');
    if (findPasswordForm) {
        findPasswordForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const username = document.getElementById('username').value;
            const email = document.getElementById('email').value;
            const messageDiv = document.getElementById('findPasswordMessage');

            // TODO: 실제로는 서버와 통신하는 로직 구현
            console.log("비밀번호 찾기 요청 아이디:", username, "이메일:", email);

            // ---- 임시 응답 처리 (클라이언트 사이드 데모용) ----
            const requestSuccessful = Math.random() > 0.5;

            if (requestSuccessful) {
                messageDiv.textContent = '입력하신 이메일로 비밀번호 재설정 안내 메일을 발송했습니다. 메일을 확인해주세요.';
                messageDiv.className = 'message success';
                messageDiv.style.display = 'block';
            } else {
                messageDiv.textContent = '입력하신 정보와 일치하는 계정을 찾을 수 없거나, 메일 발송에 실패했습니다.';
                messageDiv.className = 'message error';
                messageDiv.style.display = 'block';
            }
            // ---- 임시 응답 처리 끝 ----
        });
    }
});