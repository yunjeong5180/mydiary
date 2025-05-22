package com.diary.mydiary.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 📨 API 응답을 위한 표준 형식 DTO
 * 모든 API는 이 클래스 또는 이와 유사한 형태로 응답하여 클라이언트(웹 브라우저 등)가 일관된 방식으로 결과를 처리
 *
 * - @Getter: 모든 필드의 getter 메서드를 Lombok이 자동 생성 (예: isSuccess(), getMessage())
 * - @Setter: 모든 필드의 setter 메서드를 Lombok이 자동 생성 (예: setSuccess(boolean success))
 * - @NoArgsConstructor: 파라미터가 없는 기본 생성자를 Lombok이 자동 생성 (JSON 라이브러리 등이 객체 생성 시 필요.)
 * - @AllArgsConstructor: 모든 필드를 파라미터로 받는 생성자를 Lombok이 자동 생성
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse
{

     //✅ API 요청 처리 성공 여부 (true: 성공, false: 실패)
    private boolean success;

    // 💬 API 요청 처리 결과 메시지 (예: "로그인 성공", "데이터를 찾을 수 없습니다.")
    private String message;

    /**
     * 📦 API 요청 처리 결과 데이터 (선택 사항)
     * 성공 시 여기에 실제 데이터를 담아 보낼 수 있습니다. (예: 사용자 정보, 일기 목록 등)
     * 어떤 타입의 데이터든 담을 수 있도록 Object 타입으로 선언되었습니다.
     */
    private Object data; // 선택적으로 데이터 포함 가능

    /**
     * 🙋 성공 여부와 메시지만으로 ApiResponse 객체를 생성하는 편의 생성자입니다.
     * 데이터(data 필드) 없이 간단한 응답을 보낼 때 사용합니다.
     *
     * @param success API 요청 처리 성공 여부
     * @param message API 요청 처리 결과 메시지
     */
    public ApiResponse(boolean success, String message)
    {
        this.success = success;
        this.message = message;
        // this.data는 null로 유지
    }
}