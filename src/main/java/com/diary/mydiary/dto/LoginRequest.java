package com.diary.mydiary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 🔑 로그인 요청 시 클라이언트에서 서버로 전달하는 데이터를 담는 DTO
 *    사용자가 입력한 아이디(username)와 비밀번호(password) 포함
 *    이 객체는 데이터베이스와 직접적인 관련은 없으며, 순수하게 데이터 전달 목적으로 사용
 *
 * - @Getter: Lombok이 모든 필드의 getter 메서드(getUsername(), getPassword()) 자동 생성
 * - @Setter: Lombok이 모든 필드의 setter 메서드(setUsername(), setPassword()) 자동 생성
 * - @NoArgsConstructor: Lombok이 파라미터 없는 기본 생성자(LoginRequest()) 자동 생성
 *   (JSON 라이브러리가 객체 변환 시 필요로 할 수 있다.)
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest
{
    // 👤 사용자가 로그인 시 입력한 아이디
    private String username;

    /**
     * 🔒 사용자가 로그인 시 입력한 비밀번호 (평문 상태)
     * 서버에서는 이 비밀번호를 암호화된 DB의 비밀번호와 비교
     */
    private String password;
}
