package com.diary.mydiary.controller;

// DTO (Data Transfer Object) 클래스들 - 데이터 전송용 상자
import com.diary.mydiary.dto.FindIdRequest;
import com.diary.mydiary.dto.FindIdResponse;
import com.diary.mydiary.dto.LoginRequest;
import com.diary.mydiary.dto.PasswordResetRequest; // (비밀번호 재설정 요청 시 아이디, 이메일 담는 용도)
import com.diary.mydiary.dto.ApiResponse;          // (API 공통 응답 형식)
import com.diary.mydiary.dto.ResetPasswordRequest; // (실제 새 비밀번호와 토큰 담는 용도)

import com.diary.mydiary.model.User;              // (DB의 사용자 정보 모양)
import org.springframework.util.StringUtils;      // (문자열이 비었는지 등을 쉽게 확인하는 유틸리티)
import com.diary.mydiary.service.UserService;     // (사용자 관련 핵심 로직 처리)
import jakarta.servlet.http.HttpSession;          // (로그인 상태 유지를 위한 세션)
import jakarta.validation.Valid;                  // (@Valid 어노테이션으로 DTO 유효성 검사)
import lombok.RequiredArgsConstructor;            // (final 필드에 대한 생성자 자동 생성 - Lombok)
import org.springframework.http.HttpStatus;       // (HTTP 상태 코드)
import org.springframework.http.ResponseEntity;   // (HTTP 응답 제어)
import org.springframework.web.bind.annotation.*; // (URL 매핑 어노테이션)

/**
 * 👤 사용자 관련 요청을 처리하는 컨트롤러 (회원 관리 매니저)
 * - 역할: 회원가입, 로그인, 로그아웃, 아이디/비밀번호 찾기 등 사용자 인증 및 관리 기능을 담당
 * - 세션 사용: HttpSession을 통해 로그인 상태를 관리
 * - 공통 URL 접두사: 모든 API 경로는 "/api/users"로 시작 (예: /api/users/login)
 */
@RestController // API 컨트롤러지만, 주로 JSON 같은 데이터를 다룸
@RequestMapping("/api/users") // 이 컨트롤러의 모든 메서드 URL 앞에 "/api/users"가 붙음
@RequiredArgsConstructor    // private final 필드만 받는 생성자 자동 생성 (의존성 주입 편하게!)
public class UserController
{
    private final UserService userService; // 사용자 관련 실제 작업은 UserService에게 위임!

