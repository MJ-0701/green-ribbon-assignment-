package com.example.greenribboncalimassignment.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final String code;

    private final int status;

    private final String message;
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
