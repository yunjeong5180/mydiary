package com.diary.mydiary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 📬 비밀번호 재설정 요청 시 클라이언트에서 서버로 전달하는 데이터를 담는 DTO
 * 사용자의 아이디(또는 로그인 ID)와 가입 시 사용한 이메일 주소 포함
 * 서버는 이 정보를 바탕으로 해당 사용자가 맞는지 확인하고 재설정 절차 진행
 *
 * - @Getter: 모든 필드의 getter 메서드를 Lombok이 자동 생성
 * - @Setter: 모든 필드의 setter 메서드를 Lombok이 자동 생성
 * - @NoArgsConstructor: Lombok이 파라미터 없는 기본 생성자 자동 생성
 * - @AllArgsConstructor: Lombok이 모든 필드를 파라미터로 받는 생성자 자동 생성
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest
{
    /**
     * 🆔 비밀번호 재설정을 요청하는 사용자의 아이디 (실제로는 username, 즉 로그인 ID를 의미)
     * (필드명이 userId로 되어있지만, 서비스 로직에서는 이를 username으로 해석하여 사용)
     */
    private String userId;

    /**
     * 📧 비밀번호 재설정을 요청하는 사용자의 가입 이메일 주소
     */
    private String email;
}