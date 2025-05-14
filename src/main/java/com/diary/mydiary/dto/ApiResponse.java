package com.diary.mydiary.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data; // 선택적으로 데이터 포함 가능


    // 성공 여부와 메시지만 받는 생성자
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

}