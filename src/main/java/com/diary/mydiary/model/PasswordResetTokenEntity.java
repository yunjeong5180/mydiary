package com.diary.mydiary.model;

import jakarta.persistence.*;    // JPA 어노테이션 사용
import lombok.Getter;            // Lombok: Getter 메서드 자동 생성
import lombok.NoArgsConstructor; // Lombok: 기본 생성자 자동 생성
import lombok.Setter;            // Lombok: Setter 메서드 자동 생성

import java.util.Date;           // 토큰 만료 날짜 필드를 위해 java.util.Date 타입 사용 (Java 8 이전 스타일)

/**
 * 🔑 비밀번호 재설정 토큰 정보를 나타내는 엔티티 클래스
 * 사용자가 비밀번호 재설정을 요청하면, 이메일로 발송될 링크에 포함될 고유 토큰 정보를 저장하고 관리
 * 이 토큰의 유효성을 검증하여 안전하게 비밀번호를 재설정할 수 있도록 도음
 *
 * - @Entity: JPA 엔티티임을 나타냄
 * - @Table(name = "password_reset_tokens"): 데이터베이스 테이블 이름을 'password_reset_tokens'로 명시적으로 지정
 *                                           (지정하지 않으면 클래스 이름(PasswordResetTokenEntity)을 기반으로 자동 생성)
 * - @Getter/@Setter: Lombok을 통해 getter/setter 메서드를 자동 생성
 * - @NoArgsConstructor: JPA에서 엔티티 객체 생성 시 필요한 기본 생성자를 Lombok이 만들어줌
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetTokenEntity
{
    /**
     * ⏳ 토큰의 유효 시간 (단위: 분)
     * 예: 60 * 24는 24시간을 의미
     * 이 상수는 토큰 생성 시 만료 날짜를 계산하는 데 사용
     * (실제 만료 시간은 UserService에서 application.properties의 값을 참조하여 설정)
     */
    private static final int EXPIRATION = 60 * 24; // 예: 24시간 (분 단위)

    /**
     * 🔑 토큰의 고유 식별자 (Primary Key)
     * - @Id: 이 필드가 테이블의 기본 키(PK)임을 나타냄
     * - @GeneratedValue(strategy = GenerationType.IDENTITY): 데이터베이스가 ID를 자동 생성하도록 설정 (예: auto_increment)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 💨 비밀번호 재설정 요청 시 생성되는 고유한 토큰 문자열 이메일 링크 등을 통해 사용자에게 전달
     *
     * - nullable = false: 토큰 문자열은 반드시 존재해야 함 (null 불가)
     * - unique = true: 토큰 문자열은 전체 테이블에서 유일한 값이어야 함 (중복 불가)
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * 👤 이 토큰과 연관된 사용자 (User 엔티티와의 관계)
     *
     * - @ManyToOne: 다대일 관계. 여러 개의 토큰이 하나의 사용자에게 발급
     * - targetEntity = User.class: 연관된 엔티티가 User 클래스임을 명시
     * - fetch = FetchType.EAGER: 이 토큰 엔티티를 조회할 때 연관된 User 정보도 즉시 함께 가져옴
     *                            (상황에 따라 LAZY 로딩을 고려할 수 있음. EAGER는 연관된 엔티티가 항상 필요할 때 유용)
     * - nullable = false: 모든 토큰은 반드시 특정 사용자와 연결 (null 불가)
     * - name = "user_id": 'password_reset_tokens' 테이블에 'user_id'라는 이름의 외래 키(FK) 컬럼 사용
     */
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    /**
     * ⏰ 이 토큰이 만료되는 정확한 날짜 및 시간
     * - @Column(nullable = false): 만료 날짜 설정
     * (java.util.Date는 오래된 API이므로, java.time.LocalDateTime 또는 Instant 사용 권장)
     */
    @Column(nullable = false)
    private Date expiryDate; // 토큰 만료 날짜 및 시간

    /**
     * ✅ 이 토큰이 이미 사용되었는지 여부를 나타내는 플래그
     *
     * - @Column(nullable = false): 사용 여부 명시
     *                      기본값은 false (미사용)로 설정, 한 번 사용된 토큰은 재사용할 수 없음.
     */
    @Column(nullable = false)
    private boolean isUsed = false;

    /**
     * 🛠️ 토큰 문자열, 사용자 객체, 만료 날짜를 받아 PasswordResetTokenEntity 객체를 생성하는 생성자
     * 토큰 생성 시 이 생성자를 통해 필요한 값들을 설정
     *
     * @param token 생성된 고유 토큰 문자열
     * @param user 토큰과 연관될 사용자 객체
     * @param expiryDate 토큰이 만료될 날짜 및 시간
     */
    public PasswordResetTokenEntity(String token, User user, Date expiryDate)
    {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.isUsed = false; // 생성 시 기본적으로 미사용 상태
    }

    /**
     * ⏳ 현재 시간을 기준으로 이 토큰이 만료되었는지 여부 확인
     *
     * @return 토큰이 만료되었다면 true, 아직 유효하다면 false 반환
     */
    public boolean isTokenExpired()
    {
        // 현재 시간이 만료 날짜(this.expiryDate) 이후인지 확인
        // new Date()는 현재 시간을 나타내는 Date 객체 생성
        return new Date().after(this.expiryDate);
    }
}