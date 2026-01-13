package com.example.greenribboncalimassignment.web.dto.response;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;

public record ProxyCreateResponse(
        Long proxyRequestId,
        ProxyStatus status,
        Long totalMissedAmount,
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
