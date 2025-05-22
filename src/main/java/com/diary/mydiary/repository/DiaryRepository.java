package com.diary.mydiary.repository;

import com.diary.mydiary.model.Diary;                         // Diary 엔티티 클래스를 임포트
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA의 핵심 리포지토리 인터페이스
import java.util.List;                                        // 여러 개의 Diary 객체를 담기 위한 List 인터페이스

/**
 * 💾 [DiaryRepository] - Diary 엔티티에 대한 데이터베이스 접근(CRUD)을 담당하는 인터페이스
 *
 * Spring Data JPA의 JpaRepository<Diary, Long>를 상속받음으로써,
 * Diary 엔티티(테이블)에 대한 기본적인 데이터베이스 작업 메서드들(예: save, findById, findAll, deleteById 등)을
 * 우리가 직접 구현하지 않아도 자동으로 사용할 수 있음
 * - Diary: 이 리포지토리가 다룰 엔티티의 타입
 * - Long: 해당 엔티티의 ID 필드 타입 (Diary 엔티티의 id 필드가 Long 타입임)
 *
 * 여기에 추가적으로 필요한 조회 메서드는 Spring Data JPA의 "쿼리 메서드" 규칙에 따라
 * 메서드 시그니처(이름, 파라미터)만 선언해주면, 스프링이 알아서 적절한 JPQL 쿼리를 생성하고 실행해줌
 */
public interface DiaryRepository extends JpaRepository<Diary, Long>
{

    /**
     * 🕒 모든 사용자의 일기를 작성일(createdAt) 기준으로 최신순 정렬
     * - Spring Data JPA가 이 메서드 이름을 분석하여 "SELECT d FROM Diary d ORDER BY d.createdAt DESC" 와 유사한 쿼리 생성
     * @return 최신순으로 정렬된 모든 일기 목록 (List<Diary>)
     */
    List<Diary> findAllByOrderByCreatedAtDesc();

    /**
     * 🕒 모든 사용자의 일기를 작성일(createdAt) 기준으로 오래된순 정렬
     * - Spring Data JPA가 "SELECT d FROM Diary d ORDER BY d.createdAt ASC" 와 유사한 쿼리 생성
     * @return 오래된순으로 정렬된 모든 일기 목록 (List<Diary>)
     */
    List<Diary> findAllByOrderByCreatedAtAsc();

    /**
     * 👤 특정 사용자(userId)의 일기들을 작성일(createdAt) 기준으로 최신순 정렬 조회
     *
     * @param userId 조회하고자 하는 사용자의 ID
     * @return 해당 사용자의 일기 목록 (최신순 정렬)
     */
    List<Diary> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 👤 특정 사용자(userId)의 일기들을 작성일(createdAt) 기준으로 오래된순 조회
     *
     * @param userId 조회하고자 하는 사용자의 ID
     * @return 해당 사용자의 일기 목록 (오래된순 정렬)
     */
    List<Diary> findAllByUserIdOrderByCreatedAtAsc(Long userId);
}
