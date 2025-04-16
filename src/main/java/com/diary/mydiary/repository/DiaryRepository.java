package com.diary.mydiary.repository;

import com.diary.mydiary.model.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

// 이 인터페이스만 만들면 저장, 삭제, 조회 다 가능!
public interface DiaryRepository extends JpaRepository<Diary, Long> {
}
