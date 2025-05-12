package com.diary.mydiary.controller;

import com.diary.mydiary.dto.LoginRequest;
import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 👤 사용자 관련 요청을 처리하는 컨트롤러
 *
 * - 회원가입, 로그인, 로그아웃 등 사용자 인증 기능을 제공합니다.
 * - 세션을 사용해 로그인 상태를 관리합니다.
 */
@RestController
@RequestMapping("/users") // 모든 URL 앞에 /users 가 붙음
@RequiredArgsConstructor    // 생성자 주입 자동 생성
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 📝 회원가입 처리
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ 이미 사용 중인 아이디입니다.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("✅ 회원가입 완료");
    }

    /**
     * 🔐 로그인 (세션 기반)
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req, HttpSession session) {
        Optional<User> opt = userRepository.findByUsername(req.getUsername());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 유저가 없습니다.");
        }

        User user = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 비밀번호 오류");
        }

        // ✅ 세션에 사용자 저장 (User 객체)
        session.setAttribute("user", user);

        // ✅ 세션에 사용자 ID도 따로 저장
        session.setAttribute("userId", user.getId());

        return ResponseEntity.ok("✅ 로그인 성공 (세션 저장)");
    }

    /**
     * 🙋 로그인 상태 확인
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 로그인되어 있지 않습니다.");
        }

        return ResponseEntity.ok(user);
    }

    /**
     * 🚪 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 완료");
    }
}
