package com.diary.mydiary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 🔐 비밀번호 암호화 설정 파일
 *
 * - 회원가입 시 사용자의 비밀번호를 안전하게 저장하기 위해
 *   평문(그대로) 저장이 아닌 '암호화'된 형태로 저장합니다.
 * - 이 설정을 통해 스프링에서 사용할 암호화 도구(BCrypt)를 등록합니다.
 * - 이 파일은 로그인 필터를 포함한 '전체 보안 설정'이 아니라,
 *   암호화 도구만 따로 분리해 만든 설정입니다.
 */
@Configuration  // 이 클래스는 설정용 파일이라는 표시
public class PasswordConfig
{
    /**
     * 💡 스프링에서 사용할 암호화 도구를 등록하는 메서드
     *
     * - BCrypt는 보안에 강한 알고리즘입니다.
     * - 회원가입 시 비밀번호를 암호화하거나
     *   로그인 시 비밀번호 비교에 사용됩니다.
     */
    @Bean  // 스프링이 이 메서드를 자동으로 호출하고 관리하게 함
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();  // 비밀번호를 암호화해주는 도구
    }
}