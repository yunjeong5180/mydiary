package com.diary.mydiary.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity              // 이 클래스는  DB 테이블과 연결된다는 의미
@Getter
@Setter
@NoArgsConstructor  // 기본 생성자 자동 생성(Lombok)
@Table(name = "users")  // ✅ 테이블 이름을 명시적으로 설정
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    private  Long id;

    @Column(nullable = false, unique = true)            // 중복 불가
    private  String username;

    private String password;

    // 필요한 경우 생성자 추가 기능
    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
    }
}
