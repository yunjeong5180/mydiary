package com.diary.mydiary.controller;

import com.diary.mydiary.model.Diary;
import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.DiaryRepository;
import com.diary.mydiary.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 📘 일기 관련 요청을 처리하는 컨트롤러
 *
 * - 사용자가 일기를 작성하거나 불러오고, 삭제할 수 있도록
 *   각 기능을 API로 제공합니다.
 * - 로그인된 사용자 정보를 바탕으로 "작성자 연결" 기능도 포함됩니다.
 */
@RestController
public class DiaryController
{

    private final DiaryRepository diaryRepository;
    private final UserService userService; // ✅ 로그인된 사용자 정보 확인용

    public DiaryController(DiaryRepository diaryRepository, UserService userService)
    {
        this.diaryRepository = diaryRepository;
        this.userService = userService;
    }

    /**
     * 📋 [GET] 전체 일기 목록 조회
     *
     * - /diaries?sort=asc 또는 /diaries?sort=desc 형식으로 요청
     * - 정렬 기준에 따라 최신순 또는 오래된순으로 정렬
     */
    @GetMapping("/diaries")
    public List<Diary> getDiaries(@RequestParam(defaultValue = "desc") String sort)
    {
        if (sort.equals("asc"))
        {
            return diaryRepository.findAllByOrderByCreatedAtAsc();
        } else
        {
            return diaryRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    /**
     * ✍️ [POST] 새 일기 작성
     *
     * - 로그인된 사용자의 정보를 세션에서 가져와
     *   작성자 정보와 함께 일기를 저장합니다.
     */
    @PostMapping("/diaries")
    public Diary createDiary(@RequestBody Diary diary, HttpSession session)
    {
        User user = userService.getLoggedInUser(session); // ✅ 로그인 여부 확인
        diary.setUser(user);                              // ✅ 작성자 연결
        return diaryRepository.save(diary);               // ✅ DB에 저장
    }

    /**
     * 🗑️ [DELETE] 특정 일기 삭제
     *
     * - URL에 포함된 일기 ID를 기반으로 해당 일기 삭제
     * - 현재는 작성자 확인 없이 삭제 가능 (보완 가능)
     */
    @DeleteMapping("/diaries/{id}")
    public void deleteDiary(@PathVariable Long id)
    {
        diaryRepository.deleteById(id);
    }
}
