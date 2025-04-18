package com.diary.mydiary.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // ✅ 날짜 자동 저장 위해 필요!
public class Diary
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ✅ 작성자 정보 추가 (User 엔티티와 연관관계 설정)
    @ManyToOne
    @JoinColumn(name = "user_id")  // 외래키 컬럼명
    private User user;

    // 🔧 편의 생성자
    public Diary(String title, String content)
    {
        this.title = title;
        this.content = content;
    }
}
