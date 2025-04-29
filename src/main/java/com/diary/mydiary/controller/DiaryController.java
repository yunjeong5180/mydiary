package com.diary.mydiary.controller;

import com.diary.mydiary.model.Diary;
import com.diary.mydiary.model.User;
import com.diary.mydiary.repository.DiaryRepository;
import com.diary.mydiary.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 📘 일기 관련 요청을 처리하는 컨트롤러
 *
 * - 사용자가 일기를 작성하거나 불러오고, 삭제할 수 있도록
 *   각 기능을 API로 제공합니다.
 * - 로그인된 사용자 정보를 바탕으로 "작성자 연결" 기능도 포함됩니다.
 */
@RestController
public class DiaryController {

    private final DiaryRepository diaryRepository;
    private final UserService userService; // ✅ 로그인된 사용자 정보 확인용

    public DiaryController(DiaryRepository diaryRepository, UserService userService) {
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
    public List<Diary> getDiaries(@RequestParam(defaultValue = "desc") String sort) {
        if (sort.equals("asc")) {
            return diaryRepository.findAllByOrderByCreatedAtAsc();
        } else {
            return diaryRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    /**
     * ✍️ [POST] 새 일기 작성 (사진 첨부 가능)
     *
     * - title, content, (선택) image 파일을 함께 받습니다.
     * - 로그인된 사용자 정보와 함께 저장합니다.
     */
    @PostMapping("/diaries")
    public ResponseEntity<?> createDiary(@RequestParam("title") String title,
                                         @RequestParam("content") String content,
                                         @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                         HttpSession session) throws IOException
    {
        User user = userService.getLoggedInUser(session); // ✅ 로그인 여부 확인

        // 파일 저장 경로 설정
        String imagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/"; // ✅ 절대경로 사용
            String originalFilename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            String filePath = uploadDir + originalFilename;

            // uploads 폴더에 저장
            File dest = new File(filePath);
            dest.getParentFile().mkdirs(); // 폴더 없으면 생성
            imageFile.transferTo(dest);

            imagePath = "uploads/" + originalFilename; // ✅ DB에는 상대 경로로 저장
        }

        // Diary 객체 생성 및 저장
        Diary diary = new Diary(title, content, imagePath, user);
        diaryRepository.save(diary);

        return ResponseEntity.ok("Diary created successfully!");
    }

    /**
     * 🗑️ [DELETE] 특정 일기 삭제
     *
     * - URL에 포함된 일기 ID를 기반으로 해당 일기 삭제
     */
    @DeleteMapping("/diaries/{id}")
    public void deleteDiary(@PathVariable Long id) {
        diaryRepository.deleteById(id);
    }

    /**
     * 🛠️ [PATCH] 특정 일기 수정
     *
     * - URL에 포함된 일기 ID를 기반으로 제목과 내용을 수정합니다.
     */
    @PatchMapping("/diaries/{id}")
    public ResponseEntity<String> updateDiary(@PathVariable Long id, @RequestBody Diary diaryRequest) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diary not found"));

        diary.setTitle(diaryRequest.getTitle());
        diary.setContent(diaryRequest.getContent());

        diaryRepository.save(diary);

        return ResponseEntity.ok("Diary updated successfully");
    }
}
