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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** ✅ 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        // 아이디 중복 검사
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ 이미 사용 중인 아이디입니다.");
        }

        // 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("✅ 회원가입 완료");
    }

    /** ✅ 로그인 (세션 기반) */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req,
                                        HttpSession session) {

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

        session.setAttribute("loggedInUser", user);
        return ResponseEntity.ok("✅ 로그인 성공 (세션 저장)");
    }

    /** ✅ 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 완료");
    }
}
