package com.diary.mydiary.repository;

import com.diary.mydiary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 📁 [UserRepository] - 회원 정보를 저장하고 조회하는 인터페이스
 *
 * 이 인터페이스는 JpaRepository를 상속받아
 * 사용자(User) 데이터를 DB에 저장하거나 불러오는 기능을 자동으로 제공합니다.
 */
public interface UserRepository extends JpaRepository<User, Long>
{

    /**
     * 🔍 username(아이디)로 사용자 찾기
     * - 로그인 시 사용자가 입력한 아이디를 기준으로 DB에서 사용자 정보를 조회합니다.
     * - 결과가 없을 수도 있으므로 Optional로 감싸서 반환합니다.
     */
    Optional<User> findByUsername(String username);

    /**
     * 🔍 id(기본키)로 사용자 찾기
     * - 사용자의 고유 ID를 기준으로 DB에서 사용자 정보를 조회합니다.
     * - 결과가 없을 수도 있으므로 Optional로 감싸서 반환합니다.
     */
    Optional<User> findById(Long id); // ✅ 명시적으로 추가 (JpaRepository에도 있지만 명확히 적어줌)

    Optional<User> findByEmail(String email); // 메소드 (User 엔티티에 email 필드 가정)
}
