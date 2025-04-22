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
@RequiredArgsConstructor    // 생성자 주입을 자동으로 생성
public class UserController
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 📝 회원가입 처리
     *
     * - 중복 아이디가 있는지 확인
     * - 비밀번호를 암호화한 후 사용자 정보를 저장
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user)
    {
        // 1. 아이디 중복 검사
        if (userRepository.findByUsername(user.getUsername()).isPresent())
        {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ 이미 사용 중인 아이디입니다.");
        }

        // 2. 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("✅ 회원가입 완료");
    }

    /**
     * 🔐 로그인 (세션 기반)
     *
     * - 아이디와 비밀번호를 검사하고, 성공 시 세션에 사용자 정보 저장
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req,
                                        HttpSession session)
    {
        // 1. 아이디로 사용자 검색
        Optional<User> opt = userRepository.findByUsername(req.getUsername());
        if (opt.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 유저가 없습니다.");
        }

        // 2. 비밀번호 확인
        User user = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 비밀번호 오류");
        }

        // 3. 로그인 성공 → 세션에 사용자 정보 저장
        session.setAttribute("loggedInUser", user);
        return ResponseEntity.ok("✅ 로그인 성공 (세션 저장)");
    }

    /**
     * 🚪 로그아웃 처리
     *
     * - 세션을 초기화하여 로그인 상태 해제
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session)
    {
        session.invalidate(); // 세션 초기화
        return ResponseEntity.ok("로그아웃 완료");
    }
}
