package com.diary.mydiary.model;

import jakarta.persistence.*;    // JPA 어노테이션 사용
import lombok.Getter;            // Lombok: Getter 메서드 자동 생성
import lombok.NoArgsConstructor; // Lombok: 기본 생성자 자동 생성
import lombok.Setter;            // Lombok: Setter 메서드 자동 생성

import java.time.LocalDate;      // 날짜 정보만을 다루기 위한 클래스 (Java 8 이상)

/**
 * 👤 [User] - 사용자 정보를 나타내는 엔티티 클래스 (데이터베이스의 'users' 테이블과 매핑)
 * 회원 가입한 사용자의 다양한 정보(개인 정보, 로그인 정보 등)를 저장하고 관리
 *
 * - @Entity: 이 클래스가 JPA 엔티티임을 나타냄
 * - @Getter/@Setter: Lombok을 통해 각 필드의 getter와 setter 메서드 자동 생성
 * - @NoArgsConstructor: JPA 구현체가 엔티티 객체를 생성할 때 필요한 기본 생성자를 Lombok이 만들어줌
 * - @Table(name = "users"): 데이터베이스 테이블 이름을 'users'로 명시적으로 지정
 * (만약 'user'라는 이름의 테이블이 이미 시스템 예약어이거나 충돌 가능성이 있다면, 'users'처럼 복수형을 사용하는 것이 일반적)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User
{
    /**
     * 🔑 사용자의 고유 식별자 (Primary Key)
     * - @Id: 이 필드가 테이블의 기본 키(PK)임을 나타냄
     * - @GeneratedValue(strategy = GenerationType.IDENTITY): 기본 키 생성을 데이터베이스에 위임
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 🆔 사용자의 로그인 아이디 (계정 이름)
     *
     * - nullable = false: 아이디는 반드시 존재해야 함 (null 불가)
     * - unique = true: 아이디는 전체 사용자 중에서 유일해야 함 (중복 불가)
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 🔒 사용자의 로그인 비밀번호
     * 이 필드에는 **암호화된 형태의 비밀번호**가 저장
     * (평문 비밀번호를 직접 저장하는 것은 매우 위험!)
     * 암호화 로직은 UserService 등 서비스 계층에서 처리
     */
    private String password;

    /** 👤 사용자의 이름 */
    private String name;

    /** 🎂 사용자의 생년월일 */
    private LocalDate birth;

    /** 🚻 사용자의 성별 (예: 남자, 여자) */
    private String gender;

    /** 📧 사용자의 이메일 주소 */
    private String email;

    /** 📞 사용자의 연락처 (휴대폰 번호 등) */
    private String phone;

    /**
     * 🛠️ 아이디(username)와 비밀번호(password)만으로 User 객체를 생성하는 편의 생성자
     * 주로 간단한 객체 생성이나 테스트 시 사용
     * (주의: 이 생성자로 생성된 객체의 비밀번호는 아직 암호화되지 않은 상태일 수 있으므로, 실제 저장 전에는 반드시 암호화 과정을 거쳐야 함.)
     *
     * @param username 사용자 아이디
     * @param password 사용자 비밀번호 (평문)
     */
    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
    }
}
