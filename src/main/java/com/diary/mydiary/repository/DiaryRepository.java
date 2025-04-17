package com.diary.mydiary.repository;

import com.diary.mydiary.model.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// 🔸 이 인터페이스는 DB에 데이터를 저장하고 불러오는 기능을 자동으로 제공해줌
//     예: 저장하기, 전체 조회하기, 삭제하기 등 기본 기능은 JpaRepository가 다 처리해줘!

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 🔽 최신순 정렬로 전체 일기 불러오기
    //     → createdAt(작성 시간) 기준으로 가장 최근에 쓴 일기가 먼저 나옴
    List<Diary> findAllByOrderByCreatedAtDesc();

    // 🔼 오래된순 정렬로 전체 일기 불러오기
    //     → createdAt(작성 시간) 기준으로 가장 오래된 일기부터 보여줌
    List<Diary> findAllByOrderByCreatedAtAsc();
}
