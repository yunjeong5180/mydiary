package com.diary.mydiary.dto;

import jakarta.validation.constraints.NotBlank; // @NotBlank 어노테이션을 위해 임포트
import jakarta.validation.constraints.Size;   // @Size 어노테이션을 위해 임포트
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 🔑 비밀번호 재설정 시 실제 새 비밀번호를 전달하기 위한 DTO 클래스입니다.
 * 프론트엔드에서 사용자가 입력한 토큰과 새 비밀번호를 담아 서버로 전송합니다.
 */
@Getter
@Setter
@NoArgsConstructor // Jackson 등의 라이브러리가 JSON을 객체로 변환할 때 기본 생성자를 사용합니다.
public class ResetPasswordRequest {

    /**
     * 사용자가 이메일로 받은 비밀번호 재설정 토큰입니다.
     * 이 토큰의 유효성을 서버에서 검증합니다.
     */
    @NotBlank(message = "토큰은 필수 입력 항목입니다.") // null, "", " " (공백만 있는 문자열)을 허용하지 않음
    private String token;

    /**
     * 사용자가 새로 설정할 비밀번호입니다.
     * 비밀번호 정책(예: 최소 길이, 문자 조합 등)을 만족해야 합니다.
     */
    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.") // 예시: 최소 8자, 최대 20자
    // TODO: 필요하다면 더 강력한 비밀번호 정책을 위한 @Pattern 어노테이션 추가 고려
    // 예: 영문 대소문자, 숫자, 특수문자 조합 등
    // @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    //          message="비밀번호는 영문 대소문자, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.")
    private String newPassword;

    // Lombok의 @Getter, @Setter 어노테이션이 있으므로,
    // token과 newPassword 필드에 대한 getter와 setter 메소드는 자동으로 생성됩니다.
    // @AllArgsConstructor // 모든 필드를 받는 생성자가 필요하면 이 어노테이션을 추가할 수 있습니다.
}