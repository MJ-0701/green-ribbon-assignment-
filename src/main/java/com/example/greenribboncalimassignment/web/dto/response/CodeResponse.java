package com.example.greenribboncalimassignment.web.dto.response;

import com.example.greenribboncalimassignment.common.CodeValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 코드(Enum) 응답 DTO")
public record CodeResponse(
        @Schema(description = "코드 값 (Enum 상수명 - 서버 전송용)", example = "PENDING")
        String code,

        @Schema(description = "코드 설명 (화면 표시용)", example = "대행 신청 완료")
        String description
) {
    public static CodeResponse from(CodeValue codeValue) {
        return new CodeResponse(codeValue.getCode(), codeValue.getDescription());
    }
}