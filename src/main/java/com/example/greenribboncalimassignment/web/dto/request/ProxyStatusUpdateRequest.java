package com.example.greenribboncalimassignment.web.dto.request;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import jakarta.validation.constraints.NotNull;

public record ProxyStatusUpdateRequest(
        @NotNull(message = "변경할 상태는 필수입니다.")
        ProxyStatus status, // 변경하려는 목표 상태

        String reason // 상태 변경 사유
) {
}
