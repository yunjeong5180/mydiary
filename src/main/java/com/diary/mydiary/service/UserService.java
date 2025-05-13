package com.diary.mydiary.service;

import com.diary.mydiary.dto.FindIdResponse;
import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional; // Optional 임포트 확인 (이미 되어 있을 것)
import java.util.List; // 만약 findByEmail이 List를 반환한다면 필요

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
    public User save(User user)
    {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 🔐 비밀번호 암호화
        return userRepository.save(user);                             // 💾 저장
    }

    /**
     * ✅ 아이디 중복 체크
     * - 같은 username 이 이미 존재하는지 확인
     */
    public boolean existsByUsername(String username)
    {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * ✅ 이메일 중복 체크 (새로 추가)
     * - 같은 email이 이미 존재하는지 확인합니다.
     */
    public boolean existsByEmail(String email)
    { // UserRepository에 findByEmail이 Optional<User>를 반환한다고 가정

        return userRepository.findByEmail(email).isPresent();
        // 만약 UserRepository의 findByEmail이 List<User>를 반환한다면:
        // return !userRepository.findByEmail(email).isEmpty();
    }

    /**
     * ✅ 로그인된 사용자 가져오기
     * - 세션에서 "user" 속성으로 사용자 객체 꺼내오기
     */
    public User getLoggedInUser(HttpSession session)
    {
        Object obj = session.getAttribute("user"); // ✅ 컨트롤러와 키 이름 일치
        if (obj instanceof User u) return u;       // 세션에 유저가 있으면 반환
        throw new IllegalStateException("로그인 사용자가 없습니다.");
    }

    /**
     * ✅ 사용자 인증 (로그인 시 호출)
     * - 아이디/비번 확인 후 유저 객체 반환
     */
    public User validateUser(String username, String rawPassword)
    {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("유저가 없습니다.");
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
        {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        return user;
    }

    /**
     * userId로 사용자 가져오기
     */
    public User getUserById(Long id)
    {
        return userRepository.findById(id).orElse(null);
    }

    // --- 아이디 찾기 기능 추가 ---
    /**
     * ✅ 이메일로 사용자 아이디(username) 찾기
     * - 찾은 아이디는 마스킹 처리하여 반환합니다.
     */
    public FindIdResponse findUserIdByEmail(String email)
    {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent())
        {
            User user = userOptional.get();
            // User 엔티티의 getUsername() 메소드가 로그인 아이디를 반환한다고 가정합니다.
            String userId = user.getUsername();
            String maskedId = maskUserId(userId);
            return new FindIdResponse(true, "아이디를 찾았습니다. (일부 마스킹됨)", maskedId);
        } else
        {
            return new FindIdResponse(false, "해당 이메일로 가입된 아이디를 찾을 수 없습니다.", null);
        }
    }

    /**
     * 🔒 아이디 마스킹 처리 유틸리티 메소드
     * - 예: "testuser" -> "tes***er"
     * - 실제 마스킹 정책에 따라 상세 구현 필요
     */
    private String maskUserId(String userId)
    {
        if (userId == null || userId.isEmpty())
        {
            return "";
        }
        int length = userId.length();
        if (length <= 3)
        {
            // 3글자 이하면 첫 글자만 보여주고 나머지는 '*' (예: t**)
            // 또는 정책에 따라 다르게 처리 (예: 전부 마스킹 ***)
            return userId.substring(0, 1) + "*".repeat(Math.max(0, length - 1));
        } else if (length <= 5)
        {
            // 4~5 글자: 앞 2글자 + 나머지 '*' (예: te***)
            return userId.substring(0, 2) + "*".repeat(length - 2);
        } else
        {
            // 6글자 이상: 앞 3글자 + 중간 '*' + 마지막 2글자 (예: tes***er)
            // (주의: 중간 '*' 개수는 (전체길이 - 앞3 - 뒤2) 여야 함)
            int visibleStart = 3;
            int visibleEnd = 2;
            return userId.substring(0, visibleStart) +
                    "*".repeat(Math.max(0, length - visibleStart - visibleEnd)) +
                    userId.substring(length - visibleEnd);
        }
    }
}
