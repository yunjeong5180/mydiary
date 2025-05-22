package com.diary.mydiary.dto;

import lombok.Data;

/**
 * 📬 아이디 찾기 요청에 대해 서버가 클라이언트(웹 브라우저)로 응답하는 데이터를 담는 DTO
 * 아이디 찾기 성공 여부, 결과 메시지, (성공 시) 마스킹 처리된 사용자 아이디 포함
 *
 * - @Data: Lombok 어노테이션으로, 주요 메서드(getter, setter, toString 등)를 자동 생성
 */
@Data
public class FindIdResponse
{
    // ✅ 아이디 찾기 성공 여부 (true: 성공, false: 실패)
    private boolean success;

    // 💬 아이디 찾기 결과 메시지
    private String message;

    // 👤 (성공 시) 마스킹 처리된 사용자 아이디 (예: "test****")
    private String maskedUserId;

    // JSON 역직렬화 등을 위한 기본 생성자
    public FindIdResponse() { }

    /**
     * 모든 필드 값을 받아 FindIdResponse 객체를 생성하는 생성자
     *
     * @param success 아이디 찾기 성공 여부
     * @param message 결과 메시지
     * @param maskedUserId 마스킹 처리된 사용자 아이디 (실패 시 null일 수 있음)
     */
    public FindIdResponse(boolean success, String message, String maskedUserId)
    {
        this.success = success;
        this.message = message;
        this.maskedUserId = maskedUserId;
    }
}
