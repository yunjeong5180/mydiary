package com.diary.mydiary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 🔐 비밀번호 암호화 설정 파일
 *
 * - 목적: 회원가입 시 사용자의 비밀번호를 안전하게 저장하기 위해 평문(그대로) 저장이 아닌 '암호화'된 형태로 저장
 * - 방식: 'BCrypt'라는 강력한 암호화 알고리즘을 사용
 * - 범위: 이 파일은 스프링 시큐리티의 전체 보안 설정 중 '암호화 도구 등록' 부분만 담당
 *   (로그인 처리나 페이지 접근 권한 설정 등은 다른 곳에서 할 수 있어요.)
 */
@Configuration  // 환경 설정을 위한 특별한 클래스
public class PasswordConfig
{
    /**
     * 💡 스프링에서 사용할 비밀번호 암호화 도구를 등록하는 메서드
     * - @Bean 어노테이션: 이 메서드가 반환하는 객체(여기서는 BCryptPasswordEncoder)를 스프링이 직접 관리 (IoC 컨테이너에 등록)
     *   이렇게 등록해두면, 다른 곳에서 필요할 때마다 스프링이 알아서 제공
     * - PasswordEncoder 인터페이스: 스프링 시큐리티에서 비밀번호 암호화를 다루는 표준 방식
     * - BCryptPasswordEncoder 구현체: PasswordEncoder의 실제 구현체로, BCrypt 알고리즘을 사용
     *   (소금(salt)을 쳐서 암호화하기 때문에 보안성이 높음!)
     * - 사용처:
     *   1. 회원가입 시: 사용자가 입력한 비밀번호를 이 도구로 암호화해서 DB에 저장
     *   2. 로그인 시: 사용자가 입력한 비밀번호를 암호화해서 DB의 암호화된 비밀번호와 비교
     */
    @Bean  // 스프링이 이 메서드를 자동으로 호출하고 관리하게 함
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();  // BCrypt 암호화 도구를 생성해서 반환!
    }
}