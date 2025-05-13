package com.diary.mydiary.dto;

import lombok.Data;

@Data
public class FindIdRequest
{ // 클라이언트로부터 받을 이메일 정보 (private String email;)
    private String email;

    // 기본 생성자 (JSON 역직렬화를 위해 필요할 수 있음)
    public FindIdRequest() {
    }

    // 모든 필드를 받는 생성자
    public FindIdRequest(String email) {
        this.email = email;
    }

    // Getter
    public String getEmail() {
        return email;
    }

    // Setter
    public void setEmail(String email) {
        this.email = email;
    }
}