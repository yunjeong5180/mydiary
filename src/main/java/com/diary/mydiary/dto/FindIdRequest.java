package com.diary.mydiary.dto;

// @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor를 합쳐놓은 Lombok 어노테이션
import lombok.Data;

/**
 * 📧 아이디 찾기 요청 시 클라이언트(웹 브라우저)에서 서버로 전달하는 데이터를 담는 DTO
 * 사용자가 아이디를 찾기 위해 입력한 이메일 주소를 포함
 *
 * - @Data: Lombok 어노테이션으로, 주요 메서드(getter, setter, toString 등)를 자동으로 생성해줍니다.
 */
@Data
public class FindIdRequest
{
    // 🔎 아이디를 찾기 위해 사용자가 입력한 이메일 주소
    private String email;

    // 기본 생성자 (클라이언트에서 온 JSON 데이터를 이 객체로 변환할 때 사용)
    public FindIdRequest() {
    }

    // 이메일 주소를 받아 FindIdRequest 객체를 생성하는 생성자
    public FindIdRequest(String email) {
        this.email = email;
    }
}