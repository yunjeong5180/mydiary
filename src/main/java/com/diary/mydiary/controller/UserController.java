package com.diary.mydiary.controller;

import com.diary.mydiary.dto.LoginRequest;
import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@RestController             // 이 클래스가 REST API를 처리하는 컨트롤러임을 의미
@RequestMapping("/users")   // 이 컨트롤러는 /users 경로로 시작하는 요청 처리
public class UserController 
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // ✍️ 회원가입 (POST/users/signup)
    @PostMapping("/signup") // /users/signup 주소로 POST 요청이 오면 회원가입 수행
    public  User registerUser(@RequestBody User user) // 프론트에서 JSON으로 보낸 user 정보를 자동으로 매핑
    {
        // 나중에 비밀번호 암호화도 추가하면 더 안전!
        return userRepository.save(user); // DB에 새로운 사용자 저장
    }

    // ✅ 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest)
    {
        // username으로 사용자 검색
        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());

        if (optionalUser.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("존재하지 않는 사용자입니다.");
        }

        User user = optionalUser.get();

        // 비밀번호 비교 (암호화된 비밀번호와 비교)
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 틀렸습니다.");
        }

        // 👉 여기에서 나중에 JWT 토큰 발급 가능
        return ResponseEntity.ok("로그인 성공!");

    }
}
