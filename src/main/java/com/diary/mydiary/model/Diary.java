package com.diary.mydiary.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 📘 [Diary] - 일기장 엔티티 (DB의 diary 테이블과 연결됨)
 *
 * 이 클래스는 데이터베이스와 연결되는 "일기" 테이블을 정의합니다.
 * 사용자가 작성한 일기의 제목, 내용, 작성일시, 작성자 정보를 저장합니다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 📝 일기 제목 */
    private String title;

    /** 📄 일기 내용 */
    @Column(length = 5000)
    private String content;

    /** 🖼️ 첨부한 이미지 파일 경로 */
    @Column(nullable = true)
    private String imagePath;

    /** 🕒 일기 작성 시간 (자동 저장) */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 👤 작성자 (User 객체와 연관관계 매핑)
     * - user_id 컬럼과 매칭되지만, 이 필드는 insert/update를 하지 않음
     */
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 👤 작성자 ID (숫자만 저장/수정하는 용도)
     * - 실제 DB 컬럼 이름은 user_id
     */
    @Column(name = "user_id", nullable = false)
    private Long userIdRef;

    /** 🔧 편의 생성자 (초기 테스트용) */
    public Diary(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /** 🔧 새 생성자 (사진 추가용) */
    public Diary(String title, String content, String imagePath, Long userIdRef) {
        this.title = title;
        this.content = content;
        this.imagePath = imagePath;
        this.userIdRef = userIdRef;
    }
}
