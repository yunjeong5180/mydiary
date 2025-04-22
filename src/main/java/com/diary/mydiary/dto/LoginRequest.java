package com.diary.mydiary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 🔐 [LoginRequest] - 로그인 요청 데이터 전송용 DTO
 *
 * 👉 사용자가 로그인할 때 입력한 아이디(username)와 비밀번호(password)를 담는 클래스입니다.
 * 👉 프론트엔드에서 로그인 버튼을 눌렀을 때 백엔드로 전달되는 간단한 데이터 꾸러미
 *
 * - @Getter: 모든 필드의 getter 메서드를 자동 생성
 * - @Setter: 모든 필드의 setter 메서드를 자동 생성
 * - @NoArgsConstructor: 기본 생성자 자동 생성 (파라미터 없는 생성자)
 *
 * 👉 이 클래스는 DB와는 관련이 없고, 오직 '입력값 전달용' 으로 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest
{

    /** 👤 사용자 아이디 */
    private String username;

    /** 🔑 사용자 비밀번호 */
    private String password;
}