    /**
     * 📝 [POST /api/users/signup] 회원가입 처리 API
     * - 사용자가 입력한 정보를 받아 회원가입 진행
     * - 서버 측에서 아이디(username) 및 이메일의 필수 입력 여부와 중복 여부 검증
     *  (프론트엔드에서도 검증하겠지만, 보안을 위해 서버에서도 반드시 다시 검증해야 함!)
     * @param user 사용자 정보 (요청 본문의 JSON 데이터를 User 객체로 변환)
     * (개선점: 회원가입 전용 DTO (예: UserSignupRequestDto)를 사용하는 것이 더 좋음
     * User 엔티티에는 DB 자동 생성 ID나 암호화된 비밀번호 등이 포함될 수 있는데,클라이언트가 이런 값을 보내면 안 되기 때문이다.)
     * @return 성공 시: "회원가입 완료" 메시지와 HTTP 200 OK
     * 실패 시 (입력값 오류): 해당 오류 메시지와 HTTP 400 Bad Request
     * 실패 시 (중복): 해당 오류 메시지와 HTTP 409 Conflict
     * 실패 시 (서버 내부 오류): 에러 메시지와 HTTP 500 Internal Server Error
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user)
    {
        // 0. 서버 측 필수 입력 값 검증 (프론트에서 했더라도 한번 더!)
        if (!StringUtils.hasText(user.getUsername()))
        { // StringUtils.hasText는 null, "", " " 모두 false 처리
            return ResponseEntity.badRequest().body("❌ 아이디를 입력해주세요.");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            return ResponseEntity.badRequest().body("❌ 이메일을 입력해주세요.");
        }
        // (추가 검증) 비밀번호 길이, 형식 등도 여기서 검증할 수 있다
        // 또는 User 모델이나 DTO에 @NotBlank, @Email, @Size 같은 JPA Validation 어노테이션을 쓰고
        // 컨트롤러 메서드 파라미터에 @Valid User user 와 같이 사용하면 자동으로 검증해준다.

        // 1. 아이디(username) 중복 체크 - UserService에게 물어봄
        if (userService.existsByUsername(user.getUsername()))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT) // 409 Conflict: 이미 리소스가 존재함
                    .body("❌ 이미 사용 중인 아이디입니다.");
        }

        // 2. 이메일 중복 체크 - UserService에게 물어봄
        if (userService.existsByEmail(user.getEmail()))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ 이미 사용 중인 이메일입니다.");
        }

        // 3. 유효성 검사 통과 시 사용자 정보 저장 요청
        try {
            userService.save(user); // UserService를 통해 비밀번호 암호화 후 DB에 저장
            return ResponseEntity.ok("✅ 회원가입 완료"); // 성공!
        } catch (Exception e)
        { // 예상치 못한 오류 발생 시 (DB 연결 문제 등)
            System.err.println("회원가입 처리 중 오류 발생: " + e.getMessage()); // 서버 로그에 에러 기록
            e.printStackTrace(); // (개발 중에만 사용, 운영에서는 로깅 프레임워크 사용)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ 회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 🔐 [POST /api/users/login] 로그인 처리 API (세션 기반)
     *
     * - 사용자가 입력한 아이디와 비밀번호로 로그인 시도
     * - 성공 시, 사용자 정보를 세션에 저장하여 로그인 상태 유지.
     *
     * @param req 로그인 요청 정보 (아이디, 비밀번호)를 담은 DTO (JSON 요청 본문)
     * @param session 현재 사용자의 세션 객체 (스프링이 주입)
     * @return 성공 시: "로그인 성공 (세션 저장)" 메시지와 HTTP 200 OK
     * 실패 시 (인증 오류): 에러 메시지(예: "유저가 없습니다.", "비밀번호가 틀렸습니다.")와 HTTP 401 Unauthorized
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req, HttpSession session)
    {
        try
        {
            // 1. UserService에게 아이디, 비밀번호 검증 요청
            User user = userService.validateUser(req.getUsername(), req.getPassword()); // 서비스 사용

            // 2. 인증 성공! 사용자 정보를 세션에 저장 (이 정보로 로그인 상태 유지)
            session.setAttribute("user", user);             // User 객체 전체를 "user"라는 이름으로 세션에 저장
            session.setAttribute("userId", user.getId());   // 사용자의 고유 ID도 "userId"라는 이름으로 세션에 저장 (일기 작성/조회 등에 활용)
                                                               // (세션 만료 시간은 기본적으로 WAS(톰캣 등) 설정을 따르거나, application.properties에서 설정 가능)
            return ResponseEntity.ok("✅ 로그인 성공 (세션 저장)");
        } catch (IllegalArgumentException e)
        { // userService.validateUser에서 던진 예외 (아이디 없거나, 비번 틀림)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ " + e.getMessage());
        }
    }

    /**
     * 🙋 [GET /api/users/me] 현재 로그인된 사용자 정보 조회 API
     *
     * - 세션에 저장된 사용자 정보를 바탕으로 현재 누가 로그인되어 있는지 알려준다.
     * - 프론트엔드에서 페이지 로드 시 또는 특정 기능 사용 전에 로그인 상태를 확인할 때 사용
     *
     * @param session 현재 사용자의 세션 객체
     * @return 성공 시 (로그인 됨): 사용자 정보(User 객체)와 HTTP 200 OK
     * 실패 시 (미로그인): "로그인되어 있지 않습니다." 메시지와 HTTP 401 Unauthorized
     */
    @GetMapping("/me") // "/api/users/me" 경로로 GET 요청이 오면 실행
    public ResponseEntity<?> getCurrentUser(HttpSession session)
    {
        User user = (User) session.getAttribute("user"); // 세션에서 "user" 속성 값(로그인 시 저장한 User 객체)을 가져옴

        if (user == null)
        { // 세션에 "user" 정보가 없으면 로그인 안 된 상태
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 로그인되어 있지 않습니다.");
        }

        return ResponseEntity.ok(user);
    }

