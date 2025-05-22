package com.diary.mydiary.model;

import jakarta.persistence.*;    // JPA (Java Persistence API) 어노테이션들을 사용하기 위해 임포트
import java.time.LocalDateTime;  // 날짜와 시간 정보를 다루기 위한 클래스 (Java 8 이상)
import lombok.Getter;            // Lombok: Getter 메서드 자동 생성
import lombok.NoArgsConstructor; // Lombok: 파라미터 없는 기본 생성자 자동 생성
import lombok.Setter;            // Lombok: Setter 메서드 자동 생성
import org.springframework.data.annotation.CreatedDate;                    // Spring Data JPA: 엔티티 생성 시간 자동 기록
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Spring Data JPA: Auditing 기능 활성화

/**
 * 📖 [Diary] - 일기 정보를 나타내는 엔티티 클래스 (데이터베이스의 'diary' 테이블과 매핑)
 *
 * 사용자가 작성한 일기의 제목, 내용, 첨부 이미지 경로, 작성 시간, 어떤 사용자가 작성했는지 등의 정보를 담고 관리
 *
 * - @Entity: 이 클래스가 JPA 엔티티임을 나타냄. 데이터베이스 테이블과 1:1로 매핑
 * - @Getter/@Setter: Lombok을 통해 각 필드의 getter와 setter 메서드를 자동 생성
 * - @NoArgsConstructor: JPA 구현체(예: Hibernate)가 엔티티 객체를 생성할 때 필요한 기본 생성자를 Lombok이 만듬
 * - @EntityListeners(AuditingEntityListener.class): JPA의 Auditing 기능을 사용하여,
 *                                                  엔티티가 생성되거나 수정될 때 특정 필드(예: createdAt)의 값을 자동으로 채움
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Diary
{
    /**
     * 🔑 일기의 고유 식별자 (Primary Key)
     * - @Id: 이 필드가 테이블의 기본 키(PK)임을 나타냄
     * - @GeneratedValue(strategy = GenerationType.IDENTITY): 기본 키 생성을 데이터베이스에 위임
     *  (예: MySQL의 AUTO_INCREMENT, PostgreSQL의 SERIAL)
     * 새로운 일기가 저장될 때마다 데이터베이스가 자동으로 ID를 증가시켜 할당
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 일기 고유 ID (숫자형

    /**
     * 📝 일기 제목
     * 특별한 제약 조건이 없으므로, 데이터베이스의 기본 VARCHAR 타입으로 매핑
     */
    private String title;

    /**
     * 📄 일기 내용
     * - @Column(length = 5000): 데이터베이스 컬럼의 길이를 5000자로 설정
     * (기본 VARCHAR 길이보다 긴 내용을 저장하기 위함)
     */
    @Column(length = 5000)
    private String content;

    /**
     * 🖼️ 첨부된 이미지 파일의 서버 내 저장 경로
     * - @Column(nullable = true): 이 필드는 null 값을 허용 (이미지가 없을 수도 있으므로)
     *   데이터베이스 컬럼에는 NOT NULL 제약조건이 적용되지 않음 (예: "uploads/timestamp_image.jpg")
     */
    @Column(nullable = true)
    private String imagePath;

    /**
     * 🕒 일기가 처음 작성된 시간 (자동으로 기록됨)
     * - @CreatedDate: JPA Auditing 기능에 의해, 엔티티가 처음 저장될 때 현재 시간이 자동으로 이 필드에 할당
     * - @Column(updatable = false): 이 필드는 한 번 값이 설정된 후에는 수정할 수 없도록 한다. (즉, 작성 시간은 변경되지 않음)
     * - (참고) Auditing 기능을 사용하려면 메인 애플리케이션 클래스에 @EnableJpaAuditing 어노테이션이 필요
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 👤 이 일기를 작성한 사용자 정보 (User 엔티티와의 관계)
     * 이 필드는 주로 조회 시 사용자 정보를 함께 가져오거나, JPA 연관관계를 표현하기 위해 사용
     *
     * - @ManyToOne: 다대일 관계. 여러 개의 일기(Diary)가 하나의 사용자(User)에게 속할 수 있다.
     * - name = "user_id": 데이터베이스의 'diary' 테이블에 'user_id' 라는 이름의 외래 키(FK) 컬럼 사용
     * - insertable = false, updatable = false: 이 'user' 필드를 통해서는 'user_id' 컬럼의 값을
     *                                          직접 삽입하거나 업데이트하지 않겠다는 의미, 실제 ID 값 관리는 아래 'userIdRef' 필드 사용
     */
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 👤 이 일기를 작성한 사용자의 ID (실제 DB에 저장/수정되는 값)
     * 이 필드를 통해 일기를 저장, 수정할 때 실제 사용자 ID 값을 직접 다룬다.
     *
     * - name = "user_id": 데이터베이스의 'diary' 테이블에서 'user_id' 컬럼과 매핑 (위의 @JoinColumn과 동일한 이름)
     * - nullable = false: 이 필드는 null 값을 허용하지 않음. 모든 일기는 반드시 작성자 ID를 가져야 함.
     */
    @Column(name = "user_id", nullable = false)
    private Long userIdRef;

    /**
     * 🛠️ 제목과 내용만으로 Diary 객체를 생성하는 편의 생성자 (주로 초기 테스트나 간단한 객체 생성 시 사용)
     *
     * @param title 일기 제목
     * @param content 일기 내용
     */
    public Diary(String title, String content)
    {
        this.title = title;
        this.content = content;
    }

    /**
     * 🛠️ 모든 주요 정보를 받아 Diary 객체를 생성하는 편의 생성자 (이미지 및 작성자 ID 포함)
     *
     * @param title 일기 제목
     * @param content 일기 내용
     * @param imagePath 첨부 이미지 파일 경로 (없으면 null)
     * @param userIdRef 작성자 ID
     */
    public Diary(String title, String content, String imagePath, Long userIdRef)
    {
        this.title = title;
        this.content = content;
        this.imagePath = imagePath;
        this.userIdRef = userIdRef;
    }
}
