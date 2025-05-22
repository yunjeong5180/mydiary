package com.diary.mydiary.repository;

import com.diary.mydiary.model.PasswordResetTokenEntity;
import com.diary.mydiary.model.User; // User 모델 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;       // 만료된 토큰 삭제 쿼리에 사용될 수 있음
import java.util.Optional;   // Optional 반환 타입을 위해
import java.util.stream.Stream; // 스트림 기반 처리를 위해 (선택적)

/**
 * 🔑 [PasswordResetTokenRepository] - PasswordResetTokenEntity에 대한 데이터베이스 접근을 처리하는 인터페이스
 *
 * JpaRepository<PasswordResetTokenEntity, Long>를 상속받아,
 * 비밀번호 재설정 토큰 정보에 대한 기본적인 CRUD(생성, 조회, 수정, 삭제) 기능을 자동으로 제공받음
 * 추가적인 조회 조건이나 커스텀 쿼리가 필요할 경우, Spring Data JPA의 쿼리 메서드 규칙에 따라
 * 메서드를 선언하거나 @Query 어노테이션을 사용하여 직접 JPQL을 작성할 수 있음
 *
 * - @Repository: 이 인터페이스가 데이터 접근 계층의 컴포넌트(리포지토리 빈)임을 스프링에게 알려줌
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long>
{
    /**
     * 🔍 주어진 토큰 문자열(token)을 기준으로 PasswordResetTokenEntity를 조회
     * 토큰 문자열은 고유(unique)하므로, 결과는 하나이거나 없을 수 있다.
     * - Optional<T>: 조회 결과가 없을 수도 있는 경우(null 대신) 사용하는 컨테이너 객체
     *
     * @param token 조회하고자 하는 비밀번호 재설정 토큰 문자열
     * @return 해당 토큰 문자열을 가진 PasswordResetTokenEntity 객체를 담은 Optional (없으면 빈 Optional)
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * 👤 특정 사용자(User 객체)와 연관된 모든 비밀번호 재설정 토큰 조회
     * 한 사용자가 여러 번 비밀번호 재설정을 요청했을 경우, 여러 개의 토큰이 존재할 수 있다.
     *
     * @param user 토큰을 조회할 대상 사용자 객체
     * @return 해당 사용자와 연관된 모든 PasswordResetTokenEntity 객체들의 스트림
     */
    Stream<PasswordResetTokenEntity> findAllByUser(User user);

    /**
     * 🗑️ 특정 날짜(now) 이전에 만료된 모든 비밀번호 재설정 토큰들을 데이터베이스에서 삭제함
     * 주기적인 스케줄링 작업(예: 매일 자정)을 통해 오래되어 더 이상 유효하지 않은 토큰들을 정리하여 데이터베이스를 깨끗하게 유지하는 데 사용함
     *
     * @param now 기준이 되는 날짜 및 시간 (이 시간보다 이전에 만료된 토큰들이 삭제 대상)
     */
    void deleteAllByExpiryDateLessThan(Date now);

}