    /**
     * 🚪 [POST /api/users/logout] 로그아웃 처리 API
     *
     * - 현재 사용자의 세션을 무효화하여 로그아웃 처리
     *
     * @param session 현재 사용자의 세션 객체
     * @return "로그아웃 완료" 메시지와 HTTP 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session)
    {
        session.invalidate(); // 세션의 모든 데이터 삭제 및 세션 무효화!
        return ResponseEntity.ok("로그아웃 완료");
    }

    /**
     * 🔍 [POST /api/users/find-id] 아이디 찾기 API
     *
     * - 사용자가 입력한 이메일 주소를 받아, 해당 이메일로 가입된 사용자의 아이디(username)를 찾아서 일부 마스킹 처리 후 반환
     *
     * @param findIdRequest 아이디를 찾기 위한 이메일 정보를 담은 DTO (JSON 요청 본문)
     * @return 성공/실패 여부, 메시지, (성공 시) 마스킹된 아이디를 담은 FindIdResponse 객체
     * (이메일 누락 시 HTTP 400, 서버 오류 시 HTTP 500)
     */
    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> handleFindId(@RequestBody FindIdRequest findIdRequest)
    {
        // 1. 요청 DTO나 이메일 값이 비어있는지 간단히 확인
        if (findIdRequest == null || findIdRequest.getEmail() == null || findIdRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new FindIdResponse(false, "이메일을 입력해주세요.", null));
        }

