package com.diary.mydiary.repository;

import com.diary.mydiary.model.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 📁 [DiaryRepository] - 일기 데이터를 DB에서 저장하고 불러오는 역할
 *
 * 이 인터페이스는 Spring Data JPA에서 제공하는 JpaRepository를 상속해서,
 * 기본적인 CRUD 기능(저장, 조회, 삭제 등)을 자동으로 사용할 수 있게 해줍니다.
 *
 * 또한, 작성일(createdAt)을 기준으로 정렬된 일기 목록을 가지고 오른
 * 컨스턴 쿠어맨은도 포함되어 있어요!
 */
public interface DiaryRepository extends JpaRepository<Diary, Long>
{

    /**
     * 🔽 최신순 정렬
     * DB에서 createdAt 필드를 기준으로
     * 최신 순서대로(가장 그림이 많은) 모든 일기를 불러오는다.
     */
    List<Diary> findAllByOrderByCreatedAtDesc();

    /**
     * 🔾 오래된순 정렬
     * DB에서 createdAt 필드를 기준으로
     * 가장 오래된 일기부터 차례로 모든 일기를 불러오는다.
     */
    List<Diary> findAllByOrderByCreatedAtAsc();

    /**
     * 📌 사용자 보고싶은 일기 - 최신순
     */
    List<Diary> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 📌 사용자 보고싶은 일기 - 오래된순
     */
    List<Diary> findAllByUserIdOrderByCreatedAtAsc(Long userId);
}
