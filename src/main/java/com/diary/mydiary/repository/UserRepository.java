package com.diary.mydiary.repository;

import com.diary.mydiary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 👤 [UserRepository] - User 엔티티에 대한 데이터베이스 접근(CRUD)을 담당하는 인터페이스
 *
 * JpaRepository<User, Long>를 상속받아, 사용자(User) 정보에 대한 기본적인 데이터베이스 연산을 별도의 구현 없이 바로 사용할 수 있게 해줌
 * (예: 회원가입 시 사용자 저장, 로그인 시 사용자 조회 등)
 *
 * Spring Data JPA의 쿼리 메서드 기능을 활용하여, 필요한 조회 조건을 메서드 이름으로 표현하면 해당 JPQL 쿼리가 자동 생성
 */
public interface UserRepository extends JpaRepository<User, Long>
{

    /**
     * 🔍 아이디(username)를 기준으로 사용자 정보 조회
     * 로그인 시 사용자가 입력한 아이디를 통해 DB에서 해당 사용자를 찾을 때 사용
     *
     * @param username 조회할 사용자의 아이디 문자열
     * @return 해당 아이디를 가진 User 객체를 담은 Optional (사용자가 없으면 빈 Optional)
     */
    Optional<User> findByUsername(String username);

    /**
     * 🔍 사용자의 고유 ID(Primary Key)를 기준으로 사용자 정보 조회
     *
     * @param id 조회할 사용자의 고유 ID (Long 타입)
     * @return 해당 ID를 가진 User 객체를 담은 Optional (사용자가 없으면 빈 Optional)
     */
    Optional<User> findById(Long id);

    /**
     * 📧 이메일 주소(email)를 기준으로 사용자 정보 조회
     * 아이디 찾기, 이메일 중복 확인 등의 기능에서 사용
     *
     * @param email 조회할 사용자의 이메일 주소 문자열
     * @return 해당 이메일 주소를 가진 User 객체를 담은 Optional (사용자가 없으면 빈 Optional)
     */
    Optional<User> findByEmail(String email); // 메소드 (User 엔티티에 email 필드 가정)

    /**
     * 🆔+📧 아이디(username)와 이메일 주소(email)가 모두 일치하는 사용자 정보 조회
     * 비밀번호 재설정 요청 시, 사용자가 입력한 아이디와 이메일이 모두 DB 정보와 일치하는지 확인할 때 사용
     *
     * @param username 조회할 사용자의 아이디 문자열
     * @param email 조회할 사용자의 이메일 주소 문자열
     * @return 아이디와 이메일이 모두 일치하는 User 객체를 담은 Optional (해당 사용자가 없으면 빈 Optional)
     */
    Optional<User> findByUsernameAndEmail(String username, String email);
}
