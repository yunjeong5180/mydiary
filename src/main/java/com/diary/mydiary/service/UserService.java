package com.diary.mydiary.service;

// 필요한 DTO, Model, Repository, 유틸리티 클래스들을 가져온다.
import com.diary.mydiary.dto.*;
import com.diary.mydiary.model.*;
import com.diary.mydiary.repository.*;
import jakarta.servlet.http.HttpSession;                             // 세션 사용
import lombok.RequiredArgsConstructor;                               // Lombok (생성자 자동 주입)
import org.springframework.beans.factory.annotation.Value;           // application.properties 값 주입용
import org.springframework.mail.MailException;                       // 이메일 발송 실패 시 예외
import org.springframework.mail.SimpleMailMessage;                   // 간단한 텍스트 이메일 작성용
import org.springframework.mail.javamail.JavaMailSender;             // 스프링 이메일 발송 인터페이스
import org.springframework.security.crypto.password.PasswordEncoder; // 비밀번호 암호화 인터페이스
import org.springframework.stereotype.Service;                       // 개발자 왈: "이 클래스는 서비스 로직을 담당해!"
import org.springframework.transaction.annotation.Transactional;     // DB 작업의 원자성 보장 (All or Nothing)

import java.util.*; // Date, UUID 등 사용

/**
 * 📌 [UserService] - 사용자 관련 핵심 기능(비즈니스 로직) 처리 클래스
 *
 * ✅ 주요 역할:
 * - 회원가입: 비밀번호 암호화, 사용자 정보 DB 저장
 * - 중복 체크: 아이디(username), 이메일 중복 여부 확인
 * - 로그인 인증: 아이디/비밀번호 검증
 * - 세션 관리: 현재 로그인된 사용자 정보 조회 (컨트롤러에서 세션 객체를 받아 처리)
 * - 계정 찾기: 이메일로 아이디 찾기 (마스킹 처리), 비밀번호 재설정 요청 처리 (토큰 생성 및 이메일 발송)
 * - 비밀번호 변경: 재설정 토큰 검증 후 새 비밀번호로 업데이트
 *
 * - Controller는 사용자 요청을 받고, 실제 복잡한 처리는 이 Service 클래스에 위임
 * - Repository를 통해 데이터베이스와 상호작용
 */
@Service // 이 클래스가 비즈니스 로직을 처리하는 서비스 계층의 컴포넌트임을 스프링에게 알림
@RequiredArgsConstructor  // Lombok: final로 선언된 모든 필드를 인자로 받는 생성자를 자동으로 만들어줌 (의존성 주입)
public class UserService
{
    // 의존성 주입 (생성자를 통해 스프링이 자동으로 해당 타입의 Bean을 넣어줌)
    private final UserRepository userRepository;                             // 사용자 정보 DB 접근용
    private final PasswordEncoder passwordEncoder;                           // 비밀번호 암호화/비교용 (PasswordConfig에서 등록한 Bean)
    private final PasswordResetTokenRepository passwordResetTokenRepository; // 비밀번호 재설정 토큰 DB 접근용
    private final JavaMailSender mailSender;                                 // 이메일 발송용 (스프링 부트 메일 스타터 설정 필요)

    // application.properties (또는 yml) 에서 설정값 주입
    // @Value 어노테이션: 설정 파일의 특정 키 값을 필드에 자동으로 주입해줌
    @Value("${app.reset-password.token.expiration-ms}") // 비밀번호 재설정 토큰의 유효 시간 (밀리초 단위)
    private long tokenExpirationMs;

    @Value("${app.reset-password.base-url}") // 비밀번호 재설정 이메일에 포함될 프론트엔드 페이지의 기본 URL
    private String resetBaseUrl;             // 예: http://localhost:8080 또는 https://내도메인.com

    /**
     * ✅ 회원가입 처리 (사용자 정보 저장)
     *
     * - 사용자가 입력한 비밀번호를 암호화한 후, UserRepository를 통해 DB에 저장
     *
     * @param user 저장할 사용자 정보 (컨트롤러에서 넘어온 User 객체)
     * @return DB에 저장된 User 객체 (ID 등이 자동 생성된 상태)
     */
    public User save(User user)
    {
        // 1. 사용자의 비밀번호를 PasswordEncoder(BCrypt)를 사용해 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 2. 암호화된 비밀번호가 포함된 사용자 정보를 UserRepository를 통해 DB에 저장합니다.
        return userRepository.save(user);
    }

