package com.diary.mydiary.dto;

import jakarta.validation.constraints.NotBlank; // 필드가 null이 아니고, 공백만으로 이루어지지 않았는지 검증
import jakarta.validation.constraints.Size;   // 문자열 길이 검증
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 🔄 비밀번호를 실제로 변경할 때 클라이언트에서 서버로 전달하는 데이터를 담는 DTO
 * 사용자가 이메일 등으로 받은 비밀번호 재설정 토큰과 새로 사용할 비밀번호 포함
 *
 * - @Getter: Lombok이 모든 필드의 getter 메서드 자동 생성
 * - @Setter: Lombok이 모든 필드의 setter 메서드 자동 생성
 * - @NoArgsConstructor: Lombok이 파라미터 없는 기본 생성자 자동 생성
 * (Jackson 같은 JSON 라이브러리가 JSON을 객체로 변환할 때 필요)
 */
@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordRequest
{

    /**
     * 💨 사용자가 이메일 등을 통해 받은 비밀번호 재설정용 고유 토큰 문자열
     * 서버는 이 토큰의 유효성(존재 여부, 만료 여부, 사용 여부 등) 검증
     *
     * - @NotBlank: 이 필드는 null, 빈 문자열(""), 공백(" ")만있는 문자열을 허용하지 않음
     * 유효성 검사 실패 시 message에 지정된 메시지 사용
     */
    @NotBlank(message = "토큰은 필수 입력 항목입니다.")
    private String token;

    /**
     * 🆕 사용자가 새로 설정하고자 하는 비밀번호
     * 서버에서 정의된 비밀번호 정책(예: 최소/최대 길이, 문자 조합 등)을 만족해야 한다.
     *
     * - @NotBlank: 새 비밀번호는 필수 입력 항목
     * - @Size: 비밀번호의 길이 제한 (여기서는 최소 8자, 최대 20자로 설정)
     */
    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.") // 예시: 최소 8자, 최대 20자
    // TODO: 필요하다면 더 강력한 비밀번호 정책을 위한 @Pattern 어노테이션 추가 고려
    // 예: 영문 대소문자, 숫자, 특수문자 조합 등
    // @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    //          message="비밀번호는 영문 대소문자, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.")
    private String newPassword;

}