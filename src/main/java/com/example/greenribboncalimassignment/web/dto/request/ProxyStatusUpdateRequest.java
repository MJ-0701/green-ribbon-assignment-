package com.example.greenribboncalimassignment.web.dto.request;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "청구 대행 상태 변경 요청 DTO")
public record ProxyStatusUpdateRequest(
        @Schema(description = "변경하려는 목표 상태 (IN_PROGRESS, FEE_CLAIM, CANCELLED, DISCLAIMER)", example = "IN_PROGRESS")
        @NotNull(message = "변경할 상태는 필수입니다.")
        ProxyStatus status, // 변경하려는 목표 상태

        @Schema(description = "상태 변경 사유 (이력 저장용)", example = "서류 수집 및 심사 진행을 위해 상태 변경")
        String reason // 상태 변경 사유
) {
}
