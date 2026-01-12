package com.example.greenribboncalimassignment.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    // 1. 정상 (200)
    SUCCESS("SUCCESS", 200, "정상적으로 처리되었습니다."),
    CREATED("CREATED",201,"리소스가 생성되었습니다."),

    // 2. 비즈니스 에러 (Custom Status)
    USER_NOT_FOUND("NOT_FOUND", 4000, "존재하지 않는 사용자입니다."),
    INTERNAL_SERVER_ERROR("SERVER_ERROR", 9999, "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final int status;
    private final String message;
}
