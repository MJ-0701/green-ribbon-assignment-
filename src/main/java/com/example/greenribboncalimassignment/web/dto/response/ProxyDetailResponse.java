package com.example.greenribboncalimassignment.web.dto.response;

import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "청구 대행 신청 상세 조회 응답 DTO")
public record ProxyDetailResponse(
        @Schema(description = "신청 기본 정보")
        ProxyInfoDto proxyInfo,

        @Schema(description = "병원별 청구 단위 목록 (상세 내역)")
        List<ProxyUnitDto> units,

        @Schema(description = "상태 변경 이력 목록")
        List<ProxyHistoryDto> histories
) {

    @Schema(description = "청구 대행 신청 기본 정보")
    public record ProxyInfoDto(
            @Schema(description = "청구 대행 신청 ID", example = "1")
            Long proxyRequestId,

            @Schema(description = "신청자(유저) ID", example = "1")
            Long userId,

            @Schema(description = "신청자 이름", example = "채명정")
            String userName,

            @Schema(description = "보장 타입", example = "NORMAL_POSTPAID")
            GuaranteeType guaranteeType,

            @Schema(description = "현재 진행 상태", example = "PENDING")
            ProxyStatus status,

            @Schema(description = "총 누락 보험금 (합산 금액)", example = "600000")
            Long totalMissedAmount,

            @Schema(description = "예상 수수료", example = "120000")
            Long feeAmount,

            @Schema(description = "신청 일시", example = "2023-10-25 14:30:00")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt
    ) {
        @QueryProjection
        public ProxyInfoDto {}
    }

    @Schema(description = "병원별 청구 단위 상세 정보")
    public record ProxyUnitDto(
            @Schema(description = "단위 ID", example = "1")
            Long unitId,

            @Schema(description = "병원 이름", example = "세브란스병원")
            String hospitalName,

            @Schema(description = "해당 병원 누락 금액", example = "500000")
            Long missedAmount,

            @Schema(description = "해당 병원 최근 진료일", example = "2023-07-20")
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate recentTreatmentDate
    ) {
        @QueryProjection
        public ProxyUnitDto {}
    }

    @Schema(description = "상태 변경 이력 정보")
    public record ProxyHistoryDto(
            @Schema(description = "변경 당시 상태", example = "PENDING")
            ProxyStatus status,

            @Schema(description = "변경 사유", example = "신규 대행 신청 접수 (병원 2곳 합산)")
            String reason,

            @Schema(description = "변경 일시", example = "2023-10-25 14:30:00")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime changedAt
    ) {
        @QueryProjection
        public ProxyHistoryDto {}
    }
}