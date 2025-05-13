package com.diary.mydiary.dto;

import lombok.Data;

@Data
public class FindIdResponse
{ // 서버가 클라이언트로 보낼 응답
    private boolean success;
    private String message;
    private String maskedUserId;

    // 기본 생성자
    public FindIdResponse() {
    }

    // 모든 필드를 받는 생성자
    public FindIdResponse(boolean success, String message, String maskedUserId) {
        this.success = success;
        this.message = message;
        this.maskedUserId = maskedUserId;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMaskedUserId() {
        return maskedUserId;
    }

    public void setMaskedUserId(String maskedUserId) {
        this.maskedUserId = maskedUserId;
    }

}
