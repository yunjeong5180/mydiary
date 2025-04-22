package com.diary.mydiary.service;

import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 📌 [UserService] - 사용자 관련 핵심 기능 처리 클래스
 *
 * ✅ 역할:
 *   - 회원가입 시 비밀번호 암호화 처리
 *   - 아이디 중복 여부 확인
 *   - 현재 로그인된 사용자 정보 가져오기 (세션 기반)
 */
@Service
@RequiredArgsConstructor  // 생성자 주입 자동 생성 (userRepository, passwordEncoder)
public class UserService
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** ✅ 회원가입 처리
     *   - 비밀번호를 암호화한 뒤 DB에 저장
     */
    public User save(User user)
    {
        user.setPassword(passwordEncoder.encode(user.getPassword()));  // 🔐 비밀번호 암호화
        return userRepository.save(user);                              // 💾 저장
    }

    /** ✅ 아이디 중복 체크
     *   - 같은 username 이 이미 존재하는지 확인
     */
    public boolean existsByUsername(String username)
    {
        return userRepository.findByUsername(username).isPresent();
    }

    /** ✅ 로그인된 사용자 가져오기
     *   - 세션에서 "loggedInUser" 속성으로 사용자 객체 꺼내오기
     */
    public User getLoggedInUser(HttpSession session)
    {
        Object obj = session.getAttribute("loggedInUser");
        if (obj instanceof User u) return u;  // 세션에 유저가 있으면 반환
        throw new IllegalStateException("로그인 사용자가 없습니다."); // 없으면 예외 발생
    }
}
