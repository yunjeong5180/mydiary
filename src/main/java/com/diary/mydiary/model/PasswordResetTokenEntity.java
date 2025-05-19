package com.diary.mydiary.model;

import jakarta.persistence.*; // JPA 어노테이션 사용
import lombok.Getter;      // Lombok Getter
import lombok.NoArgsConstructor; // Lombok 기본 생성자
import lombok.Setter;      // Lombok Setter

import java.util.Date;     // 만료 날짜 필드를 위해 Date 타입 사용

/**
 * 🔑 비밀번호 재설정 토큰 정보를 나타내는 엔티티 클래스입니다.
 *
 * - 사용자가 비밀번호 재설정을 요청할 때 생성되며,
 * 이메일로 발송된 링크에 포함된 토큰의 유효성을 검증하는 데 사용됩니다.
 */
@Entity
@Table(name = "password_reset_tokens") // 데이터베이스 테이블 이름 지정
@Getter
@Setter
@NoArgsConstructor // JPA는 기본 생성자를 필요로 합니다.
public class PasswordResetTokenEntity {

    private static final int EXPIRATION = 60 * 24; // 토큰 유효 시간 (예: 24시간, 분 단위로 설정 후 Date 객체 생성 시 계산)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 데이터베이스가 ID를 자동 생성하도록 설정 (예: auto_increment)
    private Long id; // 기본 키 (Primary Key)

    @Column(nullable = false, unique = true) // null을 허용하지 않고, 유일한 값을 가져야 함
    private String token; // 비밀번호 재설정 토큰 문자열

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER) // User 엔티티와의 다대일(N:1) 관계
    @JoinColumn(nullable = false, name = "user_id") // 외래 키 (Foreign Key) 컬럼 이름 지정, null 허용 안 함
    private User user; // 해당 토큰과 연관된 사용자 (User 엔티티는 이미 존재한다고 가정)

    @Column(nullable = false)
    private Date expiryDate; // 토큰 만료 날짜 및 시간

    @Column(nullable = false)
    private boolean isUsed = false; // 토큰 사용 여부 (기본값: false)

    /**
     * 토큰, 사용자, 만료 날짜를 받아 PasswordResetTokenEntity 객체를 생성하는 생성자입니다.
     * @param token 생성된 토큰 문자열
     * @param user 토큰과 연관된 사용자 객체
     * @param expiryDate 토큰 만료 시간
     */
    public PasswordResetTokenEntity(String token, User user, Date expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.isUsed = false; // 생성 시 기본적으로 미사용 상태
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     * @return 만료되었다면 true, 그렇지 않다면 false
     */
    public boolean isTokenExpired() {
        return new Date().after(this.expiryDate);
    }
}