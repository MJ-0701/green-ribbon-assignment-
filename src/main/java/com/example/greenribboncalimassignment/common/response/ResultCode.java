package com.example.greenribboncalimassignment.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    // --- 1. 정상 (2xx) ---
    SUCCESS("SUCCESS", 200, "정상적으로 처리되었습니다."),
    CREATED("CREATED", 201, "리소스가 생성되었습니다."),

    // --- 2. 클라이언트 에러 (4xx) ---
    // 2-1. 요청 오류 (공통)
    INVALID_INPUT_VALUE("INVALID_INPUT", 400, "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", 405, "허용되지 않는 HTTP 메서드입니다."),

    // 2-2. 유저 관련 (40xx)
    USER_NOT_FOUND("USER_NOT_FOUND", 4000, "존재하지 않는 사용자입니다."),

    // 2-3. 진료 기록 관련 (41xx)
    TREATMENT_NOT_FOUND("TREATMENT_NOT_FOUND", 4100, "조회된 진료 기록이 없거나, 요청한 개수와 일치하지 않습니다."),
    ALREADY_PROCESSED_TREATMENT("ALREADY_PROCESSED", 4101, "이미 청구 대행이 진행되었거나 완료된 진료 기록이 포함되어 있습니다."),

    // 2-4. 청구 대행(Proxy) 관련 (42xx)
    PROXY_REQUEST_NOT_FOUND("PROXY_NOT_FOUND", 4200, "존재하지 않는 청구 대행 신청입니다."),
    DUPLICATE_REQUEST_NOT_ALLOWED("DUPLICATE_REQUEST", 4201, "이미 진행 중인 청구 대행 건이 있어 신청할 수 없습니다."),
    INVALID_STATUS_TRANSITION("INVALID_TRANSITION", 4202, "변경할 수 없는 상태입니다."),
    CANCEL_ONLY_AT_PENDING("CANCEL_RESTRICTED", 4203, "취소는 '대행 신청 완료(PENDING)' 상태에서만 가능합니다."),

    // --- 3. 서버 에러 (5xx) ---
    INTERNAL_SERVER_ERROR("SERVER_ERROR", 9999, "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final int status;
    private final String message;
}