        try
        {
            // 2. UserService에게 이메일로 아이디 찾기 요청
            FindIdResponse response = userService.findUserIdByEmail(findIdRequest.getEmail());
            return ResponseEntity.ok(response); // 서비스의 처리 결과를 그대로 반환
        } catch (Exception e)
        {
            // 예상치 못한 서버 내부 오류
            System.err.println("아이디 찾기 중 서버 오류 발생: " + e.getMessage());
            e.printStackTrace(); // 개발 중에만 사용
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FindIdResponse(false, "아이디 찾기 처리 중 서버 오류가 발생했습니다.", null));
        }
    }

    /**
     * 🔑 [POST /api/users/request-password-reset] 비밀번호 재설정 요청 API
     *
     * - 사용자의 아이디(username)와 이메일을 받아, DB 정보와 일치하는 경우 비밀번호를 재설정할 수 있는 링크(토큰 포함)를 이메일로 발송
     * - 보안을 위해, 실제 사용자 존재 여부나 메일 발송 성공 여부와 관계없이 프론트엔드에는 일관된 긍정 메시지를 반환하는 것이 일반적
     *
     * @param request 사용자의 아이디(여기서는 DTO 필드명이 userId로 되어있지만 실제로는 username)와 이메일을 담은 DTO
     * @return 성공/실패 여부와 메시지를 담은 ApiResponse 객체
     * (입력값 누락 등 요청 오류 시 HTTP 400, 서버 오류 시 HTTP 500)
     */
    @PostMapping("/request-password-reset") // API 경로 및 HTTP 메소드 매핑
    public ResponseEntity<ApiResponse> requestPasswordReset(@RequestBody PasswordResetRequest request)
    {
        try
        {
            // 1. UserService에게 비밀번호 재설정 요청 처리 위임
            //    (내부적으로 사용자 검증, 토큰 생성, 이메일 발송 등을 수행)
            userService.requestPasswordReset(request);

            // 2. 실제 처리 결과와 관계없이, 사용자에게는 일관된 안내 메시지 반환 (보안적 측면)
            //    (악의적인 사용자가 이 API로 특정 아이디/이메일 조합의 존재 유무를 추측하는 것을 방지)
            return ResponseEntity.ok(new ApiResponse(true, "비밀번호 재설정 안내 메일을 발송했습니다. 메일함을 확인해주세요."));
        } catch (IllegalArgumentException e)
        {
            // 서비스 로직에서 유효성 검사 실패 등 (예: 아이디/이메일 누락)으로 발생한 예외 처리
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e)
        {
            // 그 외 예측하지 못한 서버 내부 오류
            System.err.println("비밀번호 재설정 요청 처리 중 서버 오류 발생: " + e.getMessage());
            e.printStackTrace(); // 개발 중 상세 오류 확인을 위해 스택 트레이스 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }

    /**
     * 🔄 [POST /api/users/reset-password] 새 비밀번호로 실제 재설정 처리 API
     *
     * - 사용자가 이메일로 받은 재설정 토큰과 새로 사용할 비밀번호를 받아 실제로 사용자의 비밀번호를 변경.
     * - @Valid 어노테이션: ResetPasswordRequest DTO에 정의된 유효성 검사 규칙(예: @NotBlank, @Size)을 적용
     *   만약 규칙 위반 시 MethodArgumentNotValidException 발생 (보통 @ControllerAdvice에서 처리)
     *
     * @param request 유효한 토큰과 새 비밀번호를 담은 DTO
     * @return 성공/실패 여부와 메시지를 담은 ApiResponse 객체
     * (토큰/비밀번호 오류 시 HTTP 400, 서버 오류 시 HTTP 500)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request)
    {
        try
        {
            // 1. UserService에게 토큰 검증 및 비밀번호 변경 처리 위임
            userService.resetPassword(request);
            return ResponseEntity.ok(new ApiResponse(true, "비밀번호가 성공적으로 변경되었습니다."));
        }
        catch (IllegalArgumentException e)
        { // UserService에서 발생시킨 예외 (유효하지 않거나 만료된 토큰, 비밀번호 정책 위반 등)
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
        catch (Exception e)
        { // 그 외 서버 내부 오류
            // log.error("Error resetting password: ", e); // 로깅 프레임워크 사용 권장
            System.err.println("실제 비밀번호 재설정 중 서버 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "비밀번호 변경 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }

    /**
     * ✅ [GET /api/users/check-username/{username}] 아이디(username) 중복 실시간 체크 API
     *
     * - 회원가입 폼에서 사용자가 아이디를 입력할 때, 해당 아이디가 이미 사용 중인지 실시간으로 확인하여 즉각적인 피드백을 제공하기 위한 API
     * - (주의) 이 API는 사용자 편의성을 위한 보조적인 기능이며,
     *    최종적인 아이디 중복 여부는 회원가입 요청(/api/users/signup) 시 서버에서 다시 한번 검증한다.
     *
     * @param username 확인할 아이디 (URL 경로에서 추출)
     * @return 중복 여부에 따른 메시지와 HTTP 상태 코드 (사용 가능: 200 OK, 중복: 409 Conflict, 형식 오류: 400 Bad Request)
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<String> checkUsername(@PathVariable String username)
    {
        if (!StringUtils.hasText(username) || username.length() < 4)
        {
            // 1. 아이디 유효성 검사 (예: 최소 길이 4자)
            return ResponseEntity.badRequest().body("아이디는 4자 이상 입력해주세요.");
        }
        // 2. UserService를 통해 아이디 중복 여부 확인
        boolean exists = userService.existsByUsername(username);
        if (exists)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다."); // 409 Conflict
        }
        return ResponseEntity.ok("사용 가능한 아이디입니다."); // 200 OK
    }

    /**
     * ✅ [GET /api/users/check-email] 이메일 중복 실시간 체크 API
     *
     * - 회원가입 폼에서 사용자가 이메일을 입력할 때, 해당 이메일이 이미 사용 중인지 실시간으로 확인하기 위한 API
     * - 아이디 중복 체크 API와 마찬가지로 사용자 편의성을 위한 보조 기능
     *
     * @param email 확인할 이메일 주소 (URL 쿼리 파라미터 ?email=... 로 받음)
     * @return 중복 여부에 따른 메시지와 HTTP 상태 코드
     */
    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam String email)
    {
        // 1. 간단한 이메일 형식 검사 (더 정교한 검증은 정규식 사용 고려)
        if (!StringUtils.hasText(email) || !email.contains("@") || email.length() < 5)
        {
            return ResponseEntity.badRequest().body("유효한 이메일 형식이 아닙니다.");
        }
        // 2. UserService를 통해 이메일 중복 여부 확인
        boolean exists = userService.existsByEmail(email);
        if (exists)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");
        }
        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }
}
