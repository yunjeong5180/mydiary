package com.diary.mydiary.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class DiaryController
{
    // ✅ 메모리에 일기를 저장할 리스트
    private final List<Map<String, String>> diaryList = new ArrayList<>();

    // ✅ GET: 일기 목록 조회
    @GetMapping("/diaries")
    public List<Map<String, String>> getDiaries() {
        return diaryList;
    }

    // ✅ POST: 일기 추가
    @PostMapping("/diaries")
    public Map<String, String> createDiary(@RequestBody Map<String, String> diary)
    {
        diaryList.add(diary); // 받은 일기를 저장
        return diary; // 저장한 일기를 그대로 반환
    }

}


