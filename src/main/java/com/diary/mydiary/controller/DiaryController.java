package com.diary.mydiary.controller;

import com.diary.mydiary.model.Diary;
import com.diary.mydiary.repository.DiaryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ✅ 이 클래스는 웹에서 요청이 들어오면 그에 맞는 기능을 처리해주는 "컨트롤러" 역할을 해요
@RestController
public class DiaryController {

    // 📌 DB에 접근할 수 있게 도와주는 도구 (일기 저장/조회/삭제)
    private final DiaryRepository diaryRepository;

    // 💡 생성자를 통해 Spring이 자동으로 Repository를 연결해줘요
    //    → DB와 연결해서 일기 데이터를 저장하거나 불러올 수 있게 도와줌
    public DiaryController(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    // 📋 [GET] 전체 일기 목록 가져오기
    // 사용 예: /diaries?sort=asc 또는 /diaries?sort=desc
    @GetMapping("/diaries")
    public List<Diary> getDiaries(@RequestParam(defaultValue = "desc") String sort) {
        // 🔼 오래된 순으로 정렬해서 가져오기
        if (sort.equals("asc")) {
            return diaryRepository.findAllByOrderByCreatedAtAsc();
        }
        // 🔽 최신순으로 정렬해서 가져오기 (기본값)
        else {
            return diaryRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    // ✍️ [POST] 새 일기 저장하기
    // 사용 예: 일기 작성 폼에서 제목/내용을 입력해서 서버에 전송하면 이 메서드가 실행됨
    @PostMapping("/diaries")
    public Diary createDiary(@RequestBody Diary diary) {
        // 📥 받은 일기 데이터를 DB에 저장하고 그대로 반환
        return diaryRepository.save(diary);
    }

    // 🗑️ [DELETE] 특정 일기 삭제하기
    // 사용 예: /diaries/1 → ID가 1번인 일기 삭제
    @DeleteMapping("/diaries/{id}")
    public void deleteDiary(@PathVariable Long id) {
        // 🧹 해당 ID의 일기를 DB에서 찾아서 삭제함
        diaryRepository.deleteById(id);
    }
}
