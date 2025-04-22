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
@Entity                                     // 이 클래스가 DB 테이블과 연결됨
@Getter
@Setter
@NoArgsConstructor                          // 파라미터 없는 생성자 자동 생성
@EntityListeners(AuditingEntityListener.class) // 작성 시점(createdAt) 자동 저장을 위해 필요
public class Diary
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키: 자동 증가 숫자
    private Long id;

    /** 📝 일기 제목 */
    private String title;

    /** 📄 일기 내용 */
    private String content;

    /** 🕒 일기 작성 시간 (자동 저장) */
    @CreatedDate
    @Column(updatable = false)               // 수정 불가능 (처음 생성 시점만 기록)
    private LocalDateTime createdAt;

    /**
     * 👤 일기 작성자
     * - ManyToOne: 여러 개의 일기 → 하나의 사용자에게 연결됨
     * - JoinColumn(name="user_id"): users 테이블의 id와 연결되는 외래키
     */
    @ManyToOne
    @JoinColumn(name = "user_id")            // 외래키 컬럼명
    private User user;

    /** 🔧 편의 생성자 (초기 테스트용) */
    public Diary(String title, String content)
    {
        this.title = title;
        this.content = content;
    }
}
