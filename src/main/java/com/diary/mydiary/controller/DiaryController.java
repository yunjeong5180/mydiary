package com.diary.mydiary.controller;

import com.diary.mydiary.model.Diary;
import com.diary.mydiary.repository.DiaryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DiaryController {

    private final DiaryRepository diaryRepository;

    // 💡 생성자를 통해 Spring이 자동으로 Repository 주입해줌
    public DiaryController(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    // 📋 전체 일기 목록 가져오기
    @GetMapping("/diaries")
    public List<Diary> getDiaries() {
        return diaryRepository.findAll(); // DB에서 모든 일기 불러오기
    }

    // ✍️ 새 일기 저장하기
    @PostMapping("/diaries")
    public Diary createDiary(@RequestBody Diary diary) {
        return diaryRepository.save(diary); // DB에 저장하고 결과 반환
    }

    // 🗑️ 일기 삭제하기
    @DeleteMapping("/diaries/{id}")
    public void deleteDiary(@PathVariable Long id) {
        diaryRepository.deleteById(id); // 해당 ID의 일기를 DB에서 삭제
    }
}
