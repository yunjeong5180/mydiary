package com.diary.mydiary.controller;

import com.diary.mydiary.dto.FindIdRequest;
import com.diary.mydiary.dto.FindIdResponse;
import com.diary.mydiary.dto.LoginRequest;
import com.diary.mydiary.model.User; // User 모델을 @RequestBody로 직접 받는 경우
import org.springframework.util.StringUtils; // StringUtils.hasText 사용을 위해 추가
import com.diary.mydiary.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

// import java.util.Optional;

/**
 * 👤 사용자 관련 요청을 처리하는 컨트롤러
 *
 * - 회원가입, 로그인, 로그아웃 등 사용자 인증 기능을 제공합니다.
 * - 세션을 사용해 로그인 상태를 관리합니다.
 */
@RestController
@RequestMapping("/api/users") // 모든 URL 앞에 /api/users 가 붙음
@RequiredArgsConstructor    // 생성자 주입 자동 생성
public class UserController
{

    private final UserService userService;
    // private final PasswordEncoder passwordEncoder;
    // UserService의 save 메소드에서 암호화를 담당하므로,
    // UserController에 직접 PasswordEncoder 의존성이 필요 없을 수 있습니다.
    // 만약 다른 곳에서 여전히 사용한다면 유지합니다.
    // 여기서는 UserService.save()에서 암호화한다고 가정하고 주석 처리합니다.

    /**
     * 📝 회원가입 API
     * 데이터의 최종적인 유효성과 정합성을 보장하기 위한 필수적인 서버 측 검증
     * - 아이디(username) 및 이메일 필수 입력 및 중복 체크 추가
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) { // 또는 UserRegistrationRequest DTO 사용
        // 0. 필수 입력 값 검증 (username, email, password 등)
        if (!StringUtils.hasText(user.getUsername())) { // StringUtils.hasText는 null, "", " " 모두 false 처리
            return ResponseEntity.badRequest().body("❌ 아이디를 입력해주세요.");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            return ResponseEntity.badRequest().body("❌ 이메일을 입력해주세요.");
        }
        // 비밀번호에 대한 검증도 필요하다면 여기에 추가 (예: 길이, 복잡도 등)
        // User 모델 또는 DTO에 @NotBlank, @Email 등의 어노테이션을 사용하고
        // 컨트롤러 메소드 파라미터에 @Valid 를 붙여 검증하는 방법도 좋습니다.

        // 1. 아이디(username) 중복 체크
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ 이미 사용 중인 아이디입니다.");
        }

        // 2. 이메일 중복 체크
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ 이미 사용 중인 이메일입니다.");
        }

        // 유효성 검사 통과 시 사용자 저장
        try {
            userService.save(user);
            return ResponseEntity.ok("✅ 회원가입 완료");
        } catch (Exception e) {
            System.err.println("회원가입 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ 회원가입 중 오류가 발생했습니다.");
        }
    }

    /**
     * 🔐 로그인 (세션 기반)
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req, HttpSession session)
    {
        try
        {
            User user = userService.validateUser(req.getUsername(), req.getPassword()); // 서비스 사용
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            return ResponseEntity.ok("✅ 로그인 성공 (세션 저장)");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ " + e.getMessage());
        }
    }

    /**
     * 🙋 로그인 상태 확인
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session)
    {
        User user = (User) session.getAttribute("user");

        if (user == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 로그인되어 있지 않습니다.");
        }

        return ResponseEntity.ok(user);
    }

    /**
     * 🚪 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session)
    {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 완료");
    }

    // --- 아이디 찾기 API 엔드포인트 추가 ---
    /**
     * 🔍 아이디 찾기
     * - 이메일을 받아 아이디(username)를 찾아 마스킹하여 반환합니다.
     */
    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> handleFindId(@RequestBody FindIdRequest findIdRequest) {
        if (findIdRequest == null || findIdRequest.getEmail() == null || findIdRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new FindIdResponse(false, "이메일을 입력해주세요.", null));
        }

        try {
            FindIdResponse response = userService.findUserIdByEmail(findIdRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실제 운영 환경에서는 로깅 프레임워크 사용 권장 (예: SLF4J)
            System.err.println("아이디 찾기 중 서버 오류 발생: " + e.getMessage());
            e.printStackTrace(); // 개발 중에만 사용
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FindIdResponse(false, "아이디 찾기 처리 중 서버 오류가 발생했습니다.", null));
        }
    }

    // --- 실시간 중복 체크 API  ---
    // 사용자 편의성을 위한 보조적인 기능이며, 즉각적인 피드백을 제공
    /**
     * ✅ 아이디(username) 중복 실시간 체크
     * @param username 확인할 아이디
     * @return 중복 여부 및 메시지를 담은 ResponseEntity
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<String> checkUsername(@PathVariable String username)
    {
        if (!StringUtils.hasText(username) || username.length() < 4)
        { // 아이디 유효성 검사 (예: 최소 길이 4자)
            return ResponseEntity.badRequest().body("아이디는 4자 이상 입력해주세요.");
        }
        boolean exists = userService.existsByUsername(username);
        if (exists)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다.");
        }
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    /**
     * ✅ 이메일 중복 실시간 체크
     * @param email 확인할 이메일 주소
     * @return 중복 여부 및 메시지를 담은 ResponseEntity
     */
    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam String email)
    {
        // 간단한 이메일 형식 검사 (더 정교한 검증은 정규식 사용 고려)
        if (!StringUtils.hasText(email) || !email.contains("@") || email.length() < 5)
        {
            return ResponseEntity.badRequest().body("유효한 이메일 형식이 아닙니다.");
        }
        boolean exists = userService.existsByEmail(email);
        if (exists)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");
        }
        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }
}
