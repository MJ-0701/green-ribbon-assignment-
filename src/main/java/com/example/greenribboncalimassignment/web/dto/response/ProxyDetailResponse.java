package com.example.greenribboncalimassignment.web.dto.response;

import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProxyDetailResponse(
        ProxyInfoDto proxyInfo,
        List<ProxyUnitDto> units,
        List<ProxyHistoryDto> histories
) {

    public record ProxyInfoDto(
            Long proxyRequestId,
            Long userId,
            String userName,
            GuaranteeType guaranteeType,
            ProxyStatus status,
            Long totalMissedAmount,
            Long feeAmount,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt
    ) {
        @QueryProjection
        public ProxyInfoDto {}
    }

    public record ProxyUnitDto(
            Long unitId,
            String hospitalName,
            Long missedAmount,
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate recentTreatmentDate
    ) {
        @QueryProjection
        public ProxyUnitDto {}
    }

    public record ProxyHistoryDto(
            ProxyStatus status,
            String reason,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime changedAt
    ) {
        @QueryProjection
        public ProxyHistoryDto {}
    }
}
