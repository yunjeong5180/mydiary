package com.diary.mydiary.service;

import com.diary.mydiary.dto.*;
import com.diary.mydiary.model.*;
import com.diary.mydiary.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // Value 임포트
import org.springframework.mail.MailException; // MailException 임포트
import org.springframework.mail.SimpleMailMessage; // SimpleMailMessage 임포트
import org.springframework.mail.javamail.JavaMailSender; // JavaMailSender 임포트
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Transactional 임포트

import java.util.*; // Date 임포트
// import java.util.List; // 만약 findByEmail이 List를 반환한다면 필요

/**
 * 📌 [UserService] - 사용자 관련 핵심 기능 처리 클래스
 *
 * ✅ 역할:
 *   - 회원가입 시 비밀번호 암호화 처리
 *   - 아이디 중복 여부 확인
 *   - 현재 로그인된 사용자 정보 가져오기 (세션 기반)
 *   - 아이디 찾기, 비밀번호 재설정 요청 처리
 */
@Service
@RequiredArgsConstructor  // 생성자 주입 자동 생성
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // --- 비밀번호 재설정 요청 처리를 위한 의존성 추가 ---
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;

    // --- application.properties (또는 yml) 에서 설정값 주입 ---
    @Value("${app.reset-password.token.expiration-ms}")
    private long tokenExpirationMs;

    @Value("${app.reset-password.base-url}")
    private String resetBaseUrl; // 예: http://localhost:3000 (프론트엔드 주소)

    /**
     * ✅ 회원가입 처리
     * - 비밀번호를 암호화한 뒤 DB에 저장
     */
    public User save(User user)
    {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 🔐 비밀번호 암호화
        return userRepository.save(user);                             // 💾 저장
    }

    /**
     * ✅ 아이디 중복 체크
     * - 같은 username 이 이미 존재하는지 확인
     */
    public boolean existsByUsername(String username)
    {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * ✅ 이메일 중복 체크 (새로 추가)
     * - 같은 email이 이미 존재하는지 확인합니다.
     */
    public boolean existsByEmail(String email)
    { // UserRepository에 findByEmail이 Optional<User>를 반환한다고 가정

        return userRepository.findByEmail(email).isPresent();
        // 만약 UserRepository의 findByEmail이 List<User>를 반환한다면:
        // return !userRepository.findByEmail(email).isEmpty();
    }

    /**
     * ✅ 로그인된 사용자 가져오기
     * - 세션에서 "user" 속성으로 사용자 객체 꺼내오기
     */
    public User getLoggedInUser(HttpSession session)
    {
        Object obj = session.getAttribute("user"); // ✅ 컨트롤러와 키 이름 일치
        if (obj instanceof User u) return u;       // 세션에 유저가 있으면 반환
        throw new IllegalStateException("로그인 사용자가 없습니다.");
    }

    /**
     * ✅ 사용자 인증 (로그인 시 호출)
     * - 아이디/비번 확인 후 유저 객체 반환
     */
    public User validateUser(String username, String rawPassword)
    {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty())
        {
            throw new IllegalArgumentException("유저가 없습니다.");
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
        {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        return user;
    }

    /**
     * userId(PK)로 사용자 가져오기
     */
    public User getUserById(Long id)
    {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * ✅ 이메일로 사용자 아이디(username) 찾기
     * - 찾은 아이디는 마스킹 처리하여 반환합니다.
     */
    public FindIdResponse findUserIdByEmail(String email)
    {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent())
        {
            User user = userOptional.get();
            // User 엔티티의 getUsername() 메소드가 로그인 아이디를 반환한다고 가정합니다.
            String userId = user.getUsername();
            String maskedId = maskUserId(userId);
            return new FindIdResponse(true, "아이디를 찾았습니다.", maskedId);
        } else
        {
            return new FindIdResponse(false, "해당 이메일로 가입된 아이디를 찾을 수 없습니다.", null);
        }
    }

    /**
     * 🔒 아이디 마스킹 처리 유틸리티 메소드
     * - 예: "testuser" -> "tes***er"
     * - 실제 마스킹 정책에 따라 상세 구현 필요
     */
    private String maskUserId(String userId)
    {
        if (userId == null || userId.isEmpty())
        {
            return "";
        }
        int length = userId.length();
        if (length <= 3)
        {
            // 3글자 이하면 첫 글자만 보여주고 나머지는 '*' (예: t**)
            // 또는 정책에 따라 다르게 처리 (예: 전부 마스킹 ***)
            return userId.substring(0, 1) + "*".repeat(Math.max(0, length - 1));
        } else if (length <= 5)
        {
            // 4~5 글자: 앞 2글자 + 나머지 '*' (예: te***)
            return userId.substring(0, 2) + "*".repeat(length - 2);
        } else
        {
            // 6글자 이상: 앞 3글자 + 중간 '*' + 마지막 2글자 (예: tes***er)
            // (주의: 중간 '*' 개수는 (전체길이 - 앞3 - 뒤2) 여야 함)
            int visibleStart = 3;
            int visibleEnd = 2;
            return userId.substring(0, visibleStart) +
                    "*".repeat(Math.max(0, length - visibleStart - visibleEnd)) +
                    userId.substring(length - visibleEnd);
        }
    }

    /**
     * 🔑 비밀번호 재설정 요청 처리
     * - 사용자가 입력한 아이디(username)와 이메일이 일치하는지 확인합니다.
     * - 일치하면 비밀번호 재설정 토큰을 생성하고, 해당 토큰을 포함한 링크를 이메일로 발송합니다.
     * @param request PasswordResetRequest (userId, email 포함)
     * @throws IllegalArgumentException 아이디 또는 이메일이 누락된 경우
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        // PasswordResetRequest의 getUserId()가 실제로는 사용자의 로그인 ID(username)를 의미한다고 가정
        // 1. 요청으로 받은 사용자 아이디(username)와 이메일 값 확인
        String username = request.getUserId();
        String email = request.getEmail();
        System.out.println("[DEBUG] Attempting password reset for username: " + username + ", email: " + email);
        // └─확인사항: 프론트엔드에서 입력한 값이 정확히 넘어왔는지 확인합니다.

        // 2. 아이디 또는 이메일이 비어있는지 유효성 검사
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("아이디와 이메일을 모두 입력해주세요.");
        }

        // 3. DB에서 해당 아이디와 이메일로 사용자 조회
        // UserRepository에 findByUsernameAndEmail 메소드가 필요합니다.
        // User 모델의 getUsername()과 getEmail()이 각각 아이디와 이메일을 반환한다고 가정합니다.
        Optional<User> userOptional = userRepository.findByUsernameAndEmail(username, email);
        System.out.println("[DEBUG] User found by username and email: " + userOptional.isPresent());
        // └─확인사항:
        //    - true: 사용자를 찾음. 다음 단계(토큰 생성 및 메일 발송)로 진행합니다.
        //    - false: 사용자를 찾지 못함. 입력한 아이디/이메일이 DB 정보와 일치하지 않거나, 쿼리 조건에 문제가 있을 수 있습니다.

        if (userOptional.isPresent())
        { // 3-1. 사용자를 찾은 경우
            User user = userOptional.get();
            // DB에서 조회된 사용자 정보 로깅 (입력값과 비교하여 정확성 확인)
            System.out.println("[DEBUG] Processing for user - DB ID: " + user.getId() +
                    ", DB Username: " + user.getUsername() + ", DB Email: " + user.getEmail());

            // (선택적) 기존에 발급된 만료되지 않은 토큰이 있다면 무효화 처리
            // passwordResetTokenRepository.invalidateExistingTokensForUser(user.getId()); // 이런 메소드 구현 필요

            // 4. 비밀번호 재설정 토큰 생성
            String token = UUID.randomUUID().toString();
            Date expiryDate = new Date(System.currentTimeMillis() + tokenExpirationMs);

            // 5. 토큰 엔티티 생성 및 정보 설정
            PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
            resetToken.setToken(token);
            resetToken.setUser(user); // User 객체와 연관관계 설정
            resetToken.setExpiryDate(expiryDate);
            resetToken.setUsed(false); // 기본값은 false이지만 명시적으로 설정

            // 6. 생성된 토큰을 DB에 저장
            passwordResetTokenRepository.save(resetToken);
            System.out.println("[DEBUG] Token generated and saved: " + token + ". Attempting to send email...");
            // └─확인사항: 이 로그가 출력되면 토큰 생성 및 DB 저장까지는 성공한 것입니다.

            // 7. 사용자 이메일로 재설정 링크 발송
            sendPasswordResetEmail(user.getEmail(), token);
        }else
        { // 3-2. 사용자를 찾지 못한 경우
            System.out.println("[DEBUG] User not found or email/username mismatch for requested username: " + username + " and email: " + email);
            // └─주의: 이 경우에도 Controller는 보안을 위해 일관된 성공 메시지를 반환합니다.
            //          실제로는 이메일이 발송되지 않습니다.
        }
        // 사용자가 존재하지 않거나 정보가 불일치하더라도, Controller에서 일관된 메시지를 반환하므로
        // 여기서는 별도의 예외를 발생시키지 않거나, 내부 로깅만 수행할 수 있습니다.
    }

    /**
     * 📧 비밀번호 재설정 이메일 발송
     * @param toEmail 수신자 이메일 주소
     * @param token 비밀번호 재설정 토큰
     */
    private void sendPasswordResetEmail(String toEmail, String token) {
        System.out.println("[DEBUG] Inside sendPasswordResetEmail method. Recipient: " + toEmail + ", Token: " + token);

        String subject = "[내 서비스 이름] 비밀번호 재설정 안내"; // TODO: "[내 서비스 이름]"을 실제 서비스명으로 변경하세요.
        // !!! HTML 파일명(reset_password.html)과 일치하도록 수정 !!!
        String resetPageUrl = resetBaseUrl + "/reset_password.html?token=" + token; // 수정된 부분
        String messageText = "안녕하세요.\n\n" +
                "비밀번호를 재설정하려면 아래 링크를 클릭하세요. (링크는 " + (tokenExpirationMs / (1000 * 60)) + "분 동안 유효합니다.):\n" +
                resetPageUrl + "\n\n" +
                "만약 비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시해주세요.\n\n" +
                "감사합니다.";

        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(toEmail);
        emailMessage.setSubject(subject);
        emailMessage.setText(messageText);

        try {
            mailSender.send(emailMessage);
            System.out.println("[DEBUG] Password reset email successfully sent to " + toEmail);
        } catch (MailException e) {
            System.err.println("[ERROR] Error sending password reset email to " + toEmail + ". Token: " + token + ". Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔒 새 비밀번호로 사용자의 비밀번호를 재설정합니다.
     * @param request ResetPasswordRequest (token, newPassword 포함)
     * @throws IllegalArgumentException 토큰이 유효하지 않거나, 만료되었거나, 이미 사용된 경우 또는 비밀번호 정책에 맞지 않는 경우
     */
    @Transactional // 여러 DB 작업을 하나의 트랜잭션으로 묶어 처리 (사용자 비밀번호 변경, 토큰 상태 변경)
    public void resetPassword(ResetPasswordRequest request)
    {
        String token = request.getToken();
        String newPassword = request.getNewPassword();

        // 1. 토큰 유효성 검증 (토큰 존재 여부)
        // PasswordResetTokenRepository에 findByToken(String token) 메소드가 정의되어 있어야 합니다.
        PasswordResetTokenEntity passwordResetToken =
                passwordResetTokenRepository.findByToken(token)
                        .orElseThrow(() -> {
                            // 토큰이 존재하지 않으면 예외 발생
                            System.err.println("[ERROR] Invalid token received for password reset: " + token);
                            return new IllegalArgumentException("제공된 토큰이 유효하지 않습니다. 링크를 다시 확인해주세요.");
                        });

        // 1-1. 토큰 사용 여부 검증
        if (passwordResetToken.isUsed())
        {
            System.err.println("[ERROR] Attempt to use an already used token: " + token);
            throw new IllegalArgumentException("이미 사용된 토큰입니다. 비밀번호 재설정을 다시 요청해주세요.");
        }

        // 1-2. 토큰 만료 여부 검증
        // PasswordResetTokenEntity에 isTokenExpired() 메소드가 정의되어 있어야 합니다.
        if (passwordResetToken.isTokenExpired())
        {
            System.err.println("[ERROR] Expired token received: " + token);
            // 만료된 토큰은 DB에서 삭제하거나, isUsed를 true로 변경하여 재사용을 막는 정책을 고려할 수 있습니다.
            // 여기서는 간단히 예외만 발생시킵니다. 필요시 아래 주석 해제하여 삭제 로직 추가
            // passwordResetTokenRepository.delete(passwordResetToken);
            throw new IllegalArgumentException("토큰이 만료되었습니다. 비밀번호 재설정을 다시 요청해주세요.");
        }

        // (선택적이지만 권장) 백엔드에서의 비밀번호 정책 검증
        // ResetPasswordRequest DTO에 @Size, @Pattern 등으로 이미 유효성 검사를 할 수 있지만,
        // 서비스 계층에서 한 번 더 명시적으로 검증할 수도 있습니다.
        // 예를 들어, User 엔티티에 비밀번호 정책 관련 메소드가 있다면 호출하여 검증
        if (newPassword == null || newPassword.trim().length() < 8)
        { // 예시: 최소 8자 (DTO에서 이미 검증될 수 있음)
            System.err.println("[ERROR] New password does not meet policy (too short): " + newPassword.substring(0, Math.min(3, newPassword.length())) + "***");
            throw new IllegalArgumentException("새 비밀번호는 최소 8자 이상이어야 합니다.");
        }
        // TODO: 여기에 더 구체적인 비밀번호 복잡도(대소문자, 숫자, 특수문자 포함 등) 검증 로직 추가 가능


        // 2. 토큰이 유효하면, 해당 토큰과 연결된 사용자 정보를 가져옵니다.
        User user = passwordResetToken.getUser();
        if (user == null)
        { // 데이터 무결성 문제 발생 가능성에 대한 방어 코드
            System.err.println("[CRITICAL ERROR] User not found for a valid token: " + token);
            throw new IllegalStateException("토큰과 연결된 사용자 정보를 찾을 수 없습니다. 관리자에게 문의하세요.");
        }
        System.out.println("[DEBUG] User found for token. User ID: " + user.getId() + ", Username: " + user.getUsername());

        // 3. 새 비밀번호를 암호화하여 사용자 정보에 업데이트합니다.
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user); // 변경된 사용자 정보(새 비밀번호)를 DB에 저장
        System.out.println("[DEBUG] Password updated for user ID: " + user.getId());

        // 4. 사용된 토큰을 '사용됨(isUsed=true)' 상태로 변경하여 재사용을 방지합니다.
        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken); // 변경된 토큰 상태를 DB에 저장
        System.out.println("[DEBUG] Token marked as used: " + token);

        System.out.println("[INFO] Password successfully reset for user: " + user.getUsername());
    }
}
