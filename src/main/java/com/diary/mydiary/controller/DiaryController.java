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
 * 📚 일기 관리 요청을 처리하는 컨트롤러 (일기장 관리자)
 *
 * - 역할: 사용자가 웹사이트에서 일기를 "작성해줘!", "목록 보여줘!", "이거 지워줘!" 같은 요청을 보내면, 여기서 받아서 적절한 처리를 지시
 * - 로그인 연동: 로그인한 사용자의 일기만 다룰 수 있도록 세션 정보를 활용
 * - API 제공: 각 기능은 외부(프론트엔드)에서 호출할 수 있는 API 형태로 제공
 */
@RestController // 이 클래스는 데이터(주로 JSON)를 반환하는 API 컨트롤러임은 선언
public class DiaryController
{
    // 'final' 키워드: 이 객체들은 한 번 할당되면 변경되지 않음을 의미 (안정성 UP)
    private final DiaryRepository diaryRepository; // 일기 데이터베이스 담당
    private final UserService userService;         // 사용자 관련 서비스 담당 (여기서는 작성자 ID 가져올 때 필요)

    /**
     * 생성자 (객체가 만들어질 때 호출됨)
     * - 스프링이 알아서 DiaryRepository와 UserService 타입의 객체(Bean)를 찾아서 여기에 넣어줌 (의존성 주입, DI)
     */
    public DiaryController(DiaryRepository diaryRepository, UserService userService)
    {
        this.diaryRepository = diaryRepository;
        this.userService = userService;
    }

