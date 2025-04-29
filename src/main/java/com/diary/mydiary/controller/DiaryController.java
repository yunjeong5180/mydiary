package com.diary.mydiary.controller;

import com.diary.mydiary.model.Diary;
import com.diary.mydiary.repository.DiaryRepository;
import com.diary.mydiary.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 📚 일기 관리 요청을 처리하는 컨트롤러
 *
 * - 사용자가 일기를 작성하거나 불러오고, 삭제할 수 있도록
 *   각 기능을 API로 제공합니다.
 * - 로그인된 사용자 정보를 바탕으로 "작성자 연결" 기능도 포함됩니다.
 */
@RestController
public class DiaryController {

    private final DiaryRepository diaryRepository;
    private final UserService userService;

    public DiaryController(DiaryRepository diaryRepository, UserService userService) {
        this.diaryRepository = diaryRepository;
        this.userService = userService;
    }

    /**
     * 📋 [GET] 전체 일기 목록 조회
     *
     * - 로그인한 사용자의 일기만 보여줍니다.
     * - /diaries?sort=asc 또는 /diaries?sort=desc 형식으로 요청
     */
    @GetMapping("/diaries")
    public ResponseEntity<?> getDiaries(@RequestParam(defaultValue = "desc") String sort, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        List<Diary> diaries;
        if (sort.equals("asc")) {
            diaries = diaryRepository.findAllByUserIdOrderByCreatedAtAsc(userId);
        } else {
            diaries = diaryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        }

        return ResponseEntity.ok(diaries);
    }

    /**
     * ✍️ [POST] 새 일기 작성 (사진 첨부 가능)
     */
    @PostMapping("/diaries")
    public ResponseEntity<?> createDiary(@RequestParam("title") String title,
                                         @RequestParam("content") String content,
                                         @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                         HttpSession session) throws IOException {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String imagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            String originalFilename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            String filePath = uploadDir + originalFilename;

            File dest = new File(filePath);
            dest.getParentFile().mkdirs();
            imageFile.transferTo(dest);

            imagePath = "uploads/" + originalFilename;
        }

        // 🔥 userIdRef에 userId만 저장!
        Diary diary = new Diary();
        diary.setTitle(title);
        diary.setContent(content);
        diary.setImagePath(imagePath);
        diary.setUserIdRef(userId);

        diaryRepository.save(diary);

        return ResponseEntity.ok("Diary created successfully!");
    }

    /**
     * 🗑️ [DELETE] 특정 일기 삭제
     */
    @DeleteMapping("/diaries/{id}")
    public void deleteDiary(@PathVariable Long id) {
        diaryRepository.deleteById(id);
    }

    /**
     * 🛠️ [PATCH] 특정 일기 수정
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
