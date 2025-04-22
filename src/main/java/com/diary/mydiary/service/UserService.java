package com.diary.mydiary.service;

import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** ✅ 회원가입: 비밀번호 암호화 후 저장 */
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 🔐 비밀번호 암호화
        return userRepository.save(user);
    }

    /** ✅ 아이디 중복 확인 */
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /** ✅ 세션에서 로그인된 사용자 가져오기 */
    public User getLoggedInUser(HttpSession session) {
        Object obj = session.getAttribute("loggedInUser");
        if (obj instanceof User u) return u;
        throw new IllegalStateException("로그인 사용자가 없습니다.");
    }
}