    /**
     * 📋 [GET/diaries] 현재 로그인한 사용자의 전체 일기 목록 조회
     * - 요청 URL 예시: /diaries (최신순 기본) 또는 /diaries?sort=asc (오래된순)
     * - 로그인 필수: 로그인하지 않은 사용자는 접근할 수 없습니다.
     * @param sort 정렬 방식 ("asc" 또는 "desc", 기본값 "desc") - URL 쿼리 파라미터로 받음
     * @param session 현재 사용자의 세션 정보 (스프링이 알아서 넣어줌)
     * @return 성공 시: 일기 목록 (List<Diary>)과 HTTP 200 OK 상태
     * 실패 시 (미로그인): 에러 메시지와 HTTP 401 Unauthorized 상태
     */
    @GetMapping("/diaries") // HTTP GET 요청이 "/diaries" 경로로 오면 이 메서드가 실행
    public ResponseEntity<?> getDiaries(@RequestParam(defaultValue = "desc") String sort, HttpSession session)
    {
        // 1. 세션에서 현재 로그인된 사용자의 ID를 가져옵니다.
        Long userId = (Long) session.getAttribute("userId");

        // 2. 로그인 상태 확인
        if (userId == null)
        {
            // 로그인 안 되어 있으면 "로그인이 필요합니다." 메시지와 함께 401 에러 응답
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        List<Diary> diaries; // 조회된 일기들을 담을 리스트

        // 3. 정렬 방식에 따라 다른 메서드를 호출하여 일기 목록을 가져옵니다.
        //    (DiaryRepository에 미리 정의된 쿼리 메서드 사용)
        if (sort.equals("asc"))
        { // "asc"면 오래된순
            diaries = diaryRepository.findAllByUserIdOrderByCreatedAtAsc(userId);
        } else
        { // 그 외에는 최신순 (기본값 "desc" 포함)
            diaries = diaryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        }

        // 4. 성공적으로 조회된 일기 목록과 함께 200 OK 응답
        return ResponseEntity.ok(diaries);
    }

    /**
     * ✍️ [POST /diaries] 새 일기 작성 (사진 첨부 가능)
     * - HTML form에서 multipart/form-data 형식으로 제목, 내용, 이미지 파일을 받아 처리
     * - 로그인 필수: 로그인한 사용자만 일기를 작성할 수 있음
     * @param title 일기 제목 (폼 데이터 'title' 필드)
     * @param content 일기 내용 (폼 데이터 'content' 필드)
     * @param imageFile 첨부 이미지 파일 (폼 데이터 'image' 필드, 필수는 아님)
     * @param session 현재 사용자의 세션 정보
     * @return 성공 시: "Diary created successfully!" 메시지와 HTTP 200 OK 상태
     * 실패 시 (미로그인): 에러 메시지와 HTTP 401 Unauthorized 상태
     * @throws IOException 파일 저장 중 발생할 수 있는 예외
     */
    @PostMapping("/diaries") // HTTP POST 요청이 "/diaries" 경로로 오면 이 메서드가 실행
    public ResponseEntity<?> createDiary(@RequestParam("title") String title,
                                         @RequestParam("content") String content,
                                         @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                         HttpSession session) throws IOException
    {
        // 1. 세션에서 현재 로그인된 사용자의 ID를 가져온다.
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String imagePath = null;  // 이미지 파일이 저장될 경로 (DB에 저장될 값)

        // 2. 이미지 파일이 첨부되었는지 확인하고, 첨부되었다면 서버에 저장
        if (imageFile != null && !imageFile.isEmpty())
        { // 파일이 존재하고 비어있지 않다면
            // 2-1. 파일 저장 경로 설정 (프로젝트 루트/uploads/ 폴더)
            //      WebMvcConfig에서 설정한 "/uploads/**" URL과 연결될 실제 저장 위치
            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            // 2-2. 파일 이름 중복 방지를 위해 현재 시간과 원래 파일 이름을 조합하여 새 파일 이름 생성
            String originalFilename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            String filePath = uploadDir + originalFilename; // 전체 파일 경로 (예: /path/to/project/uploads/1678886400000_photo.jpg)

            File dest = new File(filePath); // 저장할 파일 객체 생성
            dest.getParentFile().mkdirs();  // 만약 /uploads/ 폴더가 없다면 자동으로 생성
            imageFile.transferTo(dest);     // 실제 파일 저장

            // 2-3. DB에 저장할 이미지 경로는 웹에서 접근 가능한 URL 형태로 저장 (WebMvcConfig 설정과 연관)
            imagePath = "uploads/" + originalFilename; // 예: "uploads/1678886400000_photo.jpg"
        }

        // 3. Diary 객체 생성 및 정보 설정
        Diary diary = new Diary();
        diary.setTitle(title);
        diary.setContent(content);
        diary.setImagePath(imagePath); // 이미지가 없으면 null이 저장
        diary.setUserIdRef(userId);    // 🔥중요: 이 일기가 어떤 사용자의 것인지 ID(참조키)를 저장

        // 4. DiaryRepository를 통해 데이터베이스에 저장
        diaryRepository.save(diary);

        return ResponseEntity.ok("Diary created successfully!"); // 성공 메시지 반환
    }

    /**
     * 🗑️ [DELETE /diaries/{id}] 특정 일기 삭제
     * - URL 경로에 포함된 ID를 기준으로 일기를 삭제
     * - (개선점: 현재는 로그인한 사용자의 일기인지 확인하는 로직이 없으므로,
     *   다른 사용자의 일기도 ID만 알면 삭제 가능할 수 있다. 보안상 userId 검증 필요!)
     * @param id 삭제할 일기의 ID (URL 경로에서 추출)
     */
    @DeleteMapping("/diaries/{id}") // 예: /diaries/10 (10번 일기 삭제)
    public void deleteDiary(@PathVariable Long id)
    { // @PathVariable: URL 경로의 {id} 값을 가져옴
        diaryRepository.deleteById(id); // ID를 기준으로 DB에서 해당 일기 삭제
        // 별도의 성공 메시지 없이 HTTP 200 OK (기본) 응답
    }

    /**
     * 🛠️ [PATCH  /diaries/{id}] 특정 일기 수정
     * - URL 경로에 포함된 ID를 기준으로 일기 내용을 수정
     * - 요청 본문(JSON)으로 수정할 제목과 내용을 받음
     * - (개선점: 이미지 수정 기능은 현재 미포함, userId 검증 로직 추가 고려)
     * @param id 수정할 일기의 ID (URL 경로에서 추출)
     * @param diaryRequest 수정할 내용을 담은 Diary 객체 (요청 본문의 JSON 데이터를 Diary 객체로 변환)
     * (실제로는 DiaryUpdateDto 같은 별도 DTO를 사용하는 것이 더 좋음.)
     * @return 성공 시: "Diary updated successfully" 메시지와 HTTP 200 OK 상태
     * 실패 시 (일기 없음): RuntimeException 발생 (전역 예외 처리기에서 처리될 수 있음)
     */
    @PatchMapping("/diaries/{id}") // HTTP PATCH 요청 (일부만 수정)
    public ResponseEntity<String> updateDiary(@PathVariable Long id, @RequestBody Diary diaryRequest)
    {
        // 1. 수정할 일기를 ID로 조회합니다.
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diary not found")); // 없으면 예외 발생

        // 2. 요청받은 내용으로 일기 정보 업데이트
        //    (주의: diaryRequest에 title이나 content가 null로 오면 해당 필드도 null로 업데이트될 수 있음.
        //     실제로는 null이 아닌 값만 업데이트하도록 로직 추가 필요)
        diary.setTitle(diaryRequest.getTitle());
        diary.setContent(diaryRequest.getContent());
        // 이미지 경로(imagePath)나 작성자(userIdRef)는 여기서 수정하지 않음

        // 3. 수정된 일기 정보 저장
        diaryRepository.save(diary);

        return ResponseEntity.ok("Diary updated successfully");
    }
}