    /**
     * ✅ 아이디(username) 중복 체크
     *
     * @param username 확인할 아이디 문자열
     * @return 해당 아이디가 이미 존재하면 true, 그렇지 않으면 false
     */
    public boolean existsByUsername(String username)
    {
        // UserRepository의 findByUsername 메서드는 Optional<User> 반환
        // isPresent()를 통해 해당 Optional 객체가 User 객체를 담고 있는지(즉, 아이디가 존재하는지) 확인
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * ✅ 이메일 중복 체크
     *
     * @param email 확인할 이메일 문자열
     * @return 해당 이메일이 이미 존재하면 true, 그렇지 않으면 false
     */
    public boolean existsByEmail(String email)
    {
        // 아이디 중복 체크와 동일한 원리
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * ✅ 로그인된 사용자 정보 가져오기 (세션 기반)
     * (실제로는 Controller에서 세션 객체를 직접 다루고, 필요시 이 메서드를 호출할 수 있다.
     *  또는, Spring Security를 사용한다면 SecurityContextHolder를 통해 더 쉽게 현재 사용자 정보를 가져올 수 있다.)
     *
     * @param session 현재 HTTP 세션 객체
     * @return 세션에 저장된 User 객체
     * @throws IllegalStateException 로그인된 사용자가 없을 경우 발생
     */
    public User getLoggedInUser(HttpSession session)
    {
        // 컨트롤러에서 "user"라는 키로 저장한 객체를 가져옴
        Object obj = session.getAttribute("user");

        // 가져온 객체가 User 타입인지 확인 후, 맞으면 User 객체로 반환
        if (obj instanceof User u) return u;

        // User 객체가 아니거나 없으면 예외 발생
        throw new IllegalStateException("로그인 사용자가 없습니다.");
    }

    /**
     * ✅ 사용자 인증 (로그인 시 아이디/비밀번호 검증)
     *
     * @param username 사용자가 입력한 아이디
     * @param rawPassword 사용자가 입력한 암호화되지 않은 평문 비밀번호
     * @return 인증 성공 시 해당 User 객체
     * @throws IllegalArgumentException 아이디가 존재하지 않거나 비밀번호가 일치하지 않을 경우 발생
     */
    public User validateUser(String username, String rawPassword)
    {
        // 1. 아이디로 사용자 정보를 DB에서 조회
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty())
        { // 해당 아이디의 사용자가 없으면
            throw new IllegalArgumentException("유저가 없습니다.");
        }

        // Optional에서 User 객체를 꺼냄
        User user = optionalUser.get();

        // 2. 입력된 평문 비밀번호(rawPassword)와 DB에 저장된 암호화된 비밀번호(user.getPassword()) 비교
        //    passwordEncoder.matches() 메서드가 이 비교 과정을 안전하게 처리
        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
        { // 비밀번호가 일치하지 않으면
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        return user; // 모든 검증 통과! 해당 사용자 객체 반환
    }

    /**
     * ✅ 사용자 ID(Primary Key)로 사용자 정보 조회
     *
     * @param id 조회할 사용자의 ID
     * @return 해당 ID의 User 객체, 없으면 null 반환
     */
    public User getUserById(Long id)
    {
        // orElse(null)은 Optional이 비어있을 경우 null을 반환
        return userRepository.findById(id).orElse(null);
    }

    /**
     * ✅ 이메일 주소로 사용자 아이디(username) 찾기 및 마스킹 처리
     *
     * @param email 아이디를 찾기 위한 이메일 주소
     * @return FindIdResponse 객체 (성공 여부, 메시지, 마스킹된 아이디 포함)
     */
    public FindIdResponse findUserIdByEmail(String email)
    {
        // 이메일로 사용자 조회
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent())
        { // 해당 이메일로 가입된 사용자가 있다면
            User user = userOptional.get();
            // User 엔티티의 getUsername() 메소드가 로그인 아이디를 반환한다고 가정합니다.
            String userId = user.getUsername();   // 사용자의 실제 아이디(username) 가져오기
            String maskedId = maskUserId(userId); // 아이디 마스킹 처리
            return new FindIdResponse(true, "아이디를 찾았습니다.", maskedId);
        } else
        { // 해당 이메일로 가입된 사용자가 없다면
            return new FindIdResponse(false, "해당 이메일로 가입된 아이디를 찾을 수 없습니다.", null);
        }
    }

    /**
     * 🔒 (private) 사용자 아이디 마스킹 처리 유틸리티 메서드
     * - 예: "testuser" -> "tes***er" (정책에 따라 변경 가능)
     * - 이 클래스 내부에서만 사용되므로 private으로 선언
     *
     * @param userId 마스킹할 원본 아이디 문자열
     * @return 마스킹 처리된 아이디 문자열
     */
    private String maskUserId(String userId)
    {
        if (userId == null || userId.isEmpty())
        {
            return ""; // 빈 문자열이나 null이면 그대로 반환
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
            int visibleStart = 3; // 앞에서 보여줄 글자 수
            int visibleEnd = 2;   // 뒤에서 보여줄 글자 수
            return userId.substring(0, visibleStart) +
                    "*".repeat(Math.max(0, length - visibleStart - visibleEnd)) +
                    userId.substring(length - visibleEnd);
        }
    }

    /**
     * 🔑 비밀번호 재설정 요청 처리 (토큰 생성 및 이메일 발송)
     *
     * - @Transactional: 이 메서드 내의 DB 작업(토큰 저장 등)들이 하나의 단위로 처리, 오류 발생 시 모든 작업이 롤백(원상복구)
     *
     * @param request 사용자의 아이디(username)와 이메일을 담은 PasswordResetRequest DTO
     * @throws IllegalArgumentException 아이디 또는 이메일이 누락되었거나, DB에서 해당 사용자를 찾을 수 없을 경우 (정책에 따라)
     * (여기서는 사용자를 못찾아도 예외를 발생시키지 않고 조용히 넘어갑니다. - 컨트롤러에서 일관된 메시지 반환 위함)
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        // PasswordResetRequest의 getUserId()가 실제로는 사용자의 로그인 ID(username)를 의미한다고 가정
        String username = request.getUserId(); // DTO에서 아이디(username) 가져오기
        String email = request.getEmail();     // DTO에서 이메일 가져오기
        // 프론트엔드에서 입력한 값이 정확히 넘어왔는지 확인
        System.out.println("[DEBUG] Attempting password reset for username: " + username + ", email: " + email);

        // 1. 아이디 또는 이메일이 비어있는지 유효성 검사 (Controller에서도 했지만 Service에서도 중요!)
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("아이디와 이메일을 모두 입력해주세요.");
        }

        // 2. DB에서 해당 아이디와 이메일로 사용자 조회
        //    UserRepository에 findByUsernameAndEmail(String username, String email) 메서드가 정의되어 있어야 한다.
        Optional<User> userOptional = userRepository.findByUsernameAndEmail(username, email);

        System.out.println("[DEBUG] 아이디/이메일로 사용자 조회 결과: " + (userOptional.isPresent() ? "찾음" : "못찾음"));

        if (userOptional.isPresent())
        { // 사용자를 찾은 경우에만 토큰 생성 및 이메일 발송 진행
            User user = userOptional.get();
            System.out.println("[DEBUG] 처리 대상 사용자 - DB ID: " + user.getId() + ", DB Username: "
                                + user.getUsername() + ", DB Email: " + user.getEmail());

            // 3. 비밀번호 재설정 토큰 생성 (UUID 사용으로 고유성 보장)
            String token = UUID.randomUUID().toString();
            // 4. 토큰 만료 날짜 계산 (현재시간 + 설정된 유효시간)
            Date expiryDate = new Date(System.currentTimeMillis() + tokenExpirationMs);

            // 5. PasswordResetTokenEntity 객체 생성 및 정보 설정
            PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
            resetToken.setToken(token);
            resetToken.setUser(user);             // 토큰과 사용자 연관 짓기 (JPA 연관관계)
            resetToken.setExpiryDate(expiryDate);
            resetToken.setUsed(false);            // 생성 시 기본적으로 '미사용' 상태

            // 6. 생성된 토큰을 DB에 저장
            passwordResetTokenRepository.save(resetToken);
            // 이 로그가 출력되면 토큰 생성 및 DB 저장까지는 성공
            System.out.println("[DEBUG] 비밀번호 재설정 토큰 생성 및 저장 완료. 토큰: " + token + ". 이메일 발송 시도...");

            // 7. 사용자 이메일로 재설정 링크(토큰 포함) 발송
            sendPasswordResetEmail(user.getEmail(), token);
        }else
        {
            // 사용자를 찾지 못한 경우 (아이디/이메일 불일치 또는 존재하지 않는 사용자)
            // 보안상, 프론트엔드에는 일관된 성공 메시지를 반환하므로 여기서는 별도 예외를 발생시키지 않거나,
            // 내부 로깅만 수행할 수 있습니다. (Controller에서 이미 그렇게 처리하고 있음)
            System.out.println("[DEBUG] 요청된 아이디(" + username + ")와 이메일(" + email + ")에 해당하는 사용자를 찾지 못했습니다. (이메일 미발송)");
        }
    }

    /**
     * 📧 (private) 비밀번호 재설정 이메일 발송 로직
     *
     * @param toEmail 수신자 이메일 주소
     * @param token 생성된 비밀번호 재설정 토큰
     */
    private void sendPasswordResetEmail(String toEmail, String token)
    {
        System.out.println("[DEBUG] 이메일 발송 메서드 시작. 수신자: " + toEmail + ", 토큰: " + token);
        System.out.println("[DEBUG] 이메일 발송 시 사용될 resetBaseUrl: " + resetBaseUrl); // 설정값 확인

        String subject = "[나의 일기방] 비밀번호 재설정 안내"; // TODO: "[내 서비스 이름]"을 실제 서비스명으로 변경하세요.
        // 프론트엔드의 비밀번호 재설정 페이지 URL + 토큰 (예: http://localhost:8080/reset_password.html?token=xxxxx)
        String resetPageUrl = resetBaseUrl + "/reset_password.html?token=" + token; // 수정된 부분
        String messageText = "안녕하세요.\n\n" +
                "비밀번호를 재설정하려면 아래 링크를 클릭하세요. (링크는 " + (tokenExpirationMs / (1000 * 60)) + "분 동안 유효합니다.):\n" +
                resetPageUrl + "\n\n" +
                "만약 비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시해주세요.\n\n" +
                "감사합니다.";

        // SimpleMailMessage: 간단한 텍스트 기반 이메일
        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(toEmail);        // 받는 사람
        emailMessage.setSubject(subject);   // 제목
        emailMessage.setText(messageText);  // 내용
        // emailMessage.setFrom("noreply@mydomain.com"); // (필요시 발신자 주소 설정, 보통은 properties에 설정)

        try
        {
            mailSender.send(emailMessage); // 메일 발송!
            System.out.println("[DEBUG] 비밀번호 재설정 이메일이 " + toEmail + " 주소로 성공적으로 발송되었습니다.");
        } catch (MailException e)
        {
            // 이메일 발송 실패 시 (네트워크 문제, 메일 서버 설정 오류 등)
            System.err.println("[ERROR] " + toEmail + " 주소로 비밀번호 재설정 이메일 발송 중 오류 발생. 토큰: " + token + ". 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔒 새 비밀번호로 사용자의 비밀번호를 실제 재설정 (업데이트)
     *
     * - @Transactional: 사용자 비밀번호 변경과 토큰 상태 변경 작업을 하나의 트랜잭션으로 처리
     *
     * @param request ResetPasswordRequest DTO (유효한 토큰, 새 비밀번호 포함)
     * @throws IllegalArgumentException 토큰이 유효하지 않거나(존재X, 만료, 이미 사용됨), 새 비밀번호가 정책에 맞지 않는 경우
     * @throws IllegalStateException 토큰은 유효하나 연결된 사용자가 없는 경우 (데이터 무결성 문제)
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request)
    {
        String token = request.getToken();
        String newPassword = request.getNewPassword();

        // 1. 토큰 유효성 검증
        // 1-1. DB에서 토큰 정보 조회
        PasswordResetTokenEntity passwordResetToken =
                passwordResetTokenRepository.findByToken(token) // 토큰 문자열로 DB에서 엔티티 찾기
                        .orElseThrow(() -> {
                            // 토큰이 존재하지 않으면 예외 발생
                            System.err.println("[ERROR] Invalid token received for password reset: " + token);
                            return new IllegalArgumentException("제공된 토큰이 유효하지 않습니다. 링크를 다시 확인해주세요.");
                        });

        // 1-2. 토큰이 이미 사용되었는지 검증
        if (passwordResetToken.isUsed())
        {
            System.err.println("[ERROR] 비밀번호 재설정 시도: 이미 사용된 토큰 - " + token);
            throw new IllegalArgumentException("이미 사용된 토큰입니다. 비밀번호 재설정을 다시 요청해주세요.");
        }

        // 1-3. 토큰이 만료되었는지 검증
        if (passwordResetToken.isTokenExpired())
        { // PasswordResetTokenEntity에 isTokenExpired() 메서드 구현 필요
            System.err.println("[ERROR] Expired token received: " + token);
            // (선택적) 만료된 토큰은 DB에서 삭제하거나, isUsed를 true로 변경하여 재사용을 막는 정책 고려 가능
            // 예: passwordResetTokenRepository.delete(passwordResetToken);
            throw new IllegalArgumentException("토큰이 만료되었습니다. 비밀번호 재설정을 다시 요청해주세요.");
        }

        // (권장) 백엔드에서의 새 비밀번호 정책 검증 (DTO의 @Size, @Pattern 등과 별개로 서비스 로직에서도 검증)
        //       ResetPasswordRequest DTO에 @Size(min=8, max=20) 등이 이미 적용되어 @Valid로 컨트롤러에서 1차 검증되지만,
        //       여기서 한 번 더 명시적으로 검증하거나, 더 복잡한 정책(대소문자, 숫자, 특수문자 조합 등)을 적용할 수 있습니다.
        if (newPassword == null || newPassword.trim().length() < 8)
        { // 예시: 최소 8자 (DTO에서 이미 검증될 수 있음)
            System.err.println("[ERROR] 새 비밀번호가 정책(최소 8자)에 맞지 않습니다. 입력된 비밀번호 길이: " + (newPassword != null ? newPassword.length() : "null"));
            throw new IllegalArgumentException("새 비밀번호는 최소 8자 이상이어야 합니다.");
        }
        // (TODO) 필요하다면 여기에 더 구체적인 비밀번호 복잡도 검증 로직 추가


        // 2. 토큰이 유효하면, 해당 토큰과 연결된 사용자 정보를 가져온다.
        User user = passwordResetToken.getUser();
        if (user == null)
        { // 데이터 무결성 문제 (토큰은 있는데 사용자가 없는 경우)
            System.err.println("[CRITICAL ERROR] 유효한 토큰(" + token + ")에 연결된 사용자 정보를 찾을 수 없습니다!");
            throw new IllegalStateException("토큰과 연결된 사용자 정보를 찾을 수 없습니다. 관리자에게 문의하세요.");
        }
        System.out.println("[DEBUG] 토큰 검증 통과. 사용자 정보 확인 - ID: " + user.getId() + ", Username: " + user.getUsername());

        // 3. 새 비밀번호를 암호화하여 사용자 정보에 업데이트
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user); // 변경된 사용자 정보(새 비밀번호)를 DB에 저장
        System.out.println("[DEBUG] 사용자(ID: " + user.getId() + ")의 비밀번호가 성공적으로 업데이트되었습니다.");

        // 4. 사용된 토큰을 '사용됨(isUsed=true)' 상태로 변경하여 재사용을 방지
        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken); // 변경된 토큰 상태를 DB에 저장
        System.out.println("[DEBUG] 비밀번호 재설정에 사용된 토큰(" + token + ")을 '사용됨'으로 표시했습니다.");

        System.out.println("[INFO] 사용자(" + user.getUsername() + ")의 비밀번호 재설정이 성공적으로 완료되었습니다.");
    }
}
