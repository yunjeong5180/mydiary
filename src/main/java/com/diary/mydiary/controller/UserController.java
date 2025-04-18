package com.diary.mydiary.controller;

import com.diary.mydiary.dto.LoginRequest;
import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController
{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ 회원가입
    @PostMapping("/signup")
    public User registerUser(@RequestBody User user)
    {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 비밀번호 암호화
        return userRepository.save(user);
    }

    // ✅ 로그인 (세션 기반)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session)
    {
        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());

        if (optionalUser.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 해당 유저가 없습니다.");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 비밀번호가 틀렸습니다.");
        }

        // ✅ 로그인 성공 → 세션에 사용자 정보 저장
        session.setAttribute("loggedInUser", user);

        return ResponseEntity.ok("✅ 로그인 성공! 세션에 사용자 정보 저장됨");
    }

    // ✅ 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session)
    {
        session.invalidate(); // 세션 종료
        return ResponseEntity.ok("로그아웃 성공");
    }
}
