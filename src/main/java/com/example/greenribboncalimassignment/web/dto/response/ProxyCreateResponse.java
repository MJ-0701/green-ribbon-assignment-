package com.example.greenribboncalimassignment.web.dto.response;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "청구 대행 신청 생성 결과 응답 DTO")
public record ProxyCreateResponse(
        @Schema(description = "생성된 청구 대행 신청 ID", example = "1")
        Long proxyRequestId,

        @Schema(description = "초기 진행 상태 (생성 직후이므로 PENDING)", example = "PENDING")
        ProxyStatus status,

        @Schema(description = "신청한 진료 기록의 총 누락 보험금 (합산 금액)", example = "600000")
        Long totalMissedAmount,

        @Schema(description = "예상 수수료 (선택한 보장 타입 요율 적용)", example = "120000")
        Long feeAmount
) {
    public static ProxyCreateResponse from(ProxyRequest request) {
        return new ProxyCreateResponse(
                request.getId(),
                request.getStatus(),
                request.getTotalMissedAmount(),
                request.getFeeAmount()
        );
    }
}
