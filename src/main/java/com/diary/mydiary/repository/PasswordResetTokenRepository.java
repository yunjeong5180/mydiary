package com.diary.mydiary.repository;

import com.diary.mydiary.model.PasswordResetTokenEntity;
import com.diary.mydiary.model.User; // User 모델 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;       // 만료된 토큰 삭제 쿼리에 사용될 수 있음
import java.util.Optional;   // Optional 반환 타입을 위해
import java.util.stream.Stream; // 스트림 기반 처리를 위해 (선택적)

/**
 * 🔑 PasswordResetTokenEntity에 대한 데이터 접근을 처리하는 리포지토리 인터페이스
 */
@Repository // 이 인터페이스가 Spring Data 리포지토리임을 나타냅니다. (선택적이지만 명시적으로 붙이는 것이 좋음)
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    /**
     * 주어진 토큰 문자열로 PasswordResetTokenEntity를 찾는다
     * @param token 찾고자 하는 토큰 문자열
     * @return Optional<PasswordResetTokenEntity> 토큰이 존재하면 해당 엔티티를, 없으면 빈 Optional을 반환
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * 특정 사용자와 연관된 모든 비밀번호 재설정 토큰을 찾는다.
     * (예: 사용자가 여러 번 재설정 요청을 했을 경우, 이전 토큰을 무효화하거나 관리할 때 사용 가능)
     * @param user 사용자 객체
     * @return Stream<PasswordResetTokenEntity> 해당 사용자의 토큰 스트림
     */
    Stream<PasswordResetTokenEntity> findAllByUser(User user);

    /**
     * 특정 날짜 이전에 만료된 모든 토큰 삭제
     * (예: 주기적인 스케줄링 작업으로 오래된 토큰을 정리할 때 사용 가능)
     * @param now 현재 날짜 또는 기준 날짜
     */
    void deleteAllByExpiryDateLessThan(Date now);

    // 필요에 따라 @Modifying, @Query 어노테이션을 사용하여 커스텀 업데이트/삭제 쿼리를 작성할 수 있습니다.
    // 예: 특정 사용자의 모든 유효한 토큰을 무효화(isUsed = true)하는 쿼리
    // @Modifying
    // @Query("update PasswordResetTokenEntity p set p.isUsed = true where p.user = ?1 and p.isUsed = false and p.expiryDate > CURRENT_TIMESTAMP")
    // void invalidateActiveTokensForUser(User user);
}