package com.diary.mydiary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Lombok 사용 시
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest
{
    private String userId; // 또는 username 등 실제 사용하는 필드명
    private String email;

}