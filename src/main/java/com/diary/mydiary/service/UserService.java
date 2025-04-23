package com.diary.mydiary.service;

import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 📌 [UserService] - 사용자 관련 핵심 기능 처리 클래스
 *
 * ✅ 역할:
 *   - 회원가입 시 비밀번호 암호화 처리
 *   - 아이디 중복 여부 확인
 *   - 현재 로그인된 사용자 정보 가져오기 (세션 기반)
 */
@Service
@RequiredArgsConstructor  // 생성자 주입 자동 생성
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * ✅ 회원가입 처리
     * - 비밀번호를 암호화한 뒤 DB에 저장
     */
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 🔐 비밀번호 암호화
        return userRepository.save(user);                             // 💾 저장
    }

    /**
     * ✅ 아이디 중복 체크
     * - 같은 username 이 이미 존재하는지 확인
     */
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * ✅ 로그인된 사용자 가져오기
     * - 세션에서 "user" 속성으로 사용자 객체 꺼내오기
     */
    public User getLoggedInUser(HttpSession session) {
        Object obj = session.getAttribute("user"); // ✅ 컨트롤러와 키 이름 일치
        if (obj instanceof User u) return u;       // 세션에 유저가 있으면 반환
        throw new IllegalStateException("로그인 사용자가 없습니다.");
    }

    /**
     * ✅ 사용자 인증 (로그인 시 호출)
     * - 아이디/비번 확인 후 유저 객체 반환
     */
    public User validateUser(String username, String rawPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("유저가 없습니다.");
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        return user;
    }
}
