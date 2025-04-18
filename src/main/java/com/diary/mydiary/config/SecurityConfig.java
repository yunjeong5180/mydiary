package com.diary.mydiary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig
{

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        http
                // ✅ CSRF 보호 활성화 (기본값이므로 생략 가능하지만 명시)
                .csrf(csrf -> csrf.disable()) // 필요 시 true로 바꿔도 됨

                // ✅ 세션 기반 로그인 사용
                .formLogin(form -> form
                        .loginPage("/login.html")           // 사용자 지정 로그인 페이지 경로
                        .loginProcessingUrl("/users/login") // 로그인 POST 처리 경로
                        .defaultSuccessUrl("/index.html", true) // 로그인 성공 시 이동
                        .permitAll()
                )

                // ✅ 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/index.html")
                        .permitAll()
                )

                // ✅ 인증 필요 없는 경로 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/login.html", "/users/signup", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
