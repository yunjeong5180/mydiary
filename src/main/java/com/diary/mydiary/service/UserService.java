package com.diary.mydiary.service;

import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // 💡 스프링이 이 클래스를 서비스로 인식하게 함
public class UserService
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 💡 생성자 주입 방식
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ 회원가입 시 비밀번호 암호화 후 저장
    public User save(User user)
    {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 비밀번호 암호화!
        return userRepository.save(user);
    }

    // ✅ 아이디 중복 검사용 메서드
    public boolean existsByUsername(String username)
    {
        return userRepository.findByUsername(username).isPresent();
    }

    // ✅ 세션에서 로그인된 사용자 가져오기
    public User getLoggedInUser(HttpSession session)
    {
        Object user = session.getAttribute("loggedInUser");

        if (user instanceof User)
        {
            return (User) user;
        }

        throw new IllegalStateException("로그인된 사용자가 없습니다.");
    }
}
