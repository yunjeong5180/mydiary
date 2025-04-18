package com.diary.mydiary.repository;

import com.diary.mydiary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 사용자 정보를 DB에서 관리해주는 인터페이스
public interface UserRepository extends JpaRepository<User, Long>
{
    // username으로 사용자 검색 (로그인에 필요!)
    Optional<User> findByUsername(String username);
}
