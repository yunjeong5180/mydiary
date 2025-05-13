document.addEventListener('DOMContentLoaded', () => { // DOM이 완전히 로드된 후 스크립트 실행
    const findIdForm = document.getElementById('findIdForm');
    if (findIdForm) {
        findIdForm.addEventListener('submit', function(event) {
            event.preventDefault(); // 폼 기본 제출 방지
            const email = document.getElementById('email').value;
            const messageDiv = document.getElementById('findIdMessage');

            // TODO: 실제로는 이 부분에서 서버로 email을 보내서 아이디를 조회해야 합니다.
            console.log("아이디 찾기 요청 이메일:", email);

            // ---- 임시 응답 처리 (클라이언트 사이드 데모용) ----
            const userExists = Math.random() > 0.5;
            const foundId = "testuser***";

            if (userExists) {
                messageDiv.textContent = `회원님의 아이디는 [${foundId}] 입니다.`;
                messageDiv.className = 'message success';
                messageDiv.style.display = 'block';
            } else {
                messageDiv.textContent = '입력하신 정보와 일치하는 아이디를 찾을 수 없습니다.';
                messageDiv.className = 'message error';
                messageDiv.style.display = 'block';
            }
            // ---- 임시 응답 처리 끝 ----
        });
    }
});