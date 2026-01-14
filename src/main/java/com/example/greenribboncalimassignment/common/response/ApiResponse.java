package com.example.greenribboncalimassignment.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "시스템 공통 표준 응답")
public class ApiResponse<T> {

    @Schema(description = "응답 코드 (성공: SUCCESS, 실패: 에러 코드)", example = "SUCCESS")
    private final String code;

    @Schema(description = "응답 상태 값 (HTTP Status와 동일하거나 내부 정의 코드)", example = "200")
    private final int status;

    @Schema(description = "응답 메시지", example = "정상적으로 처리되었습니다.")
    private final String message;

    @Schema(description = "응답 데이터 (성공 시 결과 객체, 실패 시 null일 수 있음)")
    private final T data;

    // 생성자에서 Enum의 모든 정보를 다 가져옵니다.
    private ApiResponse(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.status = resultCode.getStatus(); // ★ Enum의 숫자값 매핑
        this.message = resultCode.getMessage();
        this.data = data;
    }

    // --- 정적 팩토리 메서드 ---

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS, data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResultCode.SUCCESS, null);
    }

    public static <T> ApiResponse<T> of(ResultCode resultCode, T data) {
        return new ApiResponse<>(resultCode, data);
    }

    public static <T> ApiResponse<T> of(ResultCode resultCode) {
        return new ApiResponse<>(resultCode, null);
    }
}