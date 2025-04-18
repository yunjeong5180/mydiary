package com.diary.mydiary.service;

import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service  // 💡 스프링이 이 클래스를 서비스로 인식하게 함
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

    // ✅ 아이디 중복 검사용 메서드 (추가적으로 사용 가능!)
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
