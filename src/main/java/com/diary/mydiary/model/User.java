package com.diary.mydiary.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 👤 [User] - 사용자 정보를 저장하는 DB 테이블
 *
 * 이 클래스는 회원가입한 사용자들의 정보를 저장합니다.
 * 사용자 이름, 생년월일, 성별, 이메일, 연락처 등의 개인 정보와
 * 로그인용 아이디(username), 비밀번호를 포함합니다.
 */
@Entity                             // 이 클래스는 DB 테이블로 사용됨
@Getter
@Setter
@NoArgsConstructor                  // 기본 생성자 자동 생성 (lombok)
@Table(name = "users")             // 테이블 이름을 명시적으로 "users"로 지정
public class User
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가되는 기본키
    private Long id;

    /** 🔐 로그인용 아이디 (중복 불가) */
    @Column(nullable = false, unique = true)
    private String username;

    /** 🔒 로그인 비밀번호 (암호화 저장) */
    private String password;

    /** 👤 사용자 이름 */
    private String name;

    /** 🎂 생년월일 */
    private LocalDate birth;

    /** 🚻 성별 (예: 남자, 여자, 기타) */
    private String gender;

    /** 📧 이메일 주소 */
    private String email;

    /** 📞 연락처 (휴대폰 번호 등) */
    private String phone;

    /** 🔧 편의 생성자 (username, password만 설정할 때 사용) */
    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
    }
}
