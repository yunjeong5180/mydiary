package com.diary.mydiary.controller;

import com.diary.mydiary.model.Diary;
import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.DiaryRepository;
import com.diary.mydiary.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DiaryController
{

    private final DiaryRepository diaryRepository;
    private final UserService userService; // ✅ UserService 주입

    public DiaryController(DiaryRepository diaryRepository, UserService userService)
    {
        this.diaryRepository = diaryRepository;
        this.userService = userService;
    }

    // 📋 [GET] 전체 일기 목록 가져오기
    @GetMapping("/diaries")
    public List<Diary> getDiaries(@RequestParam(defaultValue = "desc") String sort)
    {
        if (sort.equals("asc"))
        {
            return diaryRepository.findAllByOrderByCreatedAtAsc();
        } else {
            return diaryRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    // ✍️ [POST] 새 일기 저장하기 (세션 사용자 연결!)
    @PostMapping("/diaries")
    public Diary createDiary(@RequestBody Diary diary, HttpSession session)
    {
        User user = userService.getLoggedInUser(session); // ✅ 로그인된 사용자 확인
        diary.setUser(user); // ✅ 작성자 정보 추가
        return diaryRepository.save(diary);
    }

    // 🗑️ [DELETE] 특정 일기 삭제하기
    @DeleteMapping("/diaries/{id}")
    public void deleteDiary(@PathVariable Long id)
    {
        diaryRepository.deleteById(id);
    }
}